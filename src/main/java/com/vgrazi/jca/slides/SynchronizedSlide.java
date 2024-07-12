package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class SynchronizedSlide extends Slide {

    private final ApplicationContext applicationContext;

    public SynchronizedSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private Object mutex = new Object();

    public void run() {
        reset();
        threadContext.addButton("Add thread", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            addYieldRunnable(mutex, sprite, true);
            highlightSnippet(1);
        });
//        threadContext.addButton("Add virtual thread", () -> {
//            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("virtualRunnerThreadSprite");
//            addYieldRunnable(mutex, sprite, false);
//            highlightSnippet(1);
//        });

        threadContext.addButton("exit synchronized", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("release");
                log("Set release on ", runningSprite);
                highlightSnippet(2);
            }
        });

//        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
//
        threadContext.addButton("wait()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();

            if (runningSprite != null) {
                runningSprite.setAction("waiting");
                log("Calling wait() on Runnable", runningSprite);
                highlightSnippet(3);
            }
        });
        threadContext.addButton("sleep()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();

            if (runningSprite != null) {
                runningSprite.setAction("sleeping");
                log("Calling sleep() on Runnable", runningSprite);
                highlightSnippet(7);
            }
        });

        threadContext.addButton("notify()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("notifying");
                log("Set notifying on ", runningSprite);
                highlightSnippet(4);
            }
        });

        threadContext.addButton("notifyAll()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                // The new running thread should call notify
                runningSprite.setAction("notifyingAll");
                highlightSnippet(5);
                log("Set notifyAll on ", runningSprite);
            }
        });

        threadContext.addButton("interrupt (running)", () -> {
            // find a sprite that is not interrupted, (starting with running sprites), and interrupt it
            ThreadSprite sprite = threadContext.getRunnableThread();
            if (sprite != null) {
                // The new running thread should call notify
                sprite.getThread().interrupt();
                log("Set interrupt on ", sprite);
                highlightSnippet(6);
            }
        });
        threadContext.addButton("interrupt (blocked)", () -> {
            // find a sprite that is not interrupted, (starting with running sprites), and interrupt it
            ThreadSprite sprite = threadContext.getBlockedNotInterruptedThreadSprite();
            if (sprite != null) {
                // The new running thread should call notify
                sprite.getThread().interrupt();
                log("Set interrupt on ", sprite);
                highlightSnippet(6);
            }
        });

        threadContext.addButton("interrupt (waiting)", () -> {
            // find a sprite that is not interrupted, (starting with running sprites), and interrupt it
            ThreadSprite sprite = threadContext.getWaitingNotInterruptedThread();
            if (sprite != null) {
                // The new running thread should call notify
                sprite.setAction("interrupt");
                sprite.getThread().interrupt();
                log("Set interrupt on ", sprite);
                highlightSnippet(6);
            }
        });

        threadContext.addButton("stop (blocked)", () -> {
            // find a sprite that is not interrupted, (starting with running sprites), and interrupt it
            ThreadSprite sprite=threadContext.getBlockedNotInterruptedThreadSprite();
            if(sprite!=null) {
                // The new running thread should call notify
                sprite.getThread().stop();
                log("Set stop on ", sprite);
//                highlightSnippet(6);
            }
        });

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("synchronized()",0);
        threadContext.setSlideLabel("{           }", 1);
        setSnippetFile("synchronized.html");
        setImage("images/VisibilityAndSynchronization.png", .7f);
        mutex = new Object();
    }

    private void addYieldRunnable(Object mutex, ThreadSprite sprite, boolean isPlatform) {
        sprite.attachAndStartRunnable(() -> {
            try {
                synchronized (mutex) {
                    println("Target state:" + sprite.getAction());
                    while (sprite.isRunning()) {
                        if ("release".equals(sprite.getAction())) {
                            threadContext.stopThread(sprite);
                            break;
                        }
                        switch (sprite.getAction()) {
                            case "sleeping":
                                Thread.sleep(5000);
                                sprite.setAction("default");
                                break;
                            case "waiting":
                                mutex.wait();
                                sprite.setAction("default");
                                break;
                            case "notifying":
                                mutex.notify();
                                sprite.setAction("default");
                                break;
                            case "notifyingAll":
                                mutex.notifyAll();
                                sprite.setAction("default");
                                break;
                            case "default":
                                break;
                        }
                    }
                    println(sprite + " exiting");
                }
            } catch (InterruptedException e) {
                interruptSprite(sprite, e);
            }
        }, isPlatform);
        threadContext.addSprite(sprite);
        println("Added " + sprite);
    }
}
