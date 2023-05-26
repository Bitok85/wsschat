package socketserver.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@UtilityClass
public class ConnectionInit {

    private static Connection connection;
    private static final String DB_PROPERTIES = "database.properties";
    private static final Properties CONFIG = getConfig();


    static {
        try {
            Class.forName(CONFIG.getProperty("db.driver"));
            connection = DriverManager.getConnection(
                    CONFIG.getProperty("db.delivery_server"),
                    CONFIG.getProperty("db.delivery_server_username"),
                    CONFIG.getProperty("db.delivery_server_password")
            );
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Ошибка установления соединения с DB delivery_server", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }


    private static Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream in = ConnectionInit.class
                .getClassLoader()
                .getResourceAsStream(DB_PROPERTIES)) {
            properties.load(in);
        } catch (IOException e) {
            log.error("Config initialization error", e);
        }
        return properties;
    }

}
