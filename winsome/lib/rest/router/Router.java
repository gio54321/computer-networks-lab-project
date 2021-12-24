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
        validateAnnotations(actionsObject.getClass());
        this.boundObject = actionsObject;
    }

    private boolean validateAnnotations(Class<?> actionsClass) {
        for (Method method : actionsClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Pattern p = Pattern.compile("/\\{([^{}]+)\\}");

                Route route = method.getAnnotation(Route.class);
                Parameter[] methodParameters = method.getParameters();

                var path = route.value();
                Matcher m = p.matcher(path);

                System.out.println(route.value() + " -> " + method.getName());

                String synthethizedRegex = path;
                int i = 0;
                while (m.find()) {
                    if (methodParameters.length < i + 1) {
                        System.out.println("too few arguments to match " + path + " for method " + method.getName());
                        return false;
                    }
                    Matcher m1 = p.matcher(synthethizedRegex);
                    System.out.println(m.group(1));
                    if (methodParameters[i].getType() == int.class) {
                        synthethizedRegex = m1.replaceFirst("/(\\\\d+)");
                    }
                    System.out.println(synthethizedRegex);
                    i++;
                }

                if (methodParameters.length > i) {
                    System.out.println("too many arguments to match " + path + " for method " + method.getName());
                    return false;
                }

                this.bindings.put(Pattern.compile(synthethizedRegex), method);
            }
        }
        return true;
    }

    // TODO return type of this thing?
    public void callAction(String path) {

        System.out.println("===============");
        for (var k : this.bindings.keySet()) {
            Matcher m = k.matcher(path);
            System.out.println(k.pattern());
            if (m.matches()) {
                System.out.println(path);

                Method toCallAction = this.bindings.get(k);
                Parameter[] methodParameters = toCallAction.getParameters();

                Object[] toCallParams = new Object[m.groupCount()];
                for (int i = 0; i < m.groupCount(); ++i) {
                    if (methodParameters[i].getType() == int.class) {
                        toCallParams[i] = Integer.parseInt(m.group(i + 1));
                    }
                }

                try {
                    toCallAction.invoke(this.boundObject, toCallParams);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }
}