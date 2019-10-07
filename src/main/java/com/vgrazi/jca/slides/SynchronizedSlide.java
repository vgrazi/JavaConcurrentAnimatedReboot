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
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            addYieldRunnable(mutex, sprite);

        });


//        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
//
        threadContext.addButton("wait()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();

            if (runningSprite != null) {
                runningSprite.setAction("waiting");
                log("Calling wait() on Runnable", runningSprite);
            }


        });

       threadContext.addButton("notify()", () -> {
           ThreadSprite runningSprite = threadContext.getRunningThread();
           if (runningSprite != null) {
               // The new running thread should call notify
               runningSprite.setAction("notifying");
               log("Set notifying on ", runningSprite);
           }
       });

       threadContext.addButton("notifyAll()", () -> {
           ThreadSprite runningSprite = threadContext.getRunningThread();
           if (runningSprite != null) {
               // The new running thread should call notify
               runningSprite.setAction("notifyingAll");
               log("Set notifyAll on ", runningSprite);
           }
       });

       threadContext.addButton("Release", () -> {
           ThreadSprite runningSprite = threadContext.getRunningThread();
           if (runningSprite != null) {
               // The new running thread should call notify
               runningSprite.setAction("release");
               log("Set release on ", runningSprite);
           }
       });
        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();

    }

    protected void reset() {
        super.reset();
        threadContext.setSlideLabel("synchronized/wait/notify");
        mutex = new Object();
    }

    private void addYieldRunnable(Object mutex, ThreadSprite sprite) {
        sprite.attachAndStartRunnable(() -> {
            try {
                synchronized (mutex) {
                    System.out.println("Target state:" + sprite.getAction());
                    while (sprite.isRunning()) {
                        if ("release".equals(sprite.getAction())) {
                            threadContext.stopThread(sprite);
                            break;
                        }
                        switch (sprite.getAction()) {
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
        threadContext.addSprite(sprite);
        System.out.println("Added " + sprite);
    }
}
