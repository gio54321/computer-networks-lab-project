package winsome.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import winsome.common.requests.CommentRequest;
import winsome.common.requests.LoginRequest;
import winsome.common.requests.PostRequest;
import winsome.common.requests.RateRequest;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.MulticastResponse;
import winsome.common.responses.PartialRewardResponse;
import winsome.common.responses.PostIdResponse;
import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
import winsome.common.responses.WalletResponse;
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

/**
 * This class implements all the routing exit points
 * for the actual implementation of the functionalities
 * The annotations above the methods serves as documentation
 * for the method/path of the functionality
 * 
 * When the DeserializeBody annotation is present, the router will
 * deserialize the body of the request and it will pass it to the
 * method as an object
 * 
 * When the Authenticate annotation is present, the router uses its
 * internal authInterface to authenticate the user. The router will
 * pass the calling username and it will guarantee that the request
 * was authenticated properly (if the credentials were wrong, the router
 * would not call the method, but it would fail with an UNAUTHORIZED response)
 * 
 */
public class RESTLogic {
    // the server main database
    private Database database;
    // the followers callback service
    private FollowersCallbackServiceImpl callbackService;

    // multicast informations
    private String multicastAddress = "";
    private int multicastPort = 0;

    public RESTLogic(Database database, FollowersCallbackServiceImpl callbackService) {
        if (database == null || callbackService == null) {
            throw new NullPointerException();
        }
        this.database = database;
        this.callbackService = callbackService;
    }

    @Route(method = HTTPMethod.POST, path = "/login")
    @DeserializeRequestBody(LoginRequest.class)
    public HTTPResponse login(LoginRequest body) {
        try {
            this.database.beginOp();

            // try to login the user
            var token = this.database.login(body.username, body.password);

            this.database.endOp();
            return HTTPResponse.response(HTTPResponseCode.OK, new LoginResponse(token));
        } catch (UserDoesNotExistsException e) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (UserAlreadyLoggedInException e) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User is already logged in");
        } catch (AuthenticationException e) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Route(method = HTTPMethod.DELETE, path = "/login")
    @Authenticate
    public HTTPResponse logout(String callingUsername) {
        // logout the user

        this.database.beginOp();
        this.database.logout(callingUsername);
        this.database.endOp();

        return new HTTPResponse(HTTPResponseCode.OK);
    }

