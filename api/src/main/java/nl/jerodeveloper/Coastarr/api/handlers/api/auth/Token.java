package nl.jerodeveloper.coastarr.api.handlers.api.auth;

import com.google.common.io.BaseEncoding;
import io.jsonwebtoken.io.Encoders;
import nl.jerodeveloper.coastarr.api.Coastarr;
import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.users.User;
import nl.jerodeveloper.coastarr.api.util.AuthenticationUtil;
import nl.jerodeveloper.coastarr.api.objects.JsonMessage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Handler
public class Token {

    private final AuthenticationUtil authenticationUtil;

    public Token() {
        this.authenticationUtil = new AuthenticationUtil();
    }

    @Handle(requestType = RequestType.POST, returnType = ReturnType.JSON, authorization = AuthorizationType.BASIC)
    public Response post(Request request) {
        return Response.builder().json(new TokenResponse(request.getUser())).build();
    }

    private class TokenResponse extends JsonMessage {

        private final String TOKEN;
        private final long EXPIRES_IN;
        private final String REFRESH_TOKEN;

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
