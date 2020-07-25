package nl.jerodeveloper.coastarr.api.handlers.api.auth;

import com.google.common.io.BaseEncoding;
import io.jsonwebtoken.io.Encoders;
import nl.jerodeveloper.coastarr.api.Coastarr;
import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.users.Group;
import nl.jerodeveloper.coastarr.api.objects.users.User;
import nl.jerodeveloper.coastarr.api.util.AuthenticationUtil;
import nl.jerodeveloper.coastarr.api.util.JsonMessage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

@Handler
public class Token {

    private final AuthenticationUtil authenticationUtil;

    public Token() {
        this.authenticationUtil = new AuthenticationUtil();
    }

    @Handle(requestType = RequestType.POST, returnType = ReturnType.JSON, authorization = AuthorizationType.BASIC)
    public Response post(Request request) {
        List<String> authHeader = request.getHeaders().get("Authorization");

        String encodedUserPass = authHeader.get(0).substring("Basic ".length());
        String userpass = new String(BaseEncoding.base64().decode(encodedUserPass), StandardCharsets.UTF_8);

        String username = userpass.split(":")[0];
        String password = userpass.split(":")[1];

        // TODO check username/password, send 401 if unauthorized

/*
        if (false) {
            return Response.builder()
                    .code(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .json(new JsonMessage("Invalid username and/or password"))
                    .build();
        }
*/

        return Response.builder().json(new TokenResponse(new User(0, username, password, Group.USER))).build();
    }

    private class TokenResponse extends JsonMessage {

        private String TOKEN;
        private long EXPIRES_IN;
        private String REFRESH_TOKEN;

        public TokenResponse(User user) {
            super("Succesfully authenticated!");
            this.TOKEN = authenticationUtil.generateToken(user);
            this.EXPIRES_IN = Coastarr.getSettingsUtil().getSettings().getTOKEN_EXPIRATION();
            SecureRandom secureRandom = new SecureRandom();
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);

            this.REFRESH_TOKEN = Encoders.BASE64.encode(bytes);
        }


    }

}
