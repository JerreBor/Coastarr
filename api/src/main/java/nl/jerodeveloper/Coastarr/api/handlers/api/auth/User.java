package nl.jerodeveloper.coastarr.api.handlers.api.auth;

import nl.jerodeveloper.coastarr.api.Coastarr;
import nl.jerodeveloper.coastarr.api.annotations.*;
import nl.jerodeveloper.coastarr.api.objects.JsonError;
import nl.jerodeveloper.coastarr.api.objects.JsonMessage;
import nl.jerodeveloper.coastarr.api.objects.users.Role;
import nl.jerodeveloper.coastarr.api.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Handler
public class User {

    @Handle(requestType = RequestType.POST, returnType = ReturnType.JSON, authorization = AuthorizationType.BEARER)
    public CompletableFuture<Response> post(Request request) {
        final String encodedUserPass = request.getParameters().get("userpass");
        final CompletableFuture<Response> ERROR_MESSAGE = CompletableFuture.completedFuture(Response.builder()
                .json(new JsonError("Please include a base64-encoded string in this format: username:password", HttpURLConnection.HTTP_BAD_REQUEST))
                .code(HttpURLConnection.HTTP_BAD_REQUEST)
                .build());

        if (encodedUserPass == null) {
            return ERROR_MESSAGE;
        }

        String userpass;

        try {
            userpass = new String(Base64.getDecoder().decode(encodedUserPass), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ERROR_MESSAGE;
        }

        if (userpass.split(":").length != 2) {
            return ERROR_MESSAGE;
        }

        String username = userpass.split(":")[0];
        String password = userpass.split(":")[1];

        PasswordUtil passwordUtil = new PasswordUtil();

        String salt = passwordUtil.getSalt().orElseThrow();

        CompletableFuture<Optional<String>> hashPassword = passwordUtil.hashPassword(password, salt);

        return hashPassword.thenApply(s -> {
            Session session = Coastarr.getSettingsUtil().getSettings().getDATABASE().getSessionFactory().openSession();

            Query<Object> query = session.createQuery("select 1 from User u where u.username = :username", Object.class);
            query.setParameter("username", username);

            if (query.uniqueResult() != null) {
                return Response.builder()
                        .code(HttpURLConnection.HTTP_CONFLICT)
                        .json(new JsonError("User with that username already exists", HttpURLConnection.HTTP_CONFLICT))
                        .build();
            }

            nl.jerodeveloper.coastarr.api.objects.users.User user = new nl.jerodeveloper.coastarr.api.objects.users.User();
            user.setUsername(username);
            user.setHash(s.orElseThrow());
            user.setUserRole(Role.USER);
            session.save(user);

            user.setHash(null); // Stop hash from being displayed

            return Response.builder().code(HttpURLConnection.HTTP_CREATED).json(new UserPostResponse(user)).build();
        }).exceptionally(throwable ->
                Response.builder()
                .code(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .json(new JsonError(throwable.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR))
                .build()
        );
    }

    @Handle(requestType = RequestType.GET, returnType = ReturnType.JSON)
    public Response get(Request request) {


        return Response.builder().json("").build();
    }

    private static class UserGetResponse {

        nl.jerodeveloper.coastarr.api.objects.users.User USER;

        public UserGetResponse(nl.jerodeveloper.coastarr.api.objects.users.User user) {

            this.USER = user;
        }

    }

    private static class UserPostResponse extends JsonMessage {

        nl.jerodeveloper.coastarr.api.objects.users.User USER;

        public UserPostResponse(nl.jerodeveloper.coastarr.api.objects.users.User user) {
            super("Succesfully created user");
            this.USER = user;
        }

    }

}
