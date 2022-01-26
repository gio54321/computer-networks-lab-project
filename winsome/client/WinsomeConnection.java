package winsome.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.requests.CommentRequest;
import winsome.common.requests.LoginRequest;
import winsome.common.requests.PostRequest;
import winsome.common.requests.RateRequest;
import winsome.common.responses.ErrorResponse;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.MulticastResponse;
import winsome.common.responses.PostIdResponse;
import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
import winsome.common.responses.WalletResponse;
import winsome.common.rmi.FollowersCallbackService;
import winsome.common.rmi.Registration;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPRequest;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.utils.Result;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyExistsException;
import winsome.common.rmi.FollowersCallback;

/**
 * Class that defines a Winsome connection.
 * It exports all the functionalities of the Winsome system
 * The majority of methods returns a type Result<String, String>, that can be
 * either an Ok or an Error
 * A winsomeConnection is stateful, because it retain login authorizations and
 * callback objects
 * It is important that the user calls the closeConnection() at the end of the
 * usage
 */
public class WinsomeConnection {
    /** RMI fields */
    // the registration object exported by the server
    private Registration registrationObj;
    // the callbackService exported by the server
    private FollowersCallbackService callbackService;
    // the callback object that the client exports to the server
    private FollowersCallbackImpl callbackObject;
    // the stub of the callback object
    private FollowersCallback callbackStub;

    /** REST connection fields */
    // the TCP socket
    private Socket socket;
    // the read end of the socket
    private BufferedReader connectionInput;
    // the write end of the socket
    private BufferedWriter connectionOutput;

    // global objectmapper
    private ObjectMapper mapper = new ObjectMapper();

    /** State fields */
    // the login username
    private String username = null;
    // authToken received from the server after login
    private String authToken = null;
    // the thread that listens for notifications on the multicast group
    private RewardsNotificationListener notificationListener;

    /**
     * Create a new Winsome Connection
     * 
     * @param serverAddress   the server address
     * @param serverPort      the server REST port
     * @param registryAddress the registry host name
     * @param registryPort    the registry port
     * @param netIfName       the network interface name
     * @throws NotBoundException
     * @throws IOException
     */
    public WinsomeConnection(InetAddress serverAddress, int serverPort, String registryAddress, int registryPort,
            String netIfName)
            throws NotBoundException, IOException {
        if (serverAddress == null) {
            throw new NullPointerException();
        }
        this.socket = new Socket(serverAddress, serverPort);
        // the input and output streams are encoded in US_ASCII
        this.connectionInput = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        this.connectionOutput = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

        // get the registration handler from registry
        var registry = LocateRegistry.getRegistry(registryAddress, registryPort);
        this.registrationObj = (Registration) registry.lookup("Registration-service");

        // set up followers callback
        this.callbackService = (FollowersCallbackService) registry.lookup("FollowersCallback-service");
        this.callbackObject = null;

        // start the multicast listener thread
        this.startMulticastListener(netIfName);
    }

