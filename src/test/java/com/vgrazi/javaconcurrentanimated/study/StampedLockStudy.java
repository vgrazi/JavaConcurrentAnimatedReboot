package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.StampedLock;

public class StampedLockStudy {
    private boolean stopped;

    enum Types {
        read, write
    }
    @Test
    public void testReadWriteLocking() throws InterruptedException {
        StampedLock lock = new StampedLock();
        spin(lock, 1, Types.read);
        spin(lock, 2, Types.read);
        Thread.sleep(100);
        spin(lock, 3, Types.write);
    }

    private void spin(StampedLock lock, int id, Types type) {
        new Thread(()->{
            System.out.printf("Thread %d acquiring %s lock.%n", id, type);
            long stamp = type==Types.read? lock.readLock():lock.writeLock();
            System.out.printf("Thread %d acquired %s lock. Stamp: %d%n", id, type, stamp);
            while(!stopped){
                try {
                    Thread.sleep(1000);
                    if (type == Types.read) {
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(id + " " + type + " exiting");
        }).start();
    }

    @AfterEach
    public void keepAlive() throws InterruptedException {
        Thread threadKeepAlive = new Thread(() -> {
            // THIS IS A KEEPALIVE THREAD
            Object mutex = new Object();
            synchronized (mutex) {
                try {
                    mutex.wait(5_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadKeepAlive.start();
        threadKeepAlive.join();
    }
}
