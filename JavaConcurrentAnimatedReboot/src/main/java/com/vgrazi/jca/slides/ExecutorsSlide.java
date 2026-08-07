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

        threadContext.addButton("execute", () -> {
            RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
            threadContext.addSprite(runnableSprite);
            highlightSnippet(2);

            executor.execute(() -> {
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
                    highlightSnippet(5);
                    sprite.setPooled(true);
                    runnableSprite.setDone();
                    threadContext.stopThread(runnableSprite);
                } else {
                    printf("Thread %s not known to context%n", thread);
                }
            });
        });

        threadContext.addButton("submit", () -> {
            RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
            threadContext.addSprite(runnableSprite);
            highlightSnippet(3);

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
                    highlightSnippet(6);
                    sprite.setPooled(true);
                    runnableSprite.setDone();
                    threadContext.stopThread(runnableSprite);
                } else {
                    printf("Thread %s not known to context%n", thread);
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

        threadContext.addButton("prestartAllCoreThreads()", () -> {
            ((ThreadPoolExecutor) executor).prestartAllCoreThreads();
            highlightSnippet(4);
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
        setSnippetFile("executors.html");
        setImage("images/executors.jpg", .7f);

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
