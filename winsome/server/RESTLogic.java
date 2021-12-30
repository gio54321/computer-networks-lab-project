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

    private HTTPResponse response(HTTPResponseCode code, Object body) {
        try {
            return new HTTPResponse(code)
                    .setBody(this.objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private HTTPResponse errorResponse(HTTPResponseCode code, String reason) {
        return response(code, ErrorResponse.from(reason));
    }

    @Route(method = HTTPMethod.POST, path = "/login")
    @DeserializeRequestBody(LoginRequest.class)
    public HTTPResponse login(LoginRequest body) {
        String token;
        try {
            token = this.database.loginUser(body.username, body.password);
        } catch (UserDoesNotExistsException e) {
            return errorResponse(HTTPResponseCode.UNAUTHORIZED, "User does not exists");
        } catch (UserAlreadyLoggedInException e) {
            return errorResponse(HTTPResponseCode.UNAUTHORIZED, "User is already logged in");
        } catch (AuthenticationException e) {
            return errorResponse(HTTPResponseCode.UNAUTHORIZED, "Invalid credentials");
        }
        return response(HTTPResponseCode.OK, new LoginResponse(token));
    }
}
