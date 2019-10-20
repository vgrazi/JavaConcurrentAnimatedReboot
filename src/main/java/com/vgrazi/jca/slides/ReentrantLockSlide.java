package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class ReentrantLockSlide extends Slide {

    private final ApplicationContext applicationContext;

    public ReentrantLockSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private ReentrantLock lock = new ReentrantLock();

    @Autowired
    @Qualifier("dottedStroke")
    private Stroke dottedStroke;

    public void run() {
        reset();

        threadContext.addButton("lock()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.attachAndStartRunnable(() -> {
                lock.lock();
                while (sprite.getHolder()) {
                    Thread.yield();
                }
                lock.unlock();
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });
        threadContext.addButton("lockInterrubtibly()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.setSpecialId(1);
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.setStroke(dottedStroke);
            sprite.attachAndStartRunnable(() -> {
                try {
                    lock.lockInterruptibly();
                    while (sprite.getHolder()) {
                        Thread.yield();
                    }
                    lock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    sprite.setMessage(e.toString());
                }
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });

        threadContext.addButton("tryLock()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.attachAndStartRunnable(() -> {
                boolean acquired = lock.tryLock();
                if (acquired) {
                    while (sprite.getHolder()) {
                        Thread.yield();
                    }
                    lock.unlock();
                }
                else {
                    sprite.setRetreating(true);
                }
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });

        threadContext.addButton("interrupt()", () -> {
//            ThreadSprite sprite = threadContext.getFirstWaitingInterruptibleThread();
            ThreadSprite sprite = threadContext.getFirstWaitingThreadOfSpecialId(1);
            sprite.setRetreating(true);
            sprite.getThread().interrupt();
//            setCssSelected("synchronized");
        });


//        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
//
        threadContext.addButton("wait()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();

            if (runningSprite != null) {
                runningSprite.setAction("waiting");
                log("Calling wait() on Runnable", runningSprite);
            }
//            setCssSelected("wait");
        });

        threadContext.addButton("notify()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("notifying");
                log("Set notifying on ", runningSprite);
//                setCssSelected("notify");
            }
        });

        threadContext.addButton("notifyAll()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("notifyingAll");
                log("Set notifyAll on ", runningSprite);
//                setCssSelected("notify-all");
            }
        });

        threadContext.addButton("unlock()", () -> {
            ThreadSprite<Boolean> runningSprite = threadContext.getRunningThread();
            runningSprite.setHolder(false);
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("release");
                log("Set release on ", runningSprite);
//                setCssSelected("release");
            }
        });

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();

    }

    protected void reset() {
        super.reset();
        threadContext.setSlideLabel("ReentrantLock");
//        Set styleSelectors = threadContext.setSnippetFile("synchronized.html");
//        setStyleSelectors(styleSelectors);
        lock = new ReentrantLock();
    }
}
