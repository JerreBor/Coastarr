package nl.jerodeveloper.coastarr.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.jerodeveloper.coastarr.api.database.Database;

@Data
@AllArgsConstructor
public class Settings {

    private String TMDB_TOKEN;
    private String TMDB_WEB;
    private String AUTH_SECRET;
    private String PASS_SECRET;
    private int PASS_ITERATIONS;
    private int TOKEN_EXPIRATION;
    private Database DATABASE;

}
