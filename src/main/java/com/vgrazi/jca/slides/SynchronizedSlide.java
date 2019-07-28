package com.vgrazi.jca.slides;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class SynchronizedSlide implements Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;
    private long stepDelay = 2000;

    public void run() throws InterruptedException {
        Object mutex = new Object();
        sleep("Created mutex");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        addYieldRunnable(mutex, sprite1);
        sleep("Added first runnable " + sprite1);

        ThreadSprite sprite2 = new ThreadSprite();
        addYieldRunnable(mutex, sprite2);
        sleep("Added second runnable " + sprite2);

        ThreadSprite runningSprite = threadContext.getRunningThread();
        runningSprite.setSetState(ThreadSprite.SetState.waiting);
        sleep("Set waiting on " + runningSprite);

        runningSprite = threadContext.getRunningThread();
        runningSprite.setSetState(ThreadSprite.SetState.notifying);
        sleep("Set notifying on " + runningSprite);

        runningSprite = threadContext.getRunningThread();
        runningSprite.setSetState(ThreadSprite.SetState.release);
        sleep("Set release on " + runningSprite);
//        System.out.println("1. Running:" + runningThreads);
//        threadContext.stopThread(runningSprite);
//        sleep(runningSprite + " done");
//
//        runningThreads = threadContext.getRunningThreads();
//        System.out.println("2. Running:" + runningThreads);
//        runningSprite = runningThreads.get(0);
//        threadContext.stopThread(runningSprite);
//        sleep(runningSprite + " done", 10000);
        System.exit(0);

    }

    private void addYieldRunnable(Object mutex, ThreadSprite sprite) throws InterruptedException {
        sprite.setRunnable(() -> {
            try {
                synchronized (mutex) {
                    while (sprite.isRunning()) {
                        if(sprite.getSetState() == ThreadSprite.SetState.release) {
                            break;
                        }
                        switch (sprite.getSetState()) {
                            case waiting:
                                mutex.wait();
                                sprite.setSetState(ThreadSprite.SetState.none);
                                break;
                            case notifying:
                                mutex.notify();
                                sprite.setSetState(ThreadSprite.SetState.none);
                                break;
                            case none:
                                Thread.yield();
                                break;
                        }
                    }
                    System.out.println(sprite + " exiting");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        threadContext.addThread(sprite);
    }

    private void sleep(String message) throws InterruptedException {
        sleep(message, stepDelay);
    }

    private void sleep(String message, long delay) throws InterruptedException {
        System.out.println(LocalTime.now() + " " + message);
        Thread.sleep(delay);
    }
}