    /**
     * Get multicast information from the server and start the multicast listener
     * 
     * @param netIfName the network interface name
     * @throws IOException
     */
    private void startMulticastListener(String netIfName) throws IOException {
        if (netIfName == null) {
            throw new NullPointerException();
        }
        // get the multicast informations from server at GET /multicast
        var request = new HTTPRequest(HTTPMethod.GET, "/multicast");
        sendRequest(request);
        try {
            var response = getResponse();
            if (response.getResponseCode() != HTTPResponseCode.OK) {
                System.out.println("ERROR receiving multicast informations from server");
            } else {
                var resBody = this.mapper.readValue(response.getBody(), MulticastResponse.class);

                // create a new notification listener and start it
                this.notificationListener = new RewardsNotificationListener(resBody.multicastAddress, resBody.port,
                        netIfName);
                this.notificationListener.start();
            }
        } catch (HTTPParsingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the winsome connection
     * If the user is logged in, then the logout operation is performed
     * Also stops the multicast notification listener
     * 
     * @throws IOException
     */
    public void closeConnection() throws IOException {
        if (this.username != null && this.authToken != null) {
            this.logout();
        }
        this.notificationListener.interrupt();
        this.socket.close();
    }

    /**
     * Send an HTTP request to the server
     * 
     * @param request
     * @throws IOException
     */
    private void sendRequest(HTTPRequest request) throws IOException {
        // get the formatted request
        var formattedRequest = request.getFormattedMessage();
        System.out.println("sending " + formattedRequest);
        // write on the output end of the socket
        this.connectionOutput.write(formattedRequest);
        // flush the output
        this.connectionOutput.flush();
    }

    /**
     * Get an HTTP response from the server
     * 
     * @return the HTTP response
     * @throws IOException
     * @throws HTTPParsingException if an error in HTTP parsing has occurred
     */
    private HTTPResponse getResponse() throws IOException, HTTPParsingException {
        var response = new HTTPResponse();
        // parse the start line
        response.parseStartLine(this.connectionInput.readLine());

        // read the headers
        var headers = new ArrayList<String>();
        var line = this.connectionInput.readLine();
        while (!line.contentEquals("")) {
            headers.add(line);
            line = this.connectionInput.readLine();
        }

        // parse the headers
        var headersArr = new String[headers.size()];
        headersArr = headers.toArray(headersArr);
        response.parseHeaders(headersArr);

        // if the contentLength header is prensent, then parse the body
        var contentLength = response.getHeaders().get("Content-Length");
        if (contentLength != null) {
            var bytesToRead = Integer.parseInt(contentLength);
            var buf = new char[bytesToRead];
            this.connectionInput.read(buf);
            response.parseBody(new String(buf));
        }
        System.out.println(response.getFormattedMessage());
        return response;
    }

    /**
     * Authenticate an HTTP request, by adding the Authorization header and the
     * correct parameter. After this method returns, if the user is actually
     * logged in, the request is authenticated;
     * 
     * @param request
     */
    private void authRequest(HTTPRequest request) {
        if (this.username != null && this.authToken != null) {
            request.setHeader("Authorization", "Basic " + this.username + ":" + this.authToken);
        }
    }

    /**
     * Get the error message from an error response.
     * Returns the HTTPerror code plus an eventual reason contained in the body (if
     * there is) of the response
     * 
     * @param response
     * @return a response err with the error message
     * @throws IOException
     */
    private Result<String, String> getErrorMessage(HTTPResponse response)
            throws IOException {
        var outStr = response.getResponseCode().toString();
        if (response.getBody() != null) {
            var errBody = this.mapper.readValue(response.getBody(), ErrorResponse.class);
            outStr += " " + errBody.reason;
        }
        return Result.err(outStr);
    }

    /**
     * Register user to Winsome
     * 
     * @param username
     * @param password
     * @param tags     a list of tags, the length must be less or equal to 5
     * @return
     */
    public Result<String, String> register(String username, String password, String[] tags) {
        if (username == null || password == null || tags == null) {
            throw new NullPointerException();
        }
        try {
            // onvoke the registration method on the remote registration object
            this.registrationObj.registerToWinsome(username, password, tags);
            return Result.ok("user registered");
        } catch (RemoteException e) {
            return Result.err("connection error");
        } catch (UserAlreadyExistsException e) {
            return Result.err("user already exists");
        }
    }

    /**
     * Login to the Winsome system.
     * All successive calls of authenticated methods are execute with this
     * authentication credentials.
     * 
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public Result<String, String> login(String username, String password) throws IOException {
        // if the user already logged in, then return an error
        if (this.username != null) {
            return Result.err("user already logged in");
        }

        try {
            // prepare the login request payload
            var reqBody = new LoginRequest();
            reqBody.username = username;
            reqBody.password = password;

            // send the request
            var request = new HTTPRequest(HTTPMethod.POST, "/login")
                    .setBody(this.mapper.writeValueAsString(reqBody));
            sendRequest(request);

            // get the response
            HTTPResponse response;
            try {
                response = getResponse();
            } catch (HTTPParsingException e) {
                return Result.err("bad HTTP response");
            }
            // if the response code is not OK then return the error
            if (response.getResponseCode() != HTTPResponseCode.OK) {
                return getErrorMessage(response);
            }

            // try to deserializa the response body
            var resBody = this.mapper.readValue(response.getBody(), LoginResponse.class);

            // set authentication credentials
            this.authToken = resBody.authToken;
            this.username = username;

            // set up RMI callback for followers
            try {
                // get the initial follower list
                var initialFollowers = this.callbackService.getFollowers(this.username, this.authToken);

                // initialize and export the callback object
                this.callbackObject = new FollowersCallbackImpl(initialFollowers);
                this.callbackStub = (FollowersCallback) UnicastRemoteObject.exportObject(this.callbackObject, 0);
                this.callbackService.registerForCallback(username, authToken, this.callbackStub);
            } catch (AuthenticationException e) {
                // this should not occurr since the user just authenticated
                e.printStackTrace();
            }
            return Result.ok("ok, auth:" + authToken);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.err("json processing");
        }
    }

    /**
     * Logout the user from Winsome
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> logout() throws IOException {
        if (this.username == null) {
            return Result.err("user must be logged in");
        }

        try {
            // unregister the callbackObject
            this.callbackService.unregisterForCallback(username, authToken);

            // unexport the callback Object and re initialize the callback fields
            UnicastRemoteObject.unexportObject(this.callbackObject, true);
            this.callbackObject = null;
            this.callbackStub = null;
        } catch (AuthenticationException e) {
            // this should not occurr since at this point the user is still logged in
            // and username/authToken are still valid
            e.printStackTrace();
        }

        // send a logout request
        var request = new HTTPRequest(HTTPMethod.DELETE, "/login");
        authRequest(request);
        sendRequest(request);

        // get the logout response
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }

        // if the response is OK, then the user successfully logged out

        // clear the auth fields
        this.username = null;
        this.authToken = null;
        return Result.ok("logged out");
    }

    /**
     * List users in winsome that have at least one tag in common
     * with the logged user
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> listUsers() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/users");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserializa a UserResponse array from the body
        var resBody = this.mapper.readValue(response.getBody(), UserResponse[].class);
        // return the rendered result of the deserialized users
        return Result.ok(PresentationUtils.renderUsernames(resBody));
    }

    /**
     * Start following a user
     * 
     * @param toFollowUsername
     * @return
     * @throws IOException
     */
    public Result<String, String> followUser(String toFollowUsername) throws IOException {
        var request = new HTTPRequest(HTTPMethod.PUT, "/followers/" + toFollowUsername);
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // if the response code is OK then follow operation is successful
        return Result.ok("user " + toFollowUsername + " followed");
    }

    /**
     * Stop following a user
     * 
     * @param toFollowUsername
     * @return
     * @throws IOException
     */
    public Result<String, String> unfollowUser(String toUnfollowUsername) throws IOException {
        var request = new HTTPRequest(HTTPMethod.DELETE, "/followers/" + toUnfollowUsername);
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // if the response code is OK then unfollow operation is successful
        return Result.ok("user " + toUnfollowUsername + " unfollowed");
    }

    /**
     * Get the list of users that the logged user follows
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> listFollowing() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/following");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body as a UserResponse array
        var resBody = this.mapper.readValue(response.getBody(), UserResponse[].class);
        // return the rendered user list
        return Result.ok(PresentationUtils.renderUsernames(resBody));
    }

    /**
     * Get the list of followers of the logged user.
     * This method does not send a request to the server, but uses the local
     * follower cache
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> listFollowers() {
        if (this.callbackObject == null) {
            return Result.err("not registered for callback");
        }
        // get the list of followers from the callbackObject
        var followers = this.callbackObject.getFollowers();
        // return the rendered user list
        return Result.ok(PresentationUtils.renderUsernames(followers));
    }

    /**
     * Create a post in the Winsome social media
     * 
     * @param title   the title of the post
     * @param content the content of the post
     * @return
     * @throws IOException
     */
    public Result<String, String> createPost(String title, String content) throws IOException {
        if (title == null || content == null) {
            throw new NullPointerException();
        }

        // prepare the post request body
        var reqBody = new PostRequest();
        reqBody.title = title;
        reqBody.content = content;

        // send the request
        var request = new HTTPRequest(HTTPMethod.POST, "/posts")
                .setBody(this.mapper.writeValueAsString(reqBody));
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.CREATED) {
            return getErrorMessage(response);
        }
        // try to deserialize the body of the response
        var resBody = this.mapper.readValue(response.getBody(), PostIdResponse.class);
        // return the formatted post Id
        return Result.ok("post created, id:" + Integer.toString(resBody.postId));
    }

    /**
     * Get a post from Winsome
     * 
     * @param idPost the post id
     * @return
     * @throws IOException
     */
    public Result<String, String> getPost(int idPost) throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/posts/" + Integer.toString(idPost));
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body
        var resBody = this.mapper.readValue(response.getBody(), PostResponse.class);
        // return the rendered post
        return Result.ok(PresentationUtils.renderPost(resBody));
    }