    @Route(method = HTTPMethod.GET, path = "/users")
    @Authenticate
    public HTTPResponse listUsers(String callingUsername) {
        // list the users that hae at least one tag in
        // common with username
        this.database.beginOp();
        List<UserResponse> users = this.database.listUsers(callingUsername);
        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, users);
    }

    @Route(method = HTTPMethod.PUT, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse followUser(String callingUsername, String toFollowUsername) {
        // follow request from callingUsername to toFollowUser

        this.database.beginOp();

        // check if toFollowUsername is equal to calling username
        if (callingUsername.contentEquals(toFollowUsername)) {
            this.database.endOp();
            // user cannot follow itself
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "user cannot follow itself");
        }

        try {
            // try to follow toFollowUsername
            var done = this.database.followUser(callingUsername, toFollowUsername);

            // only notify the action to the followed user if the calling user
            // was not previously following it
            if (done) {
                this.callbackService.notifyFollow(toFollowUsername, this.database.getUserResponse(callingUsername));
            }

            this.database.endOp();
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (RemoteException e) {
            this.database.endOp();

            // if the notification failed, then by callingUser perspective it is still
            // a success
            return new HTTPResponse(HTTPResponseCode.OK);
        }
    }

    @Route(method = HTTPMethod.DELETE, path = "/followers/{toFollowUser}")
    @Authenticate
    public HTTPResponse unfollowUser(String callingUsername, String toUnfollowUsername) {
        // unfollow request from callingUser to toUnfollowUsername
        // this operation must be done with exclusive access to database, since it
        // removes some mappings in the database

        this.database.beginExclusive();

        // check if toUnfollowUsername is equal to calling username
        if (callingUsername.contentEquals(toUnfollowUsername)) {
            // user cannot follow itself
            this.database.endExclusive();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "user cannot unfollow itself");
        }

        try {
            // try to unfollow
            var done = this.database.unfollowUser(callingUsername, toUnfollowUsername);

            // only notify the action to the unfollowed user if the calling user
            // was previously following it
            if (done) {
                this.callbackService.notifyUnfollow(toUnfollowUsername, this.database.getUserResponse(callingUsername));
            }

            this.database.endExclusive();
            return new HTTPResponse(HTTPResponseCode.OK);
        } catch (UserDoesNotExistsException e) {
            this.database.endExclusive();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (RemoteException e) {
            this.database.endExclusive();

            // if the notification failed, then by callingUser perspective it is still
            // a success
            return new HTTPResponse(HTTPResponseCode.OK);
        }
    }

    @Route(method = HTTPMethod.GET, path = "/following")
    @Authenticate
    public HTTPResponse listFollowing(String username) {
        // get list of following
        this.database.beginOp();
        List<UserResponse> users = this.database.listFollowing(username);
        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, users);
    }

    @Route(method = HTTPMethod.POST, path = "/posts")
    @DeserializeRequestBody(PostRequest.class)
    @Authenticate
    public HTTPResponse createPost(String callingUsername, PostRequest reqBody) {
        // request to create a post

        this.database.beginOp();

        // check that the post title length is below the maximum length
        if (reqBody.title.length() > Constants.MAX_POST_TITLE_LENGTH) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "title is too long");
        }

        // check that the post content length is below the maximum length
        if (reqBody.content.length() > Constants.MAX_POST_CONTENT_LENGTH) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "content is too long");
        }

        // create the post
        var newPostId = this.database.addPostToDatabase(callingUsername, reqBody.title, reqBody.content);

        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.CREATED, new PostIdResponse(newPostId));
    }

    @Route(method = HTTPMethod.GET, path = "/posts/{idPost}")
    @Authenticate
    public HTTPResponse getPost(String callingUsername, int idPost) {
        // get a post by id
        this.database.beginOp();
        var res = this.database.getPostFromId(idPost);
        this.database.endOp();

        // if the post does not exists return NOT FOUND
        if (res == null) {
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }

        return HTTPResponse.response(HTTPResponseCode.OK, res);
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/rewins")
    @Authenticate
    public HTTPResponse rewinPost(String callingUsername, int postId) {
        // rewin a post
        this.database.beginOp();

        // check if the post actually exists
        if (!this.database.postExists(postId)) {
            this.database.endOp();
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }

        // check that the post's author is not the calling user
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "author cannot rewin its own post");
        }

        // check that the post is actually in the feed of the calling user
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }

        // rewin the post
        var rewinRes = this.database.rewinPost(callingUsername, postId);
        this.database.endOp();

        if (rewinRes) {
            return new HTTPResponse(HTTPResponseCode.OK);
        } else {
            // if the post was already rewinned, reuturn an error
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "already rewinned");
        }
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/rates")
    @DeserializeRequestBody(RateRequest.class)
    @Authenticate
    public HTTPResponse ratePost(String callingUsername, int postId, RateRequest reqBody) {
        // rate a post

        this.database.beginOp();

        // check if the post actually exists
        if (!this.database.postExists(postId)) {
            this.database.endOp();
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }

        // check that the post's author is not the calling user
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "author cannot vote its own post");
        }

        // check that the post is actually in the feed of the calling user
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }

        // check that the rate is either +1 or -1
        if (reqBody.rate != 1 && reqBody.rate != -1) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "vote is not valid");
        }

        // rate the post
        var rewinRes = this.database.ratePost(callingUsername, postId, reqBody.rate);
        this.database.endOp();

        if (rewinRes) {
            return new HTTPResponse(HTTPResponseCode.OK);
        } else {
            // if the post was already rated return an error
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "already rated");
        }
    }

    @Route(method = HTTPMethod.POST, path = "/posts/{idPost}/comments")
    @DeserializeRequestBody(CommentRequest.class)
    @Authenticate
    public HTTPResponse commentPost(String callingUsername, int postId, CommentRequest reqBody) {
        // comment a post
        this.database.beginOp();

        // check if the post actually exists
        if (!this.database.postExists(postId)) {
            this.database.endOp();
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }

        // check that the post's author is not the calling user
        if (this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "author cannot comment its own post");
        }

        // check that the post is actually in the feed of the calling user
        if (!this.database.postIsInFeed(callingUsername, postId)) {
            this.database.endOp();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY, "the post is not in the feed");
        }

        // add a comment to the post
        this.database.addComment(postId, callingUsername, reqBody.content);

        this.database.endOp();
        return new HTTPResponse(HTTPResponseCode.CREATED);
    }

    @Route(method = HTTPMethod.GET, path = "/posts")
    @Authenticate
    public HTTPResponse viewBlog(String callingUsername) {
        // get the blog of the calling user

        this.database.beginOp();

        // get the blog post ids
        var blogIds = this.database.getPostsIdsFromAuthor(callingUsername);
        // convert post ids into post responses
        List<PostResponse> responseList = this.database.getPostReponsesFromIds(blogIds);

        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, responseList);
    }

    @Route(method = HTTPMethod.GET, path = "/feed")
    @Authenticate
    public HTTPResponse viewFeed(String callingUsername) {
        this.database.beginOp();

        // get the blog post ids
        var blogIds = this.database.getFeedPostIds(callingUsername);
        // convert post ids into post responses
        List<PostResponse> responseList = this.database.getPostReponsesFromIds(blogIds);

        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, responseList);
    }

    @Route(method = HTTPMethod.DELETE, path = "/posts/{idPost}")
    @Authenticate
    public HTTPResponse deletePost(String callingUsername, int postId) {
        // delete post is a sensitive operation that has to be
        // treated differently

        // first for this operation we require exclusive access to the entire database
        this.database.beginExclusive();

        // check if the post actually exists
        if (!this.database.postExists(postId)) {
            this.database.endExclusive();
            return new HTTPResponse(HTTPResponseCode.NOT_FOUND);
        }

        // check that the post's author is the calling user
        if (!this.database.getPostAuthor(postId).contentEquals(callingUsername)) {
            this.database.endExclusive();
            return HTTPResponse.errorResponse(HTTPResponseCode.UNPROCESSABLE_ENTITY,
                    "only author can delete the post");
        }

        // delete the post
        this.database.deletePost(postId);

        // relase exclusive access
        this.database.endExclusive();

        return new HTTPResponse(HTTPResponseCode.OK);
    }

    @Route(method = HTTPMethod.GET, path = "/wallet")
    @Authenticate
    public HTTPResponse getWallet(String callingUsername) {
        // get the wallet of the calling user

        this.database.beginOp();

        // get the reward history
        var history = this.database.getRewardHistory(callingUsername);
        // get the wallet amount
        var wallet = this.database.getWallet(callingUsername);

        // forge the response
        var response = new WalletResponse();
        response.incrementHistory = history.toArray(new PartialRewardResponse[0]);
        response.wallet = wallet;

        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, response);
    }

    @Route(method = HTTPMethod.GET, path = "/wallet/btc")
    @Authenticate
    public HTTPResponse getWalletInBtc(String callingUsername) {
        // get the wallet of calling user converted in BTC

        this.database.beginOp();

        // try to get the conversion rate
        Double conversionRate;
        try {
            conversionRate = BTCExchangeService.getWincoinToBTCConversionRate();
        } catch (IOException e) {
            this.database.endOp();
            // something went wrong with getting the conversion rate
            return HTTPResponse.errorResponse(HTTPResponseCode.INTERNAL_SERVER_ERROR, "could not get conversion rate");
        }

        // get the wallet history
        var history = this.database.getRewardHistory(callingUsername);

        // convert the wallet history to BTC
        for (var entry : history) {
            entry.partialReward *= conversionRate;
        }

        // get the wallet amount and convert it to BTC
        var wallet = this.database.getWallet(callingUsername) * conversionRate;

        // forge the response
        var response = new WalletResponse();
        response.incrementHistory = history.toArray(new PartialRewardResponse[0]);
        response.wallet = wallet;

        this.database.endOp();
        return HTTPResponse.response(HTTPResponseCode.OK, response);
    }

    /**
     * Set the multicast information that will be returned on a
     * GET /multicast request
     * 
     * @param multicastAddress the mustlicast group address
     * @param multicastPort    the multicast port
     */
    public void setMulticastInformations(String multicastAddress, int multicastPort) {
        if (multicastAddress == null) {
            throw new NullPointerException();
        }
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    @Route(method = HTTPMethod.GET, path = "/multicast")
    public HTTPResponse getMulticast() {
        // get multicast information

        // forge the response
        var response = new MulticastResponse();
        response.multicastAddress = this.multicastAddress;
        response.port = this.multicastPort;

        return HTTPResponse.response(HTTPResponseCode.OK, response);
    }

}
