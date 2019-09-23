package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

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
            Logging.logAndSleep(0, "Creating futures1 and 2");
            CompletableFuture<String> completableFuture1 = new CompletableFuture<>();
            CompletableFuture<String> completableFuture2 = new CompletableFuture<>();
            ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.schedule(()-> {
                completableFuture1.complete("value1");
                completableFuture2.complete("value2");
            }, 2, TimeUnit.SECONDS);
            CompletableFuture<Void> completableFuture = CompletableFuture.allOf(completableFuture1, completableFuture2);
            completableFuture.get();
            ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite1.setTargetState(ThreadSprite.TargetState.awaitAdvance);
            Logging.logAndSleep("Adding first await ", sprite1);
            addRunnable(completableFuture, sprite1);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void addRunnable(CompletableFuture completableFuture, ThreadSprite sprite) {
//        sprite.attachAndStartRunnable(() -> {
//            int phase = 0;
//            while (sprite.isRunning()) {
//                if (sprite.getTargetState() == ThreadSprite.TargetState.release) {
//                    threadContext.stopThread(sprite);
//                    break;
//                }
//                switch (sprite.getTargetState()) {
//                    case awaitAdvance:
//                        completableFuture.awaitAdvance(phase);
//                        sprite.setTargetState(ThreadSprite.TargetState.release);
//                        break;
//                    case arrive:
//                        phase = completableFuture.arrive();
//                        System.out.println("Phase:" + phase);
//                        sprite.setTargetState(ThreadSprite.TargetState.release);
//                        break;
//                    case default_state:
//                        Thread.yield();
//                        break;
//                }
//            }
//            System.out.println(sprite + " exiting");
//        });
        threadContext.addThread(sprite);
    }
}
