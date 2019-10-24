package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.PooledThreadSprite;
import com.vgrazi.jca.sprites.RunnableSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class ExecutorsSlide extends Slide {
    @Autowired
    private ApplicationContext applicationContext;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    private ExecutorService executor;

    public void run() {
        reset();

        threadContext.addButton("prestartAllCoreThreads()", () -> ((ThreadPoolExecutor) executor).prestartAllCoreThreads());

        threadContext.addButton("submit", () -> {
            RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
            threadContext.addSprite(runnableSprite);

            executor.submit(() -> {
                Thread thread = Thread.currentThread();
                PooledThreadSprite<String> sprite = (PooledThreadSprite) threadContext.getThreadSprite(thread);
                if (sprite != null) {
                    sprite.setPooled(false);
                    sprite.setRunning(true);
                    sprite.setYPosition(runnableSprite.getYPosition());
                    runnableSprite.setThread(thread);
                    while (sprite.isRunning()) {
                        Thread.yield();
                    }
                    sprite.setPooled(true);
                    runnableSprite.setDone();
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

    public void reset() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.reset();
        threadContext.setSlideLabel("Executors");
        threadContext.setBottomLabel("Pooled\nThreads");
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
