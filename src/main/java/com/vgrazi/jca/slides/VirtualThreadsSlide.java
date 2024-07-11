package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VirtualThreadsSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${completable-future-height}")
    private int completableFutureHeight;

    @Value("${monolith-left-border}")
    private int leftBorder;
    @Value("${monolith-right-border}")
    private int rightBorder;
    @Value("${arrow-length}")
    private int arrowLength;
    private final List<ThreadSprite> sleeping = new ArrayList<>();
    private final List<String> carriers = new ArrayList<>();

    /**
     * The first thread we create will be contained in the future
     * Then every time we create a new future, we set firstThread = null
     * so that the next thread will be the first thread, to be used in the next future
     */

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;
    private AtomicInteger threadCount = new AtomicInteger();

    public VirtualThreadsSlide() {
    }

    public void run() {
        reset();
        threadContext.addButton("Thread.ofVirtual().start(()->someAction())", () -> addCreateAction(2, ""));

        threadContext.addButton("sleep()", () -> addSleepAction(6));

        threadContext.addButton("Reset", this::reset);

        threadContext.setVisible();
    }

    private void addSleepAction(int state) {
        ThreadSprite runningThread = threadContext.getFirstNonWaitingThreadSprite();
        sleeping.add(runningThread);

    }


    /**
     * Called by the runAsynch and supplyAsync methods, adds a completableFutureSprite to the screen
     */
    private void addCreateAction(int state, String type) {
        highlightSnippet(state);
        ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("virtualRunnerThreadSprite");
        threadSprite.setXPosition(leftBorder + arrowLength);
        threadCount.incrementAndGet();

        threadSprite.attachAndStartRunnable(() -> {
            while (threadSprite.isRunning()) {
                if(sleeping.contains(threadSprite)){
                    sleeping.remove(threadSprite);
                  try {
                    Thread.sleep(2_000);
                  } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
            }
            println(threadSprite + " exiting");
        }, false);
        threadContext.addSprite(threadSprite);
    }


    public void reset() {
        super.reset();
        highlightSnippet(0);
        threadCanvas.hideMonolith(true);
        threadContext.setSlideLabel("Virtual Threads");
        threadContext.setSlideLabel("                                          Carrier", 1);
        threadCount.set(0);
        sleeping.clear();
        carriers.clear();
//        setSnippetFile("completable-future.html");
//        setImage("images/future.jpg", .7f);
//        firstThread = null;
//        completableFutures.clear();
//        bigFutureSprites.clear();
//        smallFutureSprites.clear();
//        valueIdGenerator.set(0);
//        smallFuturePointer = 0;
    }

}
