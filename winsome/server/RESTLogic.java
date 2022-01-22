package winsome.server;

import java.util.List;

import winsome.common.requests.LoginRequest;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.UserResponse;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.router.Authenticate;
import winsome.lib.router.DeserializeRequestBody;
import winsome.lib.router.Route;
import winsome.server.database.Database;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyLoggedInException;
import winsome.server.database.exceptions.UserDoesNotExistsException;

public class RESTLogic {
    private Database database;

    public RESTLogic(Database database) {
        this.database = database;
    }

    @Route(method = HTTPMethod.POST, path = "/login")
    @DeserializeRequestBody(LoginRequest.class)
    public HTTPResponse login(LoginRequest body) {
        String token;
        try {
            token = this.database.login(body.username, body.password);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (UserAlreadyLoggedInException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User is already logged in");
        } catch (AuthenticationException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "Invalid credentials");
        }
        return HTTPResponse.response(HTTPResponseCode.OK, new LoginResponse(token));
    }

    @Route(method = HTTPMethod.DELETE, path = "/login")
    @Authenticate
    public HTTPResponse logout(String username) {
        this.database.logout(username);
        return new HTTPResponse(HTTPResponseCode.OK);
    }

    @Route(method = HTTPMethod.GET, path = "/users")
    @Authenticate
    public HTTPResponse listUsers(String username) {
        List<UserResponse> users = this.database.listUsers(username);
        return HTTPResponse.response(HTTPResponseCode.OK, users);
    }

    @Route(method = HTTPMethod.POST, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse followUser(String callingUsername, String toFollowUsername) {
        System.out.println(callingUsername + "   " + toFollowUsername);
        try {
            this.database.followUser(callingUsername, toFollowUsername);
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        }
    }

    @Route(method = HTTPMethod.DELETE, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse unfollowUser(String callingUsername, String toUnfollowUsername) {
        System.out.println(callingUsername + "   " + toUnfollowUsername);
        try {
            this.database.unfollowUser(callingUsername, toUnfollowUsername);
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        }
    }

    @Route(method = HTTPMethod.GET, path = "/following")
    @Authenticate
    public HTTPResponse listFollowing(String username) {
        List<UserResponse> users = this.database.listFollowing(username);
        return HTTPResponse.response(HTTPResponseCode.OK, users);
    }
}
