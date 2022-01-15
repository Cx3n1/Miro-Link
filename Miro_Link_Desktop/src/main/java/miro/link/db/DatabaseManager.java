package miro.link.db;

import lombok.extern.slf4j.Slf4j;
import miro.link.db.repositories.MiroLinkUsageRepository;
import miro.link.utils.StatusWatchman;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class DatabaseManager {

    //*queries
    private static String QUERY_CREATE_MIRO_LINK_USAGE_TABLE;
    private static String QUERY_INSERT_INTO_MLU_TABLE;
    private static String QUERY_UPDATE_MLU_BY_ID;
    private static String QUERY_SELECT_ALL;
    private static String QUERY_USER_USAGE;
    private static String QUERY_FIND_USER;
    //*data
    private static int USER_ID;
    private static DataSource DATA_SOURCE;

    //*database config
    private static String DATABASE_CONNECTION_URI;
    private static String DATABASE_USERNAME;
    private static String DATABASE_PASSWORD;

    //*conditions
    private static boolean DATABASE_INITIALISED;


    static {
        DATABASE_INITIALISED = initialize();
        MiroLinkUsageRepository.initialise();
    }


    /**
     * initializes all properties of the class
     * @return true - if successfully false - otherwise
     */
    private static boolean initialize() {
        USER_ID = 0;

        if (
                !(loadQueriesFromConfig() &&
                loadDatabaseInfoConfig() &&
                initialiseDataSource())
        ) {
            log.warn("Couldn't initialise database");
            return false;
        }

        return true;
    }

    /**
     * loads queries from appropriate config file
     * @return true - if successfully false - otherwise
     */
    private static boolean loadQueriesFromConfig() {
        Properties props = new Properties();
        try (InputStream is = StatusWatchman.class.getResourceAsStream("/databaseConfigs/databaseQueries.properties")) {
            props.load(is);
        } catch (IOException e) {
            log.warn("Couldn't load queries");
            return false;
        }

        QUERY_SELECT_ALL = props.getProperty("QUERY_SELECT_ALL");
        QUERY_USER_USAGE = props.getProperty("QUERY_USER_USAGE");
        QUERY_FIND_USER = props.getProperty("QUERY_FIND_USER");
        QUERY_CREATE_MIRO_LINK_USAGE_TABLE = props.getProperty("QUERY_CREATE_MIRO_LINK_USAGE_TABLE");
        QUERY_INSERT_INTO_MLU_TABLE = props.getProperty("QUERY_INSERT_INTO_MLU_TABLE");
        QUERY_UPDATE_MLU_BY_ID = props.getProperty("QUERY_UPDATE_MLU_BY_ID");
        return true;
    }

    /**
     * loads appropriate properties from config file
     * @return true - if successfully false - otherwise
     */
    private static boolean loadDatabaseInfoConfig() {
        Properties props = new Properties();
        try (InputStream is = StatusWatchman.class.getResourceAsStream("/databaseConfigs/databaseConfig.properties")) {
            props.load(is);
        } catch (IOException e) {
            log.warn("Couldn't load database config", e);
            return false;
        }

        USER_ID = Integer.parseInt(props.getProperty("USER_ID"));
        DATABASE_CONNECTION_URI = props.getProperty("DATABASE_CONNECTION_URI");
        DATABASE_USERNAME = props.getProperty("DATABASE_USERNAME");
        DATABASE_PASSWORD = props.getProperty("DATABASE_PASSWORD");
        return true;
    }

    /**
     * creates DataSource in order to connect to database
     * @return true - if successfully false - otherwise
     */
    public static boolean initialiseDataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();

        if (DATABASE_CONNECTION_URI == null || DATABASE_USERNAME == null || DATABASE_PASSWORD == null) {
            log.warn("Couldn't fetch data");
            return false;
        }

        jdbcDataSource.setURL(DATABASE_CONNECTION_URI);
        jdbcDataSource.setUser(DATABASE_USERNAME);
        jdbcDataSource.setPassword(DATABASE_PASSWORD);

        DATA_SOURCE = jdbcDataSource;

        //try to establish connection with database
        try {
            DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            log.warn("Couldn't establish connection with database", e);
            return false;
        }

        return true;
    }


    public static Connection getConnection() throws SQLException {
        if (isDatabaseInitialised()) {
            return DATA_SOURCE.getConnection();
        }
        return null;
    }


    public static int getUserId(){
        return USER_ID;
    }

    public static String getQueryCreateMiroLinkUsageTable(){
        return QUERY_CREATE_MIRO_LINK_USAGE_TABLE;
    }

    public static String getQueryInsertIntoMluTable(){
        return QUERY_INSERT_INTO_MLU_TABLE;
    }

    public static String getQueryUpdateMluById() {
        return QUERY_UPDATE_MLU_BY_ID;
    }

    public static String getQuerySelectAll(){
        return QUERY_SELECT_ALL;
    }

    public static String getQueryUserUsage(){
        return QUERY_USER_USAGE;
    }

    public static String getQueryFindUser(){
        return QUERY_FIND_USER;
    }


    public static boolean isDatabaseInitialised(){
        return DATABASE_INITIALISED;
    }

}
