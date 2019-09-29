package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class CompletableFutureSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    @Value("${completable-future-height}")
    private int completableFutureHeight;

    public static void main(String[] args) throws InterruptedException {
        new CompletableFutureSlide().run();
    }

    public void run() {
//        try {
//            threadContext.colorByThreadInstance();

        threadContext.addButton("Create thread", () -> {
            // we need to create a future, a thread to attach to it, and sprites for each of those
            FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
            ThreadSprite threadSprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            threadSprite.setHolder(true);
            Runnable runnable = () -> {
                while (((Boolean) threadSprite.getHolder())) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                threadContext.stopThread(threadSprite);
            };
            CompletableFuture future = CompletableFuture.runAsync(runnable);
            futureSprite.setFuture(future);
            futureSprite.setHeight(completableFutureHeight);
            threadContext.addSprite(futureSprite);
            threadSprite.attachAndStartRunnable(runnable);
            addRunnable(threadSprite);
        });

        threadContext.addButton("CompletableFuture - waitForAll", () -> {
            FutureSprite sprite = (FutureSprite) applicationContext.getBean("futureSprite");
            CompletableFuture future = CompletableFuture.allOf();
            sprite.setAction("completable-all");
            addRunnable(sprite);
        });

        threadContext.addButton("Done", () -> {
            ThreadSprite runningSprite = threadContext.getNextRunningThread();
            if (runningSprite != null) {
                runningSprite.setHolder(false);
                runningSprite.setAction("release");
            }
            log("Set release on ", runningSprite);
        });

        threadContext.addButton("get()", () -> {
            GetterThreadSprite sprite = (GetterThreadSprite) applicationContext.getBean("getterSprite");
            sprite.setAction("get");
            addRunnable(sprite);
        });

        threadContext.setVisible();

//            scheduledExecutor.schedule(()-> {
//                completableFuture1.complete("value1");
//                completableFuture2.complete("value2");
//            }, 2, TimeUnit.SECONDS);
//            log("getting allOf...");
//            CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFuture1, completableFuture2);
//            completableFuture.get();
        log("got allOf...");
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    private void addRunnable(Sprite sprite) {
        if (sprite instanceof ThreadSprite) {
            ThreadSprite threadSprite = (ThreadSprite) sprite;
            threadSprite.attachAndStartRunnable(() -> {
                try {
                    while (sprite.isRunning()) {
                        if ("release".equals(sprite.getAction())) {
                            threadContext.stopThread(threadSprite);
                            break;
                        }
                        switch (sprite.getAction()) {
                            case "running":
                                Thread.yield();
                                break;
                            case "get":
                                break;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(sprite + " exiting");
            });
            threadContext.addSprite(threadSprite);
        } else if (sprite instanceof FutureSprite) {
            switch (sprite.getAction()) {
                case "completable-all":
                    FutureSprite futureSprite = (FutureSprite) sprite;
                    threadContext.addSprite(futureSprite);
                    CompletableFuture.runAsync(() -> {
                        while (true) {

                        }
                    });
            }
        }
    }
}