    /**
     * Rewin a post
     * 
     * @param postId the post to be rewinned
     * @return
     * @throws IOException
     */
    public Result<String, String> rewinPost(int postId) throws IOException {
        var request = new HTTPRequest(HTTPMethod.POST, "/posts/" + Integer.toString(postId) + "/rewins");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        return Result.ok("post rewinned");
    }

    /**
     * Rate a post
     * rate has to be 1 or -1
     * 
     * @param postId the post id to rate
     * @param rate   1 or -1
     * @return
     * @throws IllegalArgumentException if rate is not 1 nor -1
     * @throws IOException
     */
    public Result<String, String> ratePost(int postId, int rate) throws IOException {
        if (rate != 1 && rate != -1) {
            throw new IllegalArgumentException();
        }

        // prepare the request body
        var reqBody = new RateRequest();
        reqBody.rate = rate;

        // send the request
        var request = new HTTPRequest(HTTPMethod.POST, "/posts/" + Integer.toString(postId) + "/rates")
                .setBody(this.mapper.writeValueAsString(reqBody));
        authRequest(request);
        sendRequest(request);

        // get the response
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        return Result.ok("post rated");
    }

    /**
     * Add a comment to a post
     * 
     * @param postId  the post to add the comment
     * @param content the comment content
     * @return
     * @throws IOException
     */
    public Result<String, String> addComment(int postId, String content) throws IOException {
        // prepare the request body
        var reqBody = new CommentRequest();
        reqBody.content = content;

        // send the request
        var request = new HTTPRequest(HTTPMethod.POST, "/posts/" + Integer.toString(postId) + "/comments")
                .setBody(this.mapper.writeValueAsString(reqBody));
        authRequest(request);
        sendRequest(request);

        // get the response
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        return Result.ok("post commented");
    }

