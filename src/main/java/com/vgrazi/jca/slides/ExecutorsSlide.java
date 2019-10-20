package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.PooledThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class ExecutorsSlide extends Slide {
// todo: Pooled threads cap should not rotate
// todo: Create runnable sprite, to visualize a queued runnable
// todo: add a label above the pooled aread "Pooled Threads"
    @Autowired
    private ApplicationContext applicationContext;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    private ExecutorService executor;

    public void run() {
        reset();

        threadContext.addButton("prestartAllCoreThreads()", () -> ((ThreadPoolExecutor) executor).prestartAllCoreThreads());

        threadContext.addButton("submit", () -> {
            executor.submit(() -> {
                Thread thread = Thread.currentThread();
                PooledThreadSprite<String> sprite = (PooledThreadSprite) threadContext.getThreadSprite(thread);
                if (sprite != null) {
                    sprite.setPooled(false);
                    sprite.setRunning(true);
                    while (sprite.isRunning()) {
                        Thread.yield();
                    }
                    sprite.setPooled(true);
                } else {
                    System.out.printf("Thread %s not known to context%n", thread);
                }
            });
        });

        threadContext.addButton("(done)", () -> {
            PooledThreadSprite<String> sprite = threadContext.getRunningPooledThread();
            if (sprite != null) {
                sprite.setRunning(false);
                sprite.setPooled(true);
            }
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    protected void reset() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.reset();
        threadContext.setSlideLabel("Executors");
        executor = Executors.newFixedThreadPool(4, r -> {
            PooledThreadSprite<String> sprite = (PooledThreadSprite) applicationContext.getBean("pooledThreadSprite");
            Thread thread = new Thread(r);
            sprite.setThread(thread);
            sprite.setPooled(true);
            sprite.setRunning(false);
            threadContext.addSprite(sprite);
            return thread;
        });
    }
}
