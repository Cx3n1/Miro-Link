package miro.link.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miro.link.runnables.DatabaseUpdaterRunnable;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseUpdater {

    public static Thread UPDATER_THREAD;
    public static boolean DATABASE_UPDATER_RUNNING;

    static {
        DATABASE_UPDATER_RUNNING = false;
    }

    public static boolean startUpdater() {
        log.warn("Starting database updater . . .");

        if (!DatabaseManager.isDatabaseInitialised()) {
            log.warn("Failed to start updater");
            return false;
        }

        DATABASE_UPDATER_RUNNING = true;

        UPDATER_THREAD = new Thread(new DatabaseUpdaterRunnable());
        UPDATER_THREAD.setDaemon(true);
        UPDATER_THREAD.start();

        log.warn("Database updater started");

        return true;
    }

    public static void stopUpdater(){
        DATABASE_UPDATER_RUNNING = false;
        UPDATER_THREAD = null;
    }

}
