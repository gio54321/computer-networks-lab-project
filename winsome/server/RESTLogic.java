package winsome.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import winsome.common.requests.LoginRequest;
import winsome.common.responses.ErrorResponse;
import winsome.common.responses.LoginResponse;
import winsome.lib.http.HTTPMethod;
import winsome.lib.http.HTTPResponse;
import winsome.lib.http.HTTPResponseCode;
import winsome.lib.router.DeserializeRequestBody;
import winsome.lib.router.Route;
import winsome.server.database.Database;
import winsome.server.database.exceptions.AuthenticationException;
import winsome.server.database.exceptions.UserAlreadyLoggedInException;
import winsome.server.database.exceptions.UserDoesNotExistsException;

public class RESTLogic {
    private Database database;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RESTLogic(Database database) {
        this.database = database;
    }

    @Route(method = HTTPMethod.POST, path = "/login")
    @DeserializeRequestBody(LoginRequest.class)
    public HTTPResponse login(LoginRequest body) {
        String token;
        try {
            token = this.database.loginUser(body.username, body.password);
        } catch (UserDoesNotExistsException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (UserAlreadyLoggedInException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "User is already logged in");
        } catch (AuthenticationException e) {
            return HTTPResponse.errorResponse(HTTPResponseCode.UNAUTHORIZED, "Invalid credentials");
        }
        return HTTPResponse.response(HTTPResponseCode.OK, new LoginResponse(token));
    }
}