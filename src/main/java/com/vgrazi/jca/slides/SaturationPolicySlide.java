package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.PooledThreadSprite;
import com.vgrazi.jca.sprites.RunnableSprite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Use this as a starting point for constructing new slides. To use, uncomment the addButton for basicSlide in JCAFrame
 */
@Component
public class SaturationPolicySlide extends Slide {

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${monolith-left-border}")
    private int leftBorder;

    private ApplicationContext applicationContext;

    private ThreadPoolExecutor executor;

    public SaturationPolicySlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        reset();

        threadContext.addButton("execute", () -> {
            RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
            threadContext.addSprite(runnableSprite);
            setState(2);

            try {
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
                        sprite.setPooled(true);
                        runnableSprite.setDone();
                        threadContext.stopThread(runnableSprite);
                        setState(0);
                    } else {
                        threadContext.stopThread(runnableSprite);
                        runnableSprite.setRetreating();

                        System.out.printf("Thread %s not known to context%n", thread);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        threadContext.addButton("submit", () -> {
            RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
            threadContext.addSprite(runnableSprite);
            setState(3);

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
                    threadContext.stopThread(runnableSprite);
                    setState(0);
                } else {
                    System.out.printf("Thread %s not known to context%n", thread);
                }
                        });
                    });

        threadContext.addButton("(done)", () -> {
            PooledThreadSprite<String> sprite = threadContext.getRunningPooledThread();
            if (sprite != null) {
                setCssSelected("done");
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
        setSnippetFile("executors.html");

        threadContext.setSlideLabel("Saturation Policy");
//        setSnippetFile("some.html");
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                r -> {
                    PooledThreadSprite<String> sprite = (PooledThreadSprite) applicationContext.getBean("pooledThreadSprite");
                    Thread thread = new Thread(r);
                    sprite.setThread(thread);
                    sprite.setPooled(true);
                    sprite.setRunning(false);
                    threadContext.addSprite(sprite);
                    return thread;
                }
        );
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    }
}
