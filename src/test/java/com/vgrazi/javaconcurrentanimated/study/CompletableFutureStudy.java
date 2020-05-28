package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureStudy {

    private final Random random = new Random();

    @Test
    public void allOf() {
        int testCount = 1;
        CompletableFuture<String>[] cfs = new CompletableFuture[testCount];
        for (int i = 0; i < testCount; i++) {
            System.out.println("Creating CompletableFuture " + "CompletableFuture #" + (i+1));
            CompletableFuture<String> completableFuture = addCompletableFuture("CompletableFuture #" + (i+1), 5);
            cfs[i] = completableFuture;
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(cfs);
        allOf.join();
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void anyOf() {
        int testCount = 5;
        CompletableFuture<String>[] cfs = new CompletableFuture[testCount];
        for (int i = 0; i < testCount; i++) {
            System.out.println("Creating CompletableFuture " + "CompletableFuture #" + (i+1));
            CompletableFuture<String> completableFuture = addCompletableFuture("CompletableFuture #" + (i+1), 5);
            cfs[i] = completableFuture;
        }
        CompletableFuture<?> anyOf = CompletableFuture.anyOf(cfs);
        CompletableFuture.allOf(cfs).join();
        Object joinAny = anyOf.join();
        System.out.println("Joined!!!:" + joinAny);
        try {
            Object joinGet = anyOf.get();
            System.out.println("Joined!!!:" + joinGet);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void thenDo(){
        int testCount = 2;
        CompletableFuture<String>[] cfs = new CompletableFuture[testCount];
        for (int i = 0; i < testCount; i++) {
            System.out.println("Creating CompletableFuture " + "CompletableFuture #" + (i+1));
            CompletableFuture<String> completableFuture = addCompletableFuture("CompletableFuture #" + (i+1), 5);
            cfs[i] = completableFuture;
        }
        CompletableFuture<?> anyOf = CompletableFuture.anyOf(cfs);
        anyOf.thenRun(()-> System.out.println("I'm Running!!!!"));
        anyOf.thenApply(x -> {
            System.out.println("I'm Returning " + x + " " + x.getClass() + " !!!!");
            return x;
        });
        anyOf.thenAccept(x -> {
            System.out.println("I'm Accepting " + x + " " + x.getClass() + " !!!!");
        });
        Object joinAny = anyOf.join();
        System.out.println("Joined!!!:" + joinAny);
        try {
            Object joinGet = anyOf.get();
            System.out.println("Joined!!!:" + joinGet);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    private CompletableFuture<String> addCompletableFuture(String id, int timeSecs) {
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
            try {
                long sleepTime = (random.nextInt(timeSecs) + 1)*1000;
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Completing CompletableFuture " + id);
            return id;
        });
        return f1;
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
