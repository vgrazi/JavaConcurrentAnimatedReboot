package com.vgrazi.javaconcurrentanimated.study;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

public class ExecutorsStudy {
    private boolean stopped;

    @Test
    public void testPrestart() {
        ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        int threadCount = service.prestartAllCoreThreads();
        service.submit(() -> {
//    while (working) {
//        doSomeWork();
//    }
//    // done
        });
    }

    @Test
    public void saturationPolicy() throws InterruptedException {
        Executor executor = new ThreadPoolExecutor(0,2,2, TimeUnit.SECONDS, new SynchronousQueue<>());
//        Executor executor = Executors.newFixedThreadPool(2);
//        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
//        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
//        ((ThreadPoolExecutor) executor).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        BlockingQueue queue = new LinkedBlockingDeque();
        Collection list = new ArrayList();
        queue.drainTo(list);
        execute(executor);
        execute(executor);
        execute(executor);
        keepAlive();
    }

    private void execute(Executor executor) {
        executor.execute(()->{
            System.out.println("Launching " + Thread.currentThread());
            while(!stopped) {
                Thread.yield();
            }
            System.out.println("Exiting " + Thread.currentThread());
        });
    }

    @Test
    public void handler() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setRejectedExecutionHandler(executor.getRejectedExecutionHandler());
        for (int i = 0; i < 6; i++) {
            addRunnable(executor);
        }
        keepAlive();
    }

    @Test
    public void tryAgain() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.AbortPolicy());

        executor.execute(() -> waitFor(250));

        long startTime = System.currentTimeMillis();
        executor.execute(() -> waitFor(500));
        long blockedDuration = System.currentTimeMillis() - startTime;

//        assertThat(blockedDuration).isGreaterThanOrEqualTo(500);
    }

    private void waitFor(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    private void addRunnable(ExecutorService executorService) {
        System.out.println("Trying " + Thread.currentThread());
        executorService.execute(()->{
            System.out.println("Launching " + Thread.currentThread());
            while(!stopped) {
                Thread.yield();
            }
            System.out.println("Completing " + Thread.currentThread());
        });
    }
}
