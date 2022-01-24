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

import winsome.common.requests.LoginRequest;
import winsome.common.requests.PostRequest;
import winsome.common.responses.ErrorResponse;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.PostIdResponse;
import winsome.common.responses.PostResponse;
import winsome.common.responses.UserResponse;
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

public class WinsomeConnection {
    private Registration registrationObj;
    private FollowersCallbackService callbackService;
    private FollowersCallbackImpl callbackObject;
    private Socket socket;
    private BufferedReader connectionInput;
    private BufferedWriter connectionOutput;
    private ObjectMapper mapper = new ObjectMapper();

    private String username = null;
    private String authToken = null;

    public WinsomeConnection(InetAddress serverAddress, int port) throws NotBoundException, IOException {
        if (serverAddress == null) {
            throw new NullPointerException();
        }
        this.socket = new Socket(serverAddress, port);
        // the input and output streams are encoded in US_ASCII
        this.connectionInput = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        this.connectionOutput = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

        // get the registration handler from registryk
        // TODO host of registry
        // TODO port config
        var registryPort = 1235;
        var registry = LocateRegistry.getRegistry(registryPort);
        this.registrationObj = (Registration) registry.lookup("Registration-service");

        // set up followers callback
        this.callbackService = (FollowersCallbackService) registry.lookup("FollowersCallback-service");
        this.callbackObject = null;
    }

    public void closeConnection() throws IOException {
        this.socket.close();
    }

    private void sendRequest(HTTPRequest request) throws IOException {
        var formattedRequest = request.getFormattedMessage();
        System.out.println("sending " + formattedRequest);
        this.connectionOutput.write(formattedRequest);
        this.connectionOutput.flush();
    }

    private HTTPResponse getResponse() throws IOException, HTTPParsingException {
        var response = new HTTPResponse();

        response.parseStartLine(this.connectionInput.readLine());

        var headers = new ArrayList<String>();
        var line = this.connectionInput.readLine();
        while (!line.contentEquals("")) {
            headers.add(line);
            line = this.connectionInput.readLine();
        }
        var headersArr = new String[headers.size()];
        headersArr = headers.toArray(headersArr);
        response.parseHeaders(headersArr);

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

    private void authRequest(HTTPRequest request) {
        if (this.username != null && this.authToken != null) {
            request.setHeader("Authorization", "Basic " + this.username + ":" + this.authToken);
        }
    }

    private Result<String, String> getErrorMessage(HTTPResponse response)
            throws IOException {
        var outStr = response.getResponseCode().toString();
        if (response.getBody() != null) {
            var errBody = this.mapper.readValue(response.getBody(), ErrorResponse.class);
            outStr += " " + errBody.reason;
        }
        return Result.err(outStr);
    }

    public Result<String, String> register(String username, String password, String[] tags) {
        try {
            this.registrationObj.registerToWinsome(username, password, tags);
            return Result.ok("user registered");
        } catch (RemoteException e) {
            return Result.err("connection error");
        } catch (UserAlreadyExistsException e) {
            return Result.err("user already exists");
        }
    }

    public Result<String, String> login(String username, String password) throws IOException {
        if (this.username != null) {
            return Result.err("user already logged in");
        }
        try {
            var reqBody = new LoginRequest();
            reqBody.username = username;
            reqBody.password = password;
            var request = new HTTPRequest(HTTPMethod.POST, "/login")
                    .setBody(this.mapper.writeValueAsString(reqBody));
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
            var resBody = this.mapper.readValue(response.getBody(), LoginResponse.class);
            this.username = username;
            this.authToken = resBody.authToken;

            // set up RMI callback for followers
            try {
                var initialFollowers = this.callbackService.getFollowers(this.username, this.authToken);
                this.callbackObject = new FollowersCallbackImpl(initialFollowers);
                var callbackStub = (FollowersCallback) UnicastRemoteObject.exportObject(this.callbackObject, 0);
                this.callbackService.registerForCallback(username, authToken, callbackStub);

            } catch (AuthenticationException e) {
                // TODO this should not occurr
                e.printStackTrace();
            }

            return Result.ok("ok, auth:" + authToken);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.err("json processing");
        }
    }

    public Result<String, String> logout() throws IOException {
        if (this.username == null) {
            return Result.err("user must be logged in");
        }

        // unregister callback
        try {
            this.callbackService.unregisterForCallback(username, authToken);
            this.callbackObject = null;
        } catch (AuthenticationException e) {
            // TODO this should not occurr
            e.printStackTrace();
        }

        var request = new HTTPRequest(HTTPMethod.DELETE, "/login");
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

        this.username = null;
        this.authToken = null;
        return Result.ok("logged out");
    }

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
        var resBody = this.mapper.readValue(response.getBody(), UserResponse[].class);
        return Result.ok(PresentationUtils.renderUsernames(resBody));
    }

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
        return Result.ok("user " + toFollowUsername + " followed");
    }

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
        return Result.ok("user " + toUnfollowUsername + " unfollowed");
    }

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
        var resBody = this.mapper.readValue(response.getBody(), UserResponse[].class);
        return Result.ok(PresentationUtils.renderUsernames(resBody));
    }

    public Result<String, String> listFollowers() {
        if (this.callbackObject == null) {
            return Result.err("not registered for callback");
        }
        var followers = this.callbackObject.getFollowers();
        return Result.ok(PresentationUtils.renderUsernames(followers));
    }

    public Result<String, String> createPost(String title, String content) throws IOException {
        var reqBody = new PostRequest();
        reqBody.title = title;
        reqBody.content = content;
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
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            return getErrorMessage(response);
        }
        var resBody = this.mapper.readValue(response.getBody(), PostIdResponse.class);
        return Result.ok("post created, id:" + Integer.toString(resBody.postId));
    }

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
        var resBody = this.mapper.readValue(response.getBody(), PostResponse.class);
        return Result.ok(PresentationUtils.renderPost(resBody));
    }
}
