package winsome.server;

import java.rmi.RemoteException;
import java.util.List;

import winsome.common.requests.CommentRequest;
import winsome.common.requests.LoginRequest;
import winsome.common.requests.PostRequest;
import winsome.common.requests.RateRequest;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.PostIdResponse;
import winsome.common.responses.PostResponse;
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
        var newPostId = this.database.addPostToDatabase(callingUsername, reqBody.title, reqBody.content);
        return HTTPResponse.response(HTTPResponseCode.OK, new PostIdResponse(newPostId));
    }

    @Route(method = HTTPMethod.GET, path = "/posts/{idPost}")
    @Authenticate
    public HTTPResponse getPost(String username, int idPost) {
        var res = this.database.getPostFromId(idPost);
        if (res == null) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }
        return HTTPResponse.response(HTTPResponseCode.OK, res);
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/rewins")
    @Authenticate
    public HTTPResponse createPost(String callingUsername, int postId) {
        if (!this.database.postExists(postId)) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "author cannot rewin its own post");
        }
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }
        var rewinRes = this.database.rewinPost(callingUsername, postId);
        if (rewinRes) {
            return new HTTPResponse(HTTPResponseCode.OK);
        } else {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "already rewinned");
        }
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/rates")
    @DeserializeRequestBody(RateRequest.class)
    @Authenticate
    public HTTPResponse ratePost(String callingUsername, int postId, RateRequest reqBody) {
        System.out.println(postId);
        if (!this.database.postExists(postId)) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "author cannot vote its own post");
        }
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }
        if (reqBody.rate != 1 && reqBody.rate != -1) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "vote is not valid");
        }
        var rewinRes = this.database.ratePost(callingUsername, postId, reqBody.rate);
        if (rewinRes) {
            return new HTTPResponse(HTTPResponseCode.OK);
        } else {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "already rated");
        }
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/comments")
    @DeserializeRequestBody(CommentRequest.class)
    @Authenticate
    public HTTPResponse commentPost(String callingUsername, int postId, CommentRequest reqBody) {
        System.out.println(postId);
        if (!this.database.postExists(postId)) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "author cannot comment its own post");
        }
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }
        // TODO maybe check for null?
        this.database.addComment(postId, callingUsername, reqBody.content);
        return new HTTPResponse(HTTPResponseCode.OK);
    }

    @Route(method = HTTPMethod.GET, path = "/posts")
    @Authenticate
    public HTTPResponse viewBlog(String callingUsername) {
        var blogIds = this.database.getPostsIdsFromAuthor(callingUsername);
        List<PostResponse> responseList = this.database.getPostReponsesFromIds(blogIds);
        return HTTPResponse.response(HTTPResponseCode.OK, responseList);
    }

    @Route(method = HTTPMethod.GET, path = "/feed")
    @Authenticate
    public HTTPResponse viewFeed(String callingUsername) {
        var blogIds = this.database.getFeedPostIds(callingUsername);
        List<PostResponse> responseList = this.database.getPostReponsesFromIds(blogIds);
        return HTTPResponse.response(HTTPResponseCode.OK, responseList);
    }

    @Route(method = HTTPMethod.DELETE, path = "/posts/{idPost}")
    @Authenticate
    public HTTPResponse deletePost(String callingUsername, int postId) {
        System.out.println(postId);
        if (!this.database.postExists(postId)) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }
        if (!this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "only author can delete the post");
        }
        this.database.deletePost(postId);
        return new HTTPResponse(HTTPResponseCode.OK);
    }
}
