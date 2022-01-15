package miro.link.runnables;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import miro.link.utils.Compressor;
import miro.link.utils.StatusWatchman;
import miro.link.utils.Utils;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;


@Slf4j
public class ServerRunnable implements Runnable {

    @Override
    public void run() {
        Socket accept = null;

        StatusWatchman.log("Launching server . . .");

        //** Connecting to server;
        try {
            Utils.createServerSocketOnFreePortAndNotifyStatusWatchmanAboutIt();
        } catch (Exception e) {
            log.error("Server startup failed", e);
            StatusWatchman.log("ERROR: Couldn't startup server!");
            StatusWatchman.log("Disconnected");
            return;
        }

        StatusWatchman.log("Server Launched");
        StatusWatchman.log("Waiting for connection . . .");

        //** Waiting for connection
        try {
            accept = Utils.establishConnectionWithClient();
        } catch (IOException e) {
            if (StatusWatchman.SERVER_SHOULD_STOP) { //if server should stop then user initiated stopping so just warning
                log.warn("Wait for connection terminated");
                StatusWatchman.log("Warning: Wait for connection stopped");
            } else {
                log.error("Connection terminated due to unknown reason", e);
                StatusWatchman.log("ERROR: Connection terminated due to unknown reason");
            }
            StatusWatchman.reportServerStop();
            return;
        }

        StatusWatchman.log("Client detected");
        StatusWatchman.log("Establishing data-link with client . . .");

        //** Establishing data link (AKA getting output stream)
        try {
            Utils.getOutputStreamToClientAndStoreItInStatusWatchman(accept);
        } catch (Exception e) {
            if (!StatusWatchman.SERVER_SHOULD_STOP) {
                log.error("Couldn't establish data-link with client", e);
                StatusWatchman.log("ERROR: Couldn't data-link with client");
            } else {
                log.warn("Establish data-link with client stopped by user", e);
                StatusWatchman.log("Warning: stopped establishing data-link");
            }

            StatusWatchman.reportServerStop();
            return;
        }

        StatusWatchman.log("Data-link established");
        StatusWatchman.log("Mirroring initiated");

        //*** Starting main loop
        try {
            Utils.startMainLoopAccordingToDebugModeDeclaredInStatusWatchman();
        } catch (AWTException e) {
            log.error("Platform configuration doesn't grant permission on low level input control", e);
            StatusWatchman.log("ERROR: Platform configuration doesn't grant permission on low level input control!");
        } catch (IOException e) {
            log.error("Couldn't send data to client", e);
            StatusWatchman.log("ERROR: Couldn't send data to client!");
        } catch (Exception e) {
            log.error("Unknown exception occurred", e);
            StatusWatchman.log("ERROR: Unknown exception occurred!");
        }

        log.info("Mirroring stopped");
        log.info("Closing thread");
        StatusWatchman.log("Mirroring stopped");
        StatusWatchman.reportServerStop();
    }

}

