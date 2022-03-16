package com.vgrazi.javaconcurrentanimated.study;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CountdownLatchStudy {
    public static void main(String[] args) {
        AtomicBoolean success =new AtomicBoolean(true);
        CountDownLatch latch=new CountDownLatch(1);
        Thread thread1 = new Thread(()->{
            try {
                Thread.currentThread().interrupt();
                latch.await();
                success.set(false);

            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
    }
}
