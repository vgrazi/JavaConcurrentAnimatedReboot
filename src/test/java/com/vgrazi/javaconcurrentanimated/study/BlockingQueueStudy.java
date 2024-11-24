package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueStudy {
    @Test
    public void whatHappensToThreadWhenOfferFails() {
        BlockingQueue queue = new ArrayBlockingQueue(1);
        new Thread(()->{
            boolean xxx = queue.offer("xxx");
            System.out.println("Done. " + xxx + " 1. queue size:" + queue.size());
        }).start();
        new Thread(()->{
            boolean xxx = queue.offer("xxx");
            System.out.println("Done. " + xxx + " 2. queue size:" + queue.size());
        }).start();
    }

    @AfterEach
    public void keepAlive() throws InterruptedException {
        Thread.sleep(5_000);
    }
}
