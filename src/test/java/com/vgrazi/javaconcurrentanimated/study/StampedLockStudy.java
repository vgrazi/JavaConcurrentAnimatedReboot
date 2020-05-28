package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.StampedLock;
import java.util.logging.Logger;

public class StampedLockStudy {
    Logger logger = Logger.getLogger("StampedLockStudy");
    private boolean stopped;

    enum Types {
        read, write
    }
    @Test
    public void testReadWriteLocking() throws InterruptedException {
        StampedLock lock = new StampedLock();
        spin(lock, 1, Types.read);
        spin(lock, 2, Types.read);
        Thread.sleep(500);
        spin(lock, 3, Types.write);
    }

    private void spin(StampedLock lock, int id, Types type) {
        new Thread(()->{
            logger.info(String.format("Thread %d acquiring %s lock.%n", id, type));
            long stamp = type==Types.read? lock.readLock():lock.writeLock();
            logger.info(String.format("Thread %d acquired %s lock. Stamp: %d%n", id, type, stamp));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            long releaseStamp = -stamp;
            long releaseStamp = stamp;
            unlock(lock, id, type, releaseStamp);
//            lock.unlock(releaseStamp);
//            logger.info(String.format("Thread %d released %s lock. Stamp: %d%n", id, type, releaseStamp));
        }).start();
    }

    private void unlock(StampedLock lock, int id, Types type, long stamp) {
        if (type == Types.read) {
            lock.unlockRead(stamp);
        } else {
            lock.unlockWrite(stamp);
        }
        logger.info(String.format("Thread %d released %s lock. Stamp: %d%n", id, type, stamp));
    }

    @AfterEach
    public void keepAlive() throws InterruptedException {
        Thread threadKeepAlive = new Thread(() -> {
            // THIS IS A KEEPALIVE THREAD
            Object mutex = new Object();
            synchronized (mutex) {
                try {
                    mutex.wait(3_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadKeepAlive.start();
        threadKeepAlive.join();
    }
}
