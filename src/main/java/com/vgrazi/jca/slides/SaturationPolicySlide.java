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
    private SynchronousQueue<Runnable> workQueue;

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
                Runnable[] runnables = new Runnable[1];
                runnables[0] = () -> {
                    PooledThreadSprite<String> sprite = (PooledThreadSprite) threadContext.getThreadSprite(Thread.currentThread());
                    if (sprite != null) {
                        sprite.setPooled(false);
                        sprite.setRunning(true);
                        sprite.setYPosition(runnableSprite.getYPosition());
                        runnableSprite.setThread(Thread.currentThread());
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
                        System.out.printf("Thread %s not known to context%n", Thread.currentThread());
                    }
                };
                executor.execute(runnables[0]);
            } catch (Exception e) {
                e.printStackTrace();
                setMessage(e.getMessage());
                threadContext.stopThread(runnableSprite);
                runnableSprite.setRetreating();
            }
        });

        threadContext.addButton("(done)", () -> {
            PooledThreadSprite<String> sprite = threadContext.getRunningPooledThread();
            if (sprite != null) {
                setCssSelected("done");
                sprite.setRunning(false);
                sprite.setPooled(true);
                }
        });

        threadContext.addButton("setRejectedExecutionHandler(CallerRuns)", ()->{
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        });
        threadContext.addButton("setRejectedExecutionHandler(Discard)", ()->{
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        });
        threadContext.addButton("setRejectedExecutionHandler(DiscardOldest)", ()->{
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        });
        threadContext.addButton("setRejectedExecutionHandler(Abort)", ()->{
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
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
        workQueue = new SynchronousQueue<>();
        executor = new ThreadPoolExecutor(0, 4, 2, TimeUnit.SECONDS,
                workQueue,
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
    }
}
