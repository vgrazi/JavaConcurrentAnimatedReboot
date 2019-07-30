package com.vgrazi.jca.slides;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SynchronizedSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public void run() throws InterruptedException {
        Object mutex = new Object();
        Logging.sleepAndLog("Created mutex");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        addYieldRunnable(mutex, sprite1);
        Logging.sleepAndLog("Added first runnable " + sprite1);

        ThreadSprite sprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        addYieldRunnable(mutex, sprite2);
        Logging.sleepAndLog("Added second runnable " + sprite2);

        ThreadSprite runningSprite = threadContext.getRunningThread();
        runningSprite.setTargetState(ThreadSprite.TargetState.waiting);
        Logging.sleepAndLog("Set waiting on " + runningSprite);

        runningSprite = threadContext.getRunningThread();
        runningSprite.setTargetState(ThreadSprite.TargetState.notifying);
        Logging.sleepAndLog("Set notifying on " + runningSprite);

        runningSprite = threadContext.getRunningThread();
        runningSprite.setTargetState(ThreadSprite.TargetState.release);
        Logging.sleepAndLog("Set release on " + runningSprite);
//        System.out.println("1. Running:" + runningThreads);
//        threadContext.stopThread(runningSprite);
//        sleepAndLog(runningSprite + " done");
//
//        runningThreads = threadContext.getRunningThreads();
//        System.out.println("2. Running:" + runningThreads);
//        runningSprite = runningThreads.get(0);
//        threadContext.stopThread(runningSprite);
//        sleepAndLog(runningSprite + " done", 10000);
    }

    private void addYieldRunnable(Object mutex, ThreadSprite sprite) {
        sprite.setRunnable(() -> {
            try {
                synchronized (mutex) {
                    while (sprite.isRunning()) {
                        if(sprite.getTargetState() == ThreadSprite.TargetState.release) {
                            threadContext.stopThread(sprite);
                            break;
                        }
                        switch (sprite.getTargetState()) {
                            case waiting:
                                mutex.wait();
                                sprite.setTargetState(ThreadSprite.TargetState.default_state);
                                break;
                            case notifying:
                                mutex.notify();
                                sprite.setTargetState(ThreadSprite.TargetState.default_state);
                                break;
                            case default_state:
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
}
