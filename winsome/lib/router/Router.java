package winsome.lib.router;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.requests.Request;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPRequest;
import winsome.lib.http.HTTPResponse;

public class Router {
    private Object boundObject;
    private HashMap<RESTRoute, Method> bindings = new HashMap<>();
    private HashMap<Method, Class<? extends Request>> deserializationMap = new HashMap<>();

    public Router(Object actionsObject) throws InvalidRouteAnnotationException {
        if (!validateAnnotationsAndBind(actionsObject.getClass())) {
            throw new InvalidRouteAnnotationException();
        }
        this.boundObject = actionsObject;
    }

    /**
     * Validate annotations and bind path to actions.
     * Every method of the class of the actionClass is checked for the Route
     * annotation. If it has one the path contained is matched against the method's
     * parameters. The path template is transformed into a regex that matches all
     * the instances of that template. The types are given by the method's parameter
     * types. Only the integer and string types are supported. The REST method
     * is also stored.
     * 
     * The generated regex is then put in the bindings map, together with the
     * matching method
     * 
     * Example:
     * 
     * TODO change return type
     * 
     * @Route(path="/foo/{id}/bar/{n}", method=RESTMethod.POST);
     * public type baz(int a, int b) {...}
     * 
     * id is bound to a
     * n is bound to b
     * the generated regex that matches all the
     * istances of the template is
     * /foo/(\d+)/bar/(\d+)
     * the entry <</foo/(\d+)/bar/(\d+), POST>, baz> is
     * added to the bindings map
     * 
     * @param actionsClass the Route annotated class
     * @return true if all the matching and bindings succeded, false otherwise
     */
    private boolean validateAnnotationsAndBind(Class<?> actionsClass) {
        // this regex pattern matches any valid path api, few examples are:
        // "/", "/foo/{bar}/baz", "/{foo}/4"
        final Pattern wellFormedApiPathPattern = Pattern.compile("((/\\w+|/\\{\\w+\\})+)|/");

        // this regex pattern matches only parameters in the api path, like
        // "{foo}" or "{bar}"
        final Pattern pathParameterPattern = Pattern.compile("\\{([^{}]+)\\}");

        // iterate on the methods of the class of the provided object
        for (Method classMethod : actionsClass.getDeclaredMethods()) {

            // check if the method is annotated with the @Route annotation
            if (classMethod.isAnnotationPresent(Route.class)) {
                // get the actual object of the annotation and the path contained
                Route routeAnnotation = classMethod.getAnnotation(Route.class);
                String path = routeAnnotation.path();
                HTTPMethod restMethod = routeAnnotation.method();

                // check if the method returns a RESTResponse
                if (classMethod.getReturnType() != HTTPResponse.class) {
                    System.out.println("Router binding error: the return type of " + classMethod.getName()
                            + " must be RESTResponse");
                    return false;
                }

                // get the list of parameters of the method
                Parameter[] methodParameters = classMethod.getParameters();

                // check if the path is well formed
                Matcher apiPathMatcher = wellFormedApiPathPattern.matcher(path);
                if (!apiPathMatcher.matches()) {
                    System.out.println("Router binding error: the path " + path + "is not well formed");
                    return false;
                }

                // create a new matcher for the parameters in the path
                Matcher parameterMatcher = pathParameterPattern.matcher(path);
                // initialize the output regex
                String synthethizedRegex = path;
                int i = 0;

                // for each match in the api path
                while (parameterMatcher.find()) {
                    // check if the method has enough arguments
                    if (methodParameters.length < i + 1) {
                        System.out.println("Router binding error: too few arguments to match " + path + " for method "
                                + classMethod.getName());
                        return false;
                    }

                    // create a new matcher to replace the parameter in the output regex
                    Matcher replaceParameterMatcher = pathParameterPattern.matcher(synthethizedRegex);

                    // replace the template parameter to a regex choosing from the input type
                    if (methodParameters[i].getType() == int.class) {
                        synthethizedRegex = replaceParameterMatcher.replaceFirst("(\\\\d+)");
                    } else if (methodParameters[i].getType() == String.class) {
                        synthethizedRegex = replaceParameterMatcher.replaceFirst("(\\\\w+)");
                    } else {
                        System.out.println(
                                "Router binding error: unsupported type " + methodParameters[i].getType().toString());
                        return false;
                    }

                    // increment the counter
                    i++;
                }

                // TODO doc
                if (classMethod.isAnnotationPresent(DeserializeRequestBody.class)) {
                    var annotation = classMethod.getAnnotation(DeserializeRequestBody.class);
                    if (methodParameters.length < i + 1) {
                        System.out.println("Router binding error: too few arguments to match " + path + " for method "
                                + classMethod.getName());
                        return false;
                    }
                    if (methodParameters[i].getType() != annotation.value()) {
                        System.out.println(
                                "Router binding error: incorrect body type "
                                        + methodParameters[i].getType().toString());
                        return false;
                    }
                    this.deserializationMap.put(classMethod, annotation.value());
                    i++;
                }

                // chek that the method does not have more arguments than there are parameters
                // in the api path
                if (methodParameters.length > i) {
                    System.out.println("Router binding error: too many arguments to match " + path + " for method "
                            + classMethod.getName());
                    return false;
                }

                // finally put the output regex and method in the bindings map, together with
                // the mapped method
                var route = new RESTRoute(Pattern.compile(synthethizedRegex), restMethod);
                this.bindings.put(route, classMethod);
            }
        }
        return true;
    }

    /**
     * Call bound action from path.
     * Match the request with a route path and invoke the corresponding method.
     * The invocation is done on the object that has been passed to the constructor.
     * 
     * @param path the api path instance
     * @return the response to send to the client
     */
    public HTTPResponse callAction(HTTPRequest request) {
        var path = request.getPath();
        var method = request.getMethod();

        // for each entry in the bindings map
        for (RESTRoute route : this.bindings.keySet()) {

            var requestInstancePattern = route.getPattern();
            // check if the request methods match, if not skip the iteration
            if (route.getMethod() != method) {
                continue;
            }

            // try to match the path to the request pattern
            Matcher requestInstanceMatcher = requestInstancePattern.matcher(path);
            if (requestInstanceMatcher.matches()) {
                // get the bound method for the request
                Method toCallAction = this.bindings.get(route);

                Request deserializedBody = null;
                boolean deserializeBody = false;
                if (this.deserializationMap.containsKey(toCallAction)) {
                    deserializeBody = true;
                    var mapper = new ObjectMapper();
                    try {
                        deserializedBody = mapper.readValue(request.getBody(),
                                this.deserializationMap.get(toCallAction));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // bind the actual data in the path to the method parameter
                // according to their types
                Parameter[] methodParameters = toCallAction.getParameters();
                Object[] toCallParams = new Object[methodParameters.length];
                for (int i = 0; i < methodParameters.length; ++i) {
                    if (methodParameters[i].getType() == int.class) {
                        toCallParams[i] = Integer.parseInt(requestInstanceMatcher.group(i + 1));
                    } else if (methodParameters[i].getType() == String.class) {
                        toCallParams[i] = requestInstanceMatcher.group(i + 1);
                    }
                    if (deserializeBody && i == methodParameters.length - 1) {
                        toCallParams[i] = deserializedBody;
                    }
                }

                // TODO find a better way to deal with failure
                HTTPResponse response = null;
                // finally invoke the method
                try {
                    // the cast to RESTResponse is safe since the return type of
                    // the method has been checked in the validation phase
                    response = (HTTPResponse) toCallAction.invoke(this.boundObject, toCallParams);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return response;
            }
        }
        // TODO find a better way to deal with failure
        return null;
    }
}