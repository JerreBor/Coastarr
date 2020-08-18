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
import java.util.Map;
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
        Map<String, String> parameters = request.getParameters();

        Session session = Coastarr.getSettingsUtil().getSettings().getDATABASE().getSessionFactory().openSession();

        if (parameters.containsKey("username")) {
            nl.jerodeveloper.coastarr.api.objects.users.User user = (nl.jerodeveloper.coastarr.api.objects.users.User)
                    session.createQuery("SELECT u FROM User u WHERE u.username=:name").setParameter("name", parameters.get("username")).uniqueResult();

            if (user == null) {
                return Response.builder()
                        .code(HttpURLConnection.HTTP_NOT_FOUND)
                        .json(new JsonError("Could not find user with that username", HttpURLConnection.HTTP_NOT_FOUND))
                        .build();
            }

            user.setHash(null); // Stop hash from being displayed

            return Response.builder()
                    .json(new UserGetResponse(user))
                    .build();
        } else if (parameters.containsKey("id")) {

            int id;

            try {
                id = Integer.parseInt(parameters.get("id"));
            } catch (Exception e) {
                return Response.builder()
                        .code(HttpURLConnection.HTTP_BAD_REQUEST)
                        .json(new JsonError("Please provide a number as id", HttpURLConnection.HTTP_BAD_REQUEST))
                        .build();
            }

            nl.jerodeveloper.coastarr.api.objects.users.User user = (nl.jerodeveloper.coastarr.api.objects.users.User)
                    session.createQuery("SELECT u FROM User u WHERE u.id=:id").setParameter("id", id).uniqueResult();

            if (user == null) {
                return Response.builder()
                        .code(HttpURLConnection.HTTP_NOT_FOUND)
                        .json(new JsonError("Could not find user with that id", HttpURLConnection.HTTP_NOT_FOUND))
                        .build();
            }

            user.setHash(null); // Stop hash from being displayed

            return Response.builder()
                    .json(new UserGetResponse(user))
                    .build();
        } else {
            return Response.builder()
                    .code(HttpURLConnection.HTTP_BAD_REQUEST)
                    .json(new JsonError("Please include a username or id parameter", HttpURLConnection.HTTP_BAD_REQUEST))
                    .build();
        }
    }

    private static class UserGetResponse {

        final nl.jerodeveloper.coastarr.api.objects.users.User USER;

        public UserGetResponse(nl.jerodeveloper.coastarr.api.objects.users.User user) {
            this.USER = user;
        }

    }

    private static class UserPostResponse extends JsonMessage {

        final nl.jerodeveloper.coastarr.api.objects.users.User USER;

        public UserPostResponse(nl.jerodeveloper.coastarr.api.objects.users.User user) {
            super("Successfully created user");
            this.USER = user;
        }

    }

}
