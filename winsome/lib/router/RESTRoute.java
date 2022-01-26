package winsome.lib.router;

import java.util.regex.Pattern;

import winsome.lib.http.HTTPMethod;

/**
 * Class that represent a pair <pattern, method>
 */
public class RESTRoute {
    private Pattern pattern;
    private HTTPMethod method;

    public RESTRoute(Pattern pattern, HTTPMethod method) {
        this.pattern = pattern;
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(HTTPMethod method) {
        this.method = method;
    }

}
