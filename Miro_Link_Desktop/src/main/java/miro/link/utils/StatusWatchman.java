package miro.link.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miro.link.utils.observer.Subject;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusWatchman extends Subject {
    //*config
    public static boolean DEBUG_MODE;
    public static String LOG_CONSOLE_TIME_FORMAT;
    private static String LOG_CONSOLE_TEXT;


    //*data
    private static int PORT;
    private static String IP;
    public static Thread SERVER_THREAD;
    public static ServerSocket SERVER_SOCKET;
    public static DataOutputStream SEND_STREAM;
    public static DataInputStream RECEIVE_STREAM;


    //*conditions
    public static boolean SERVER_SHOULD_STOP;
    private static boolean SERVER_RUNNING;
    private static boolean CLIENT_CONNECTED;



    static {
        initializeData();

        loadConfigFrom("defaultConfig");
    }

    public static void initializeData(){
        SERVER_THREAD = null;
        SERVER_SOCKET = null;
        SERVER_SHOULD_STOP = false;
        DEBUG_MODE = false;
        SEND_STREAM = null;
        RECEIVE_STREAM = null;
        IP = null;
        PORT = 0;
        CLIENT_CONNECTED = false;
        SERVER_RUNNING = false;
    }

    public static void loadConfigFrom(String configFile) {
        Properties props = new Properties();
        try (InputStream is = StatusWatchman.class.getResourceAsStream("/watchmanConfigs/" + configFile + ".properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DEBUG_MODE= Boolean.parseBoolean(props.getProperty("DEBUG_MODE"));
        LOG_CONSOLE_TIME_FORMAT = props.getProperty("LOG_CONSOLE_TIME_FORMAT");
        LOG_CONSOLE_TEXT = props.getProperty("LOG_CONSOLE_TEXT");
    }



    public static String getIP() {
        return IP;
    }

    public static int getPORT() {
        return PORT;
    }

    public static void updatePort(int port) {
        PORT = port;
        updateAll();
    }

    public static void updateIP(String ip) {
        IP = ip;
        updateAll();
    }


    public static boolean isClientConnected() {
        return CLIENT_CONNECTED;
    }

    public static boolean isServerRunning() {
        return SERVER_RUNNING;
    }


    public static void reportClientConnected(boolean clientConnected) {
        CLIENT_CONNECTED = clientConnected;
        updateAll();
    }

    public static void reportServerStop() {
        SERVER_RUNNING = false;
        PORT = 0;
        IP = null;
        log("Disconnected"); //log also calls updateAll()
        log.info("Server status updated: [status;stopped]");
    }

    public static void reportServerStart(int port, String ip) {
        SERVER_RUNNING = true;
        PORT = port;
        IP = ip;
        updateAll();
        log.info("Server status updated: [status;running], [address;" + ip + "], [port;" + port + "]");
    }


    public static String getLogConsoleText() {
        return LOG_CONSOLE_TEXT;
    }

    public static void log(String log) {
        LOG_CONSOLE_TEXT += (Utils.now() + log + "\n");
        updateAll();
    }


    //TODO dont let this pass to production
    //could create something like this for security but meh for now
    /*@NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Database{
        private static DataSource dataSource;
        public static DataSource getDataSource(){
            if (dataSource == null){

            }
        }

    }*/
}
