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
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.requests.LoginRequest;
import winsome.common.responses.ErrorResponse;
import winsome.common.responses.LoginResponse;
import winsome.common.responses.UserResponse;
import winsome.common.rmi.Registration;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPParsingException;
import winsome.lib.http.HTTPRequest;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.utils.Result;
import winsome.server.database.exceptions.UserAlreadyExistsException;

public class WinsomeConnection {
    private Registration registrationObj;
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

        return response;
    }

    private String renderUsernames(UserResponse[] users) {
        // username column has to be at least 6 chars wide
        int maxUsernameLength = 6;
        for (var u : users) {
            if (u.username.length() > maxUsernameLength) {
                maxUsernameLength = u.username.length();
            }
        }

        var outStr = "";
        // render the header
        outStr += "Utente";
        for (int i = 6; i <= maxUsernameLength; ++i) {
            outStr += " ";
        }
        outStr += "| Tags\n";

        for (int i = 0; i <= maxUsernameLength + 10; ++i) {
            outStr += "-";
        }
        outStr += "\n";

        for (var u : users) {
            outStr += u.username;
            for (int i = u.username.length(); i <= maxUsernameLength; ++i) {
                outStr += " ";
            }
            outStr += "| ";
            for (var t : u.tags) {
                outStr += t + " ";
            }
            outStr += "\n";
        }
        return outStr;
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
                var errBody = this.mapper.readValue(response.getBody(), ErrorResponse.class);
                return Result.err(errBody.reason);
            }
            var resBody = this.mapper.readValue(response.getBody(), LoginResponse.class);
            this.username = username;
            this.authToken = resBody.authToken;
            return Result.ok("ok, auth:" + authToken);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.err("json processing");
        }
    }

    public Result<String, String> listUsers() throws IOException {
        if (this.username == null) {
            return Result.err("user must be logged in");
        }

        var request = new HTTPRequest(HTTPMethod.GET, "/users");
        request.setHeader("Authorization", "Basic " + this.username + ":" + this.authToken);
        sendRequest(request);
        HTTPResponse response;
        try {
            response = getResponse();
        } catch (HTTPParsingException e) {
            return Result.err("bad HTTP response");
        }
        if (response.getResponseCode() != HTTPResponseCode.OK) {
            var errBody = this.mapper.readValue(response.getBody(), ErrorResponse.class);
            return Result.err(errBody.reason);
        }
        var resBody = this.mapper.readValue(response.getBody(), UserResponse[].class);
        return Result.ok(renderUsernames(resBody));
    }
}
