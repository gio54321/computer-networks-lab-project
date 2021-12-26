package winsome.lib.rest.router;

import java.util.regex.Pattern;

import winsome.lib.rest.RESTMethod;

// TODO doc
public class RESTRoute {
    private Pattern pattern;
    private RESTMethod method;

    public RESTRoute(Pattern pattern, RESTMethod method) {
        this.pattern = pattern;
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public RESTMethod getMethod() {
        return method;
    }

    public void setMethod(RESTMethod method) {
        this.method = method;
    }

}
