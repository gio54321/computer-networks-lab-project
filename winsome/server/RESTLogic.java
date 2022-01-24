package winsome.server;

import java.rmi.RemoteException;
import java.util.List;

import winsome.common.requests.LoginRequest;
import winsome.common.requests.PostRequest;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.PostIdResponse;
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
import winsome.server.database.post.ContentPost;

public class RESTLogic {
    private Database database;
    private FollowersCallbackServiceImpl callbackService;

    public RESTLogic(Database database, FollowersCallbackServiceImpl callbackService) {
        this.database = database;
        this.callbackService = callbackService;
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

    @Route(method = HTTPMethod.PUT, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse followUser(String callingUsername, String toFollowUsername) {
        System.out.println(callingUsername + "   " + toFollowUsername);
        if (callingUsername.contentEquals(toFollowUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "user cannot follow itself");
        }
        try {
            var done = this.database.followUser(callingUsername, toFollowUsername);
            if (done) {
                this.callbackService.notifyFollow(toFollowUsername, this.database.getUserResponse(callingUsername));
            }
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (RemoteException e) {
            // TODO if the user failed to notify, then the request is still a success?
            return new HTTPResponse(HTTPResponseCode.OK);
        }
    }

    @Route(method = HTTPMethod.DELETE, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse unfollowUser(String callingUsername, String toUnfollowUsername) {
        System.out.println(callingUsername + "   " + toUnfollowUsername);
        if (callingUsername.contentEquals(toUnfollowUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "user cannot unfollow itself");
        }
        try {
            var done = this.database.unfollowUser(callingUsername, toUnfollowUsername);
            if (done) {
                this.callbackService.notifyUnfollow(toUnfollowUsername, this.database.getUserResponse(callingUsername));
            }
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (RemoteException e) {
            // TODO same as before
            return new HTTPResponse(HTTPResponseCode.OK);
        }
    }

    @Route(method = HTTPMethod.GET, path = "/following")
    @Authenticate
    public HTTPResponse listFollowing(String username) {
        List<UserResponse> users = this.database.listFollowing(username);
        return HTTPResponse.response(HTTPResponseCode.OK, users);
    }

    @Route(method = HTTPMethod.POST, path = "/posts")
    @DeserializeRequestBody(PostRequest.class)
    @Authenticate
    public HTTPResponse createPost(String callingUsername, PostRequest reqBody) {
        if (reqBody.title.length() > Constants.MAX_POST_TITLE_LENGTH) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "title is too long");
        }
        if (reqBody.content.length() > Constants.MAX_POST_CONTENT_LENGTH) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "content is too long");
        }
        var post = new ContentPost(callingUsername, reqBody.title, reqBody.content);
        var newPostId = this.database.addPostToDatabase(post);
        return HTTPResponse.response(HTTPResponseCode.OK, new PostIdResponse(newPostId));
    }
}
