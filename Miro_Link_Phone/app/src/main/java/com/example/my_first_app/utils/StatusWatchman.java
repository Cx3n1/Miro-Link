package com.example.my_first_app.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.my_first_app.utils.interfaces.Listener;
import com.example.my_first_app.utils.interfaces.Subject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusWatchman extends Subject {

    //device info
    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;
    public static int DEVICE_NAVBAR_HEIGHT;

    //connection info
    public static Thread RECEIVER_THREAD;
    public static String IP;
    public static int PORT;
    private static boolean TERMINATE_CONNECTION;
    private static boolean CONNECTION_ESTABLISHED;

    //mirroring info
    public static boolean MIRRORING_ACTIVITY_LAUNCHED;
    public static Socket CLIENT_SOCKET;
    public static DataInputStream INCOMING_STREAM;
    private static Bitmap CURRENT_MIRRORED_IMAGE;


    //initialization
    static{
        TERMINATE_CONNECTION = false;
        CONNECTION_ESTABLISHED = false;
        MIRRORING_ACTIVITY_LAUNCHED = false;
    }


    public static Bitmap getCurrentMirroredImage() {
        return CURRENT_MIRRORED_IMAGE;
    }


    //**Reports**\\
    /* reports are methods which update fields of status
     * watchman and notify listeners about these updates
     */

    /**
     * should be called when currentMirroredImage is updated,
     * used to communicate with mirroring display
     *
     * updates CURRENT_MIRRORED_IMAGE
     * @param currentMirroredImage - new image
     */
    public static void reportMirroredImageUpdate(Bitmap currentMirroredImage) {
        //no Logs for performance
        CURRENT_MIRRORED_IMAGE = currentMirroredImage;
        updateAll();
    }

    /**
     * should be called when connection with server couldn't be established
     * reason could be invalid port, ip or just inability to get dataInputStream
     */
    public static void reportConnectionFailed(){
        Log.w("report: warning", "Connection failure reported");
        CONNECTION_ESTABLISHED = false;
        TERMINATE_CONNECTION = true;
        updateAll();
        TERMINATE_CONNECTION = false;
    }

    /**
     * should be called when connection with server is successfully established
     * and data interchange is possible (through dataStream)
     */
    public static void reportConnectionEstablished() {
        Log.i("report: info", "Connection establishment reported");
        CONNECTION_ESTABLISHED = true;
        updateAll();
    }

    //these two are currently almost identical but that could change...
    /**
     * should be called when error occurred and connection needs to be reported as terminated
     */
    public static void reportTerminateConnection(){
        Log.w("report: info", "Connection termination reported");
        TERMINATE_CONNECTION = true;
        CONNECTION_ESTABLISHED = false;
        RECEIVER_THREAD = null;
        IP = null;
        PORT = 0;

        try {
            CLIENT_SOCKET.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        updateAll();

        TERMINATE_CONNECTION = false;
    }

    /**
     * should be called when connection is stopped naturally,
     * e.g. if user stopped it by pressing back on navbar
     */
    public static void reportConnectionStop(){
        Log.w("report: info", "Connection stop reported");
        TERMINATE_CONNECTION = true;
        CONNECTION_ESTABLISHED = false;
        RECEIVER_THREAD = null;
        IP = null;
        PORT = 0;

        try {
            CLIENT_SOCKET.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        updateAll();

        TERMINATE_CONNECTION = true;
    }

    /**
     * currently this does absolutely nothing
     */
    public static void reportReceiverThreadStart() {
        Log.i("report: info", "Thread start reported");
        TERMINATE_CONNECTION = false;
        updateAll();
    }


    //**Getters**\\
    public static boolean shouldConnectionBeTerminated() {
        return TERMINATE_CONNECTION;
    }

    public static boolean isConnectionEstablished() {
        return CONNECTION_ESTABLISHED;
    }



}
