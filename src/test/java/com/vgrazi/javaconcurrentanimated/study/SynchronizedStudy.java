package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class SynchronizedStudy {
    private final Object MUTEX = new Object();
    private boolean stopped;

    private static Logger logger = Logger.getLogger("SynchronizedStudy");
    private static void println(Object message) {
        logger.info(String.valueOf(message));
    }

    /**
     * Demo for how thread.stop will release a blocked thread.
     * Calling stop() on the holding thread is the only way for a blocked thread to proactively get unblocked. And since the thread is blocked,
     * it can't do this directly
     * @throws InterruptedException
     */
    @Test
    public void testBlockedStop() throws InterruptedException {
        Thread thread1 = getThread();
        Thread thread2 = getThread();
        Thread thread3 = getThread();
//        Thread.holdsLock()

        println("Trying interrupt on thread 1");
        thread1.interrupt();
        Thread.sleep(1000);
        println("Trying stop on thread 2");
        thread2.stop();
        Thread.sleep(1000);

        println("Trying stop on thread 1");
        thread1.stop();
        Thread.sleep(100);

        println("Exhausted all attempts");
        keepAlive();
    }

    @Test
    public void testInterrupt() throws InterruptedException {
        Thread thread1 = getThread();
        Thread thread2 = getThread();
        thread1.interrupt();
        keepAlive();
    }

    private void keepAlive() throws InterruptedException {
        Thread threadKeepAlive = new Thread(() -> {
            // THIS IS A KEEPALIVE THREAD
            while (true) {
                Thread.yield();
            }
        });
        threadKeepAlive.start();
        threadKeepAlive.join();
    }

    private Thread getThread() throws InterruptedException {
        Thread thread = new Thread(() -> {
            println(Thread.currentThread() + " ACQUIRING LOCK");
            synchronized (MUTEX) {
                println(Thread.currentThread() + " HAS LOCK");
                while (!stopped && !Thread.currentThread().isInterrupted()) {
                    Thread.yield();
                }
                println(Thread.currentThread() + "Exiting");
            }
        });
        thread.start();
        Thread.sleep(100);
        return thread;
    }
}
