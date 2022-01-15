package com.example.my_first_app.runnables;


import android.util.Log;

import com.example.my_first_app.utils.StatusWatchman;
import com.example.my_first_app.utils.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class ClientRunnable implements Runnable {
    @Override
    public void run() {
        //**establishing connection
        Socket socket = new Socket();
        try {
            socket.setSoTimeout(5000);
            socket.connect(new InetSocketAddress(StatusWatchman.IP, StatusWatchman.PORT), 200);
        } catch(SocketTimeoutException e){
            Log.e("warning", "Connection timed out");
            StatusWatchman.reportConnectionFailed();
            return;
        } catch (IOException e) {
            Log.e("error", "Couldn't establish connection with server", e);
            StatusWatchman.reportConnectionFailed();
            return;
        }

        //**getting incoming stream
        DataInputStream incomingStream;
        try {
            //TODO Check that it's successful if not notify status watchman
            incomingStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            Log.e("error", "Couldn't fetch input stream", e);
            StatusWatchman.reportConnectionFailed();
            return;
        }


        try {
            socket.setSoTimeout(5000);
            if(incomingStream.readInt() != StatusWatchman.PORT){
                Log.e("error", "Server verification failed");
                StatusWatchman.reportConnectionFailed();
                return;
            }
            socket.setSoTimeout(0);
        } catch (IOException e) {
            Log.e("error", "Couldn't verify server", e);
            StatusWatchman.reportConnectionFailed();
            return;
        }



        //if everything passed nicely store everything in status watchman
        Log.i("info", "Connection successfully established");
        StatusWatchman.CLIENT_SOCKET = socket;
        StatusWatchman.INCOMING_STREAM = incomingStream;
        StatusWatchman.reportConnectionEstablished();


        //**Start main loop
        try{
            Utils.mainLoop(incomingStream);
        } catch (IOException e) {
            Log.e("error", "Couldn't read data from server", e);
            StatusWatchman.reportTerminateConnection();
            return;
        }

        Log.i("info", "Mirroring ended");
        StatusWatchman.reportConnectionStop();
    }
}

