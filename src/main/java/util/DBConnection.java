// src/main/java/util/DBConnection.java
package util;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Properties props = new Properties();
            InputStream is = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (is == null) {
                throw new RuntimeException("config.properties not found");
            }

            props.load(is);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username"); // ✅ FIXED
            String password = props.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            e.printStackTrace(); // IMPORTANT
            return null;
        }
    }
}
