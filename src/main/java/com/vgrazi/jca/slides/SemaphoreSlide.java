package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class SemaphoreSlide extends Slide {

    private final ApplicationContext applicationContext;

    public SemaphoreSlide(ApplicationContext applicationContext) {
        this.applicationContext=applicationContext;
    }

    private Semaphore semaphore=new Semaphore(4);
    private final Set<ThreadSprite> alreadyTried = new HashSet<>();

    public void run() {
        reset();

        threadContext.addButton("semaphore.acquire()", () -> {
            ThreadSprite sprite=(ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(() -> {
                threadContext.addSprite(sprite);
                highlightSnippet(1);
                try {
                    log("About to acquire", sprite);
                    semaphore.acquire();
                    printAvailablePermits();

                    log("acquired ", sprite);
                    while(sprite.isRunning()) {
                        Thread.yield();
                    }
                    threadContext.stopThread(sprite);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    sprite.setRetreating();
                }
            }, true);
        });

        threadContext.addButton("semaphore.acquireUninterruptibly()", () -> {
            ThreadSprite sprite=(ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(() -> {
                threadContext.addSprite(sprite);
                highlightSnippet(8);
                log("About to acquire", sprite);
                semaphore.acquireUninterruptibly();
                printAvailablePermits();

                log("acquired ", sprite);
                while(sprite.isRunning()) {
                    Thread.yield();
                }
                threadContext.stopThread(sprite);
            }, true);
        });

        threadContext.addButton("semaphore.tryAcquire()", () -> {
            highlightSnippet(3);
            ThreadSprite sprite=(ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(() -> {
                threadContext.addSprite(sprite);
                boolean b=semaphore.tryAcquire();
                printAvailablePermits();

                if(b) {
                    log("acquired ", sprite);
                    while(sprite.isRunning()) {
                        Thread.yield();
                    }
                } else {
                    sprite.setRetreating();
                }
                threadContext.stopThread(sprite);
            }, true);
        });

        threadContext.addButton("semaphore.tryAcquire(3, TimeUnit.SECONDS)", () -> {
            ThreadSprite sprite=(ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            highlightSnippet(4);
            sprite.attachAndStartRunnable(() -> {
                threadContext.addSprite(sprite);
                try {
                    boolean b=semaphore.tryAcquire(3, TimeUnit.SECONDS);
                    printAvailablePermits();

                    if(!b) {
                        // todo: create a backoff rendering for threadSprite
                        sprite.setRetreating();
                    } else {
                        log("acquired ", sprite);
                        while(sprite.isRunning()) {
                            Thread.yield();
                        }
                    }
                    threadContext.stopThread(sprite);
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    sprite.setRetreating();
                }
            }, true);
        });

        threadContext.addButton("semaphore.release()", () -> {
            ThreadSprite sprite=threadContext.getRunningThread();
            highlightSnippet(2);
            semaphore.release();
            printAvailablePermits();
            if(sprite!=null) {
                sprite.attachAndStartRunnable(() -> {
                    threadContext.stopThread(sprite);
                }, true);
            }
        });

        threadContext.addButton("interrupt", () -> {
            highlightSnippet(7);
            ThreadSprite sprite=threadContext.getFirstWaitingThreadExcept(alreadyTried);
            if(sprite!=null){
                sprite.getThread().interrupt();
                alreadyTried.add(sprite);
            }
        });


        threadContext.addButton("semaphore.drainPermits()", () -> {
            highlightSnippet(5);
            int count=semaphore.drainPermits();
            printAvailablePermits();
        });

        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();

    }

    private void printAvailablePermits() {
        setMessage("Available permits:" + semaphore.availablePermits());
    }

    public void reset() {
        super.reset();
        alreadyTried.clear();
        threadContext.setSlideLabel("Semaphore");
        setSnippetFile("semaphore.html");
        semaphore=new Semaphore(4);
    }
}
