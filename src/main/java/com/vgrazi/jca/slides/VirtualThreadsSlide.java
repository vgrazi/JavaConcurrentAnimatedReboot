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

    @Value("${monolith-left-border}")
    private int leftBorder;
    @Value("${arrow-length}")
    private int arrowLength;

    private int sleepMS = 2500;
    private final List<ThreadSprite> sleeping = new ArrayList<>();
    private final List<ThreadSprite> waiting = new ArrayList<>();
    private final List<ThreadSprite> yield = new ArrayList<>();
    private final List<ThreadSprite> exit = new ArrayList<>();
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
        threadContext.addButton("Thread.ofVirtual().start(()->someAction())", () -> addCreateAction(0));

        threadContext.addButton("sleep(" + sleepMS + ")", () -> addSleepAction(1));
        threadContext.addButton("synchronized(lock){lock.wait(" + sleepMS + ");}", () -> addWaitAction(2));
//        threadContext.addButton("Thread.yield()", () -> addYieldAction(3));
        threadContext.addButton("exit", () -> addExitAction(4));

        threadContext.addButton("Reset", this::reset);

        threadContext.setVisible();
    }

    private void addSleepAction(int state) {
        highlightSnippet(state);
        ThreadSprite runningThread = threadContext.getFirstNonWaitingThreadSprite();
        sleeping.add(runningThread);
    }

    private void addWaitAction(int state) {
        highlightSnippet(state);
        ThreadSprite runningThread = threadContext.getFirstNonWaitingThreadSprite();
        waiting.add(runningThread);
    }

    private void addYieldAction(int state) {
        highlightSnippet(state);
        ThreadSprite runningThread = threadContext.getRandomNonWaitingThreadSprite();
        yield.add(runningThread);
    }
    private void addExitAction(int state) {
        highlightSnippet(state);
        ThreadSprite runningThread = threadContext.getFirstNonWaitingThreadSprite();
        exit.add(runningThread);
    }


    /**
     * Called by the runAsynch and supplyAsync methods, adds a completableFutureSprite to the screen
     */
    private void addCreateAction(int state) {
        highlightSnippet(state);
        ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("virtualRunnerThreadSprite");
        threadSprite.setXPosition(leftBorder + arrowLength);
        threadCount.incrementAndGet();

        threadSprite.attachAndStartRunnable(() -> {
            while (!exit.contains(threadSprite)) {
                if(sleeping.contains(threadSprite)){
                    sleeping.remove(threadSprite);
                  try {
                    Thread.sleep(sleepMS);
                      highlightSnippet(0);
                  } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
                if(waiting.contains(threadSprite)){
                    waiting.remove(threadSprite);
                  try {
                      synchronized(threadSprite) {
                          threadSprite.wait(sleepMS);
                          highlightSnippet(0);
                      }
                  } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
                if(yield.contains(threadSprite)) {
                    yield.remove(threadSprite);
                    Thread.yield();
                }
            }
            exit.remove(threadSprite);
            threadContext.stopThread(threadSprite);
            println(threadSprite + " exiting");
            if(threadContext.getAllThreads().isEmpty())
                threadCount.set(0);
        }, false);
        threadContext.addSprite(threadSprite);
    }


    public void reset() {
        super.reset();
        highlightSnippet(0);
        threadCanvas.hideMonolith(true);
        threadContext.setSlideLabel("Virtual Threads                  ");
        threadContext.setSlideLabel("            Carrier", 1);
        threadCount.set(0);
        sleeping.clear();
        carriers.clear();
        waiting.clear();
        yield.clear();
        exit.clear();
        setSnippetFile("virtualthreads.html");
    }

}
