package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class SemaphoreSlide extends Slide {

    private final ApplicationContext applicationContext;

    public SemaphoreSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    private Semaphore semaphore = new Semaphore(4);
    public void run() {
        reset();

        threadContext.addButton("acquire()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(()-> {
                threadContext.addSprite(sprite);
                try {
                    log("About to acquire", sprite);
                    semaphore.acquire();
                    log("acquired ", sprite);
                    while (sprite.isRunning()) {
                        Thread.yield();
                    }
                    threadContext.stopThread(sprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        threadContext.addButton("tryAcquire()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(()-> {
                threadContext.addSprite(sprite);
                boolean b = semaphore.tryAcquire();

                if (b) {
                    log("acquired ", sprite);
                    while (sprite.isRunning()) {
                        Thread.yield();
                    }
                }
                else {
                    sprite.setRetreating(true);
                }
                threadContext.stopThread(sprite);
            });
        });
        threadContext.addButton("tryAcquire(3, TimeUnit.SECONDS)", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(()-> {
                threadContext.addSprite(sprite);
                try {
                    boolean b = semaphore.tryAcquire(3, TimeUnit.SECONDS);
                    if(!b) {
                        // todo: create a backoff rendering for threadSprite
                        sprite.setRetreating(true);
                    }
                    else {
                        log("acquired ", sprite);
                        while (sprite.isRunning()) {
                            Thread.yield();
                        }
                    }
                    threadContext.stopThread(sprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });

        threadContext.addButton("release()", () -> {
            ThreadSprite sprite = threadContext.getRunningThread();
            if (sprite != null) {
                semaphore.release();
                sprite.attachAndStartRunnable(()-> {
                    threadContext.stopThread(sprite);
                });
            }
        });

//
////        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
////
//        threadContext.addButton("wait()", () -> {
//            ThreadSprite runningSprite = threadContext.getRunningThread();
//
//            if (runningSprite != null) {
//                runningSprite.setAction("waiting");
//                log("Calling wait() on Runnable", runningSprite);
//            }
//
//
//        });
//
//       threadContext.addButton("notify()", () -> {
//           ThreadSprite runningSprite = threadContext.getRunningThread();
//           if (runningSprite != null) {
//               // The new running thread should call notify
//               runningSprite.setAction("notifying");
//               log("Set notifying on ", runningSprite);
//           }
//       });
//
//       threadContext.addButton("notifyAll()", () -> {
//           ThreadSprite runningSprite = threadContext.getRunningThread();
//           if (runningSprite != null) {
//               // The new running thread should call notify
//               runningSprite.setAction("notifyingAll");
//               log("Set notifyAll on ", runningSprite);
//           }
//       });
//
//       threadContext.addButton("Release", () -> {
//           ThreadSprite runningSprite = threadContext.getRunningThread();
//           if (runningSprite != null) {
//               // The new running thread should call notify
//               runningSprite.setAction("release");
//               log("Set release on ", runningSprite);
//           }
//       });
        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();

    }

    public void reset() {
        super.reset();
        semaphore = new Semaphore(4);
        threadContext.setSlideLabel("Semaphore");
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
