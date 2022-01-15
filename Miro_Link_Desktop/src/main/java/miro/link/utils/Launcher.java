package miro.link.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miro.link.runnables.ServerRunnable;

import java.io.IOException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Launcher {

    public static void toggleServer() throws IOException {
        if (StatusWatchman.isServerRunning()) {
            Launcher.stop();
        } else {
            Launcher.launch();
        }
    }

    public static void launch() {
        log.info("Initiating server launch . . .");
        StatusWatchman.SERVER_SHOULD_STOP = false;
        try {
            log.trace("Creating thread");
            StatusWatchman.SERVER_THREAD = new Thread(new ServerRunnable());
            StatusWatchman.SERVER_THREAD.setDaemon(true);
            StatusWatchman.SERVER_THREAD.start();
            log.trace("Thread started");
        } catch (Exception e) {
            log.error("Server launch failed", e);
            StatusWatchman.log("ERROR: Server launch failed");
        }
    }

    public static void stop() throws IOException {
        log.info("Stopping server . . .");
        StatusWatchman.SERVER_SHOULD_STOP = true;
        StatusWatchman.SERVER_SOCKET.close();
    }
}