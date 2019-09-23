package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.vgrazi.jca.util.Logging.log;
import static com.vgrazi.jca.util.Logging.logAndSleep;

@Component
public class SynchronizedSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public void run() throws InterruptedException {
        Object mutex = new Object();
        log("Created mutex");

        threadContext.addButton("Add thread", e -> {
            ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
            addYieldRunnable(mutex, sprite1);

        });

        threadContext.setVisible();

//        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
//

        threadContext.addButton("Wait", e -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            runningSprite.setTargetState(ThreadSprite.TargetState.waiting);
            log("Calling wait() on Runnable", runningSprite);


        });

       threadContext.addButton("Notify", e -> {
            try {
                ThreadSprite runningSprite = threadContext.getRunningThread();
                // The new running thread should call notify
                runningSprite.setTargetState(ThreadSprite.TargetState.notifying);
                logAndSleep("Set notifying on " + runningSprite);
                runningSprite = threadContext.getRunningThread();
                runningSprite.setTargetState(ThreadSprite.TargetState.release);
                log("Set release on " + runningSprite);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }


        });

//

//        List<ThreadSprite> runningThreads = threadContext.getRunningThreads();
//        System.out.println("1. Running:" + runningThreads);
//        threadContext.stopThread(runningSprite);
//        logAndSleep(runningSprite + " done");
//
//        runningThreads = threadContext.getRunningThreads();
//        System.out.println("2. Running:" + runningThreads);
//        runningSprite = runningThreads.get(0);
//        threadContext.stopThread(runningSprite);
//        logAndSleep(1000, " done", runningSprite);
    }

    private void addYieldRunnable(Object mutex, ThreadSprite sprite) {
        sprite.attachAndStartRunnable(() -> {
            try {
                synchronized (mutex) {
                    System.out.println("Target state:" + sprite.getTargetState());
                    while (sprite.isRunning()) {
                        if (sprite.getTargetState() == ThreadSprite.TargetState.release) {
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
        System.out.println("Added " + sprite);
    }
}
