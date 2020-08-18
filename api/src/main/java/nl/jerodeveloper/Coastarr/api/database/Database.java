package nl.jerodeveloper.coastarr.api.database;

import lombok.Data;
import nl.jerodeveloper.coastarr.api.objects.users.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.logging.Logger;

@Data
public class Database {

    private final String url;
    private final String database;
    private final String username;
    private final String password;
    private final String dialect;
    private final int port;
    private transient Connection connection;
    private transient Logger logger;
    private transient SessionFactory sessionFactory;

    public Database(String url, String database, String username, String password, String dialect, int port) {
        this.url = url;
        this.database = database;
        this.username = username;
        this.password = password;
        this.dialect = dialect;
        this.port = port;
    }

    public void connect() {
        this.logger = Logger.getLogger("database");

        Configuration configuration = new Configuration().configure();
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect." + getDialect());
        configuration.setProperty("hibernate.connection.url", String.format("%s:%s/%s", getUrl(), getPort(), getDatabase()));
        configuration.setProperty("hibernate.connection.username", getUsername());
        configuration.setProperty("hibernate.connection.password", getPassword());
        configuration.configure();

        configuration.addAnnotatedClass(User.class);

        this.sessionFactory = configuration.buildSessionFactory();
    }

}