    /**
     * Get the logged user's blog
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> viewBlog() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/posts");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body
        var resBody = this.mapper.readValue(response.getBody(), PostResponse[].class);
        // return the rendered post list
        return Result.ok(PresentationUtils.renderPostFeed(resBody));
    }

    /**
     * Get the logged user's feed
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> viewFeed() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/feed");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body
        var resBody = this.mapper.readValue(response.getBody(), PostResponse[].class);
        // return the rendered post list
        return Result.ok(PresentationUtils.renderPostFeed(resBody));
    }

    /**
     * Delete a post
     * 
     * @param postId the post to be deleted
     * @return
     * @throws IOException
     */
    public Result<String, String> deletePost(int postId) throws IOException {
        var request = new HTTPRequest(HTTPMethod.DELETE, "/posts/" + Integer.toString(postId));
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        return Result.ok("post deleted");
    }

    /**
     * Get the logged user's wallet
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> getWallet() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/wallet");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body
        var resBody = this.mapper.readValue(response.getBody(), WalletResponse.class);
        // return the rendered wallet
        return Result.ok(PresentationUtils.renderWallet(resBody));
    }

    /**
     * Get the logged user's in BTC
     * 
     * @return
     * @throws IOException
     */
    public Result<String, String> getWalletInBtc() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "/wallet/btc");
        authRequest(request);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        // try to deserialize the response body
        var resBody = this.mapper.readValue(response.getBody(), WalletResponse.class);
        // return the rendered wallet
        return Result.ok(PresentationUtils.renderWallet(resBody));
    }
}
