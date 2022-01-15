package miro.link.runnables;

import lombok.extern.slf4j.Slf4j;
import miro.link.db.DatabaseUpdater;
import miro.link.utils.StatusWatchman;

import java.util.Date;

import static miro.link.db.repositories.MiroLinkUsageRepository.incrementUserTotalUsageTimeBy;
import static miro.link.db.repositories.MiroLinkUsageRepository.incrementUserUsagesBy;

@Slf4j
public class DatabaseUpdaterRunnable implements Runnable{
    private static final Date clock = new Date();


    @Override
    public void run() {
        incrementUserUsagesBy(1);
        while(StatusWatchman.isServerRunning() && DatabaseUpdater.DATABASE_UPDATER_RUNNING){
            long updateStartTime = clock.getTime()/1000;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.warn("Database updater was interrupted", e);
            }

            incrementUserTotalUsageTimeBy(clock.getTime()/1000 - updateStartTime);
        }
    }
}
