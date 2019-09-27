package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class CompletableFutureSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new CompletableFutureSlide().run();
    }

    public void run() throws InterruptedException {
        try {
            threadContext.setVisible();

            CompletableFuture<String> completableFuture1 = new CompletableFuture<>();
            CompletableFuture<String> completableFuture2 = new CompletableFuture<>();
            ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

            scheduledExecutor.schedule(()-> {
                completableFuture1.complete("value1");
                completableFuture2.complete("value2");
            }, 2, TimeUnit.SECONDS);
            log("getting allOf...");
            CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFuture1, completableFuture2);
            completableFuture.get();
            log("got allOf...");
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void addRunnable(CompletableFuture completableFuture, ThreadSprite sprite) {
        sprite.attachAndStartRunnable(() -> {
            int phase = 0;
            while (sprite.isRunning()) {
                if ("release".equals(sprite.getAction())) {
                    threadContext.stopThread(sprite);
                    break;
                }
                switch (sprite.getAction()) {
                }
            }
            System.out.println(sprite + " exiting");
        });
        threadContext.addThread(sprite);
    }
}
