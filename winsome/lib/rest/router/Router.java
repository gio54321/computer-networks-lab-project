package winsome.lib.rest.router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {
    private Object boundObject;
    private HashMap<Pattern, Method> bindings = new HashMap<>();

    public Router(Object actionsObject) {
        validateAnnotationsAndBind(actionsObject.getClass());
        this.boundObject = actionsObject;
    }

    /**
     * Validate annotations and bind path to actions.
     * Every method of the class of the actionClass is checked for the Route
     * annotation. If it has one the path contained is matched against the method's
     * parameters. The path template is transformed into a regex that matches all
     * the instances of that template. The types are given by the method's parameter
     * types. Only the integer and string types are supported.
     * 
     * The generated regex is then put in the bindings map, together with the
     * matching method
     * 
     * Example:
     * 
     * TODO change return type
     * @Route("/foo/{id}/bar/{n}")
     * public type baz(int a, int b) {...}
     * 
     * id is bound to a
     * n is bound to b
     * the generated regex that matches all the istances of the template is
     * /foo/(\d+)/bar/(\d+)
     * the entry </foo/(\d+)/bar/(\d+), baz> is added to the bindings map
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
        for (Method method : actionsClass.getDeclaredMethods()) {

            // check if the method is annotated with the @Route annotation
            if (method.isAnnotationPresent(Route.class)) {
                // get the actual object of the annotation and the path contained
                Route routeAnnotation = method.getAnnotation(Route.class);
                String path = routeAnnotation.value();

                // get the list of parameters of the method
                Parameter[] methodParameters = method.getParameters();

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
                                + method.getName());
                        return false;
                    }

                    // create a new matcher to replace the parameter in the output regex
                    Matcher replaceParameterMatcher = pathParameterPattern.matcher(synthethizedRegex);

                    // replace the template parameter to a regex choosing from the input type
                    if (methodParameters[i].getType() == int.class) {
                        synthethizedRegex = replaceParameterMatcher.replaceFirst("(\\\\d+)");
                    }

                    // increment the counter
                    i++;
                }

                // chek that the method does not have more arguments than there are parameters
                // in the api path
                if (methodParameters.length > i) {
                    System.out.println("Router binding error: too many arguments to match " + path + " for method "
                            + method.getName());
                    return false;
                }

                // finally put the output regex in the bindings map, together with the mapped
                // method
                this.bindings.put(Pattern.compile(synthethizedRegex), method);
            }
        }
        return true;
    }

    // TODO return type of this thing?
    // TODO 404?
    /**
     * Call bound action from path.
     * Match the path with a route path and invoke the corresponding method.
     * The invocation is done on the object that has been passed to the constructor.
     * 
     * @param path the api path instance
     */
    public void callAction(String path) {
        // for each entry in the bindings map
        for (Pattern requestInstancePattern : this.bindings.keySet()) {

            // try to match the path to the request pattern
            Matcher requestInstanceMatcher = requestInstancePattern.matcher(path);
            if (requestInstanceMatcher.matches()) {
                // get the bound method for the request
                Method toCallAction = this.bindings.get(requestInstancePattern);

                // bind the actual data in the path to the method parameter
                // according to their types
                Parameter[] methodParameters = toCallAction.getParameters();
                Object[] toCallParams = new Object[requestInstanceMatcher.groupCount()];
                for (int i = 0; i < requestInstanceMatcher.groupCount(); ++i) {
                    if (methodParameters[i].getType() == int.class) {
                        toCallParams[i] = Integer.parseInt(requestInstanceMatcher.group(i + 1));
                    }
                }

                // finally invoke the method
                try {
                    toCallAction.invoke(this.boundObject, toCallParams);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
        }
    }
}