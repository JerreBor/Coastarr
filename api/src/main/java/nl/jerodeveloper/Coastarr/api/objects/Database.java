package nl.jerodeveloper.coastarr.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Database {

    String url, database, username, password, tablePrefix;
    int port;

}
