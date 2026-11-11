package com.stockapp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;

public class DBConnection {
    private static final Logger logger = AppLogger.getLogger(DBConnection.class);

    private static String URL;// = "jdbc:postgresql://localhost:5432/gradedb";
    private static String USERNAME ;//= "postgres";
    private static String PASSWORD;// = "hatier1009";
    private static String DRIVER;
    static {
        loadProperties();
    }
    //private static Connection connection;

    private DBConnection() {} // private constructor

    private static void loadProperties() {
        Properties props = new Properties();
        
        // Try to load from file system first (project root)
        try (InputStream input = new FileInputStream("db.properties")) {
            props.load(input);
            logger.info("Loaded database configuration from db.properties");
            
        } catch (IOException e) {
            // If file not found, try classpath (inside JAR)
            try (InputStream input = DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                
                if (input != null) {
                    props.load(input);
                    logger.info("Loaded database configuration from classpath");
                } else {
                    // Use default values
                    logger.warning("db.properties not found! Using default configuration.");
                    setDefaultProperties(props);
                }
                
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to load db.properties", ex);
                setDefaultProperties(props);
            }
        }
        
        // Extract properties
        URL = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/gradedb");
        USERNAME = props.getProperty("db.username", "postgres");
        PASSWORD = props.getProperty("db.password");
        if (PASSWORD == null || PASSWORD.isBlank()) {
            PASSWORD = System.getenv("DB_PASSWORD");
        }
        DRIVER = props.getProperty("db.driver", "org.postgresql.Driver");
        
        logger.info("Database URL: " + URL);
        logger.info("Database User: " + USERNAME);
    }
    
    private static void setDefaultProperties(Properties props) {
        props.setProperty("db.url", "jdbc:postgresql://localhost:5432/gradedb");
        props.setProperty("db.username", "postgres");
        props.setProperty("db.password", "");
        props.setProperty("db.driver", "org.postgresql.Driver");
    }
    public static Connection getConnection() throws SQLException {
            if (PASSWORD == null || PASSWORD.isBlank()) {
                throw new SQLException("Database password is not configured. Set db.password or DB_PASSWORD.");
            }
            try {
                Class.forName(DRIVER);
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                logger.info("Connection established.");
                return connection;
            } catch(ClassNotFoundException e) {
                logger.log(Level.SEVERE, "PostgreSQL driver not found", e);
                throw new SQLException("Driver not found.", e);
            }catch (SQLException e) {
                logger.log(Level.SEVERE, "Database connection failed", e);
                throw e;
            }
        }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.info("Connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to close connection", e);
        }
    }
}
