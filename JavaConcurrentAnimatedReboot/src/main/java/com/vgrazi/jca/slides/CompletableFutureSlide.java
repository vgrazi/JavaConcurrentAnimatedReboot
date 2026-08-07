package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class CompletableFutureSlide extends Slide {

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
    private final List<CompletableFuture> completableFutures = new ArrayList<>();

    /**
     * The first thread we create will be contained in the future
     * Then every time we create a new future, we set firstThread = null
     * so that the next thread will be the first thread, to be used in the next future
     */
    private ThreadSprite<Boolean> firstThread;
    private int threadCount;
    private final List<CompletableFutureSprite> bigFutureSprites = new ArrayList<>();
    private final List<CompletableFutureSprite> smallFutureSprites = new ArrayList<>();
    private int smallFuturePointer = 0;

    @Value("${future-top-margin}")
    private int futureTopMargin;
    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;
    private AtomicInteger valueIdGenerator = new AtomicInteger(0);

    public CompletableFutureSlide() {
    }

    public void run() {
        reset();
        threadContext.addButton("supplyAsync", () -> addCreateAction(2, "supply-async"));
        threadContext.addButton("runAsync", () -> addCreateAction(1, "run-async"));

        threadContext.addButton("CompletableFuture.anyOf()", () -> {
            if (!completableFutures.isEmpty()) {
                highlightSnippet(4);
            }
            CompletableFuture<?> future = CompletableFuture.anyOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            addCompletableFutureSprite(future, "anyOf");
        });

        threadContext.addButton("CompletableFuture.allOf()", () -> {
            if (!completableFutures.isEmpty()) {
                highlightSnippet(3);
            }
            CompletableFuture<Void> future = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            addCompletableFutureSprite(future, "allOf");
        });

        threadContext.addButton("join()", () -> addGetAction(6, "join"));

        threadContext.addButton("get()", () -> addGetAction(5, "get"));

        threadContext.addButton("getNow()", () -> addGetAction(7, "get-now"));

        threadContext.addButton("cancel(true)", () -> addGetAction(10, "cancel"));

        threadContext.addButton("thenRun", () -> {
            if (!bigFutureSprites.isEmpty()) {

                highlightSnippet(8);
                RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
                threadContext.reclaimYPosition();
                runnableSprite.setXPosition(leftBorder + 15);
                runnableSprite.setRunning(true);
                runnableSprite.setHolder(true);
                int spriteXOffset = rightBorder - leftBorder + 15;
                runnableSprite.setXOffset(spriteXOffset);
                CompletableFutureSprite bigFutureSprite = bigFutureSprites.get(bigFutureSprites.size() - 1);
                runnableSprite.setYPosition(bigFutureSprite.getYPosition() - futureTopMargin + 10);

                CompletableFuture completableFuture = bigFutureSprite.getFuture();
                CompletableFuture thenRun = completableFuture.thenRun(() -> {
                            RunnerThreadSprite runnerThreadSprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
                            threadContext.reclaimYPosition();
                            runnerThreadSprite.setYPosition(runnableSprite.getYPosition());
                            runnerThreadSprite.setXPosition(leftBorder + 5);
                            runnerThreadSprite.setXOffset(spriteXOffset);

                            // set the holder to true for running
                            runnerThreadSprite.setHolder(true);
                            runnerThreadSprite.attachAndStartRunnable(() -> {
                                long startTime = System.currentTimeMillis();
                                highlightSnippet(9);
//                                runnerThreadSprite.fadeOut();
//                                runnableSprite.fadeOut();
//                                while(runnerThreadSprite.getState() != threadContext.terminated) {
                                while(System.currentTimeMillis() - startTime < 2_000) {
                                    Thread.yield();
                                }

                                threadContext.stopThread(runnerThreadSprite);
                                threadContext.stopThread(runnableSprite);
                            }, true);
                            threadContext.addSprite(runnerThreadSprite);
                        }
                );
                runnableSprite.attachAndStartRunnable(() -> {
                    while (runnableSprite.isRunning()) {
                        Thread.yield();
                    }
                    println(runnableSprite + " exiting");
                }, true);
                threadContext.addSprite(runnableSprite);
            }
            else if (!smallFutureSprites.isEmpty()) {
                highlightSnippet(2);
                RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
                threadContext.reclaimYPosition();
                runnableSprite.setXPosition(leftBorder + 15);
                runnableSprite.setRunning(true);
                runnableSprite.setHolder(true);
                int spriteXOffset = rightBorder - leftBorder + 15;
                runnableSprite.setXOffset(spriteXOffset);
                int pointer = smallFuturePointer;
                if(pointer < smallFutureSprites.size() -1) {
                    smallFuturePointer++;
                }
                CompletableFutureSprite smallFutureSprite = smallFutureSprites.get(pointer);
                runnableSprite.setYPosition(smallFutureSprite.getYPosition() - futureTopMargin + 10);

                CompletableFuture completableFuture = smallFutureSprite.getFuture();
                CompletableFuture thenRun = completableFuture.thenRun(() -> {
                            RunnerThreadSprite runnerThreadSprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
                            threadContext.reclaimYPosition();
                            runnerThreadSprite.setYPosition(runnableSprite.getYPosition());
                            runnerThreadSprite.setXPosition(leftBorder + 5);
                            runnerThreadSprite.setXOffset(spriteXOffset);

                            // set the holder to true for running
                            runnerThreadSprite.setHolder(true);
                            runnerThreadSprite.attachAndStartRunnable(() -> {
                                long startTime = System.currentTimeMillis();
//                                runnerThreadSprite.fadeOut();
//                                runnableSprite.fadeOut();
//                                while(runnerThreadSprite.getState() != threadContext.terminated) {
                                while(System.currentTimeMillis() - startTime < 2_000) {
                                    Thread.yield();
                                }

                                threadContext.stopThread(runnerThreadSprite);
                                threadContext.stopThread(runnableSprite);
                            }, true);
                            threadContext.addSprite(runnerThreadSprite);
                        }
                );
                runnableSprite.attachAndStartRunnable(() -> {
                    while (runnableSprite.isRunning()) {
                        Thread.yield();
                    }
                    println(runnableSprite + " exiting");
                }, true);
                threadContext.addSprite(runnableSprite);
            }
        });


        threadContext.addButton("Done", () -> {
            ThreadSprite<Boolean> runningSprite = threadContext.getRunningThread();
            if (runningSprite != null) {
                runningSprite.setHolder(false);
            }
            log("Set release on ", runningSprite);
        });

        threadContext.addButton("Reset", this::reset);

        threadContext.setVisible();
    }

    private void addGetAction(int state, String type) {
        if (!bigFutureSprites.isEmpty() || !smallFutureSprites.isEmpty()) {
            CompletableFutureSprite futureSprite;

            if (!bigFutureSprites.isEmpty()) {
                futureSprite = bigFutureSprites.get(bigFutureSprites.size() - 1);
            } else {
//          !completableFutures.isEmpty
                futureSprite = smallFutureSprites.get(smallFutureSprites.size() - 1);
            }
            highlightSnippet(state);
            GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
            getter.setYPosition(futureSprite.getYCenter());
            getter.attachAndStartRunnable(() -> {
                CompletableFuture future = futureSprite.getFuture();
                if (!future.isCompletedExceptionally()) {
                    Object value = null;
                    try {
                        switch (type) {
                            case "get":
                                try {
                                    value = future.get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "join":
                                value = future.join();
                                break;
                            case "get-now":
                                value = future.getNow("\"valueIfAbsent\"");
                                break;
                            case "cancel":
                                ThreadSprite<Boolean> runningSprite = threadContext.getRunningThread();
                                if (runningSprite != null) {
                                    runningSprite.setHolder(false);
                                }

                                try {
                                    future.cancel(true);
                                } catch (CancellationException e) {
                                    setMessage("Already canceled " + e.getMessage());
                                }
                                break;
                        }
                    } catch (CancellationException | ExecutionException e) {
                        log(e.getMessage());
                        setMessage("Exception " + e.getMessage() + ". Cause:" + e.getCause() );
                    }
                    getter.setLabel(String.valueOf(value));
                }
                threadContext.stopThread(getter);
            }, true);
            threadContext.addSprite(getter);
        }
    }
    /**
     * Called by the runAsynch and supplyAsync methods, adds a completableFutureSprite to the screen
     */
    private void addCreateAction(int state, String type) {
        highlightSnippet(state);
        ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        if (firstThread == null) {
            firstThread = threadSprite;
        }
        threadSprite.setXPosition(leftBorder + arrowLength);
        threadCount++;
        // we need to create a future, a thread to attach to it, and sprites for each of those
        CompletableFutureSprite futureSprite = (CompletableFutureSprite) applicationContext.getBean("completableFutureSprite");
        smallFutureSprites.add(futureSprite);
        futureSprite.setXMargin(15);
        futureSprite.setXRightMargin(15);
        futureSprite.setYMargin(+6);
        futureSprite.setYPosition(threadSprite.getYPosition() - futureTopMargin);
        // the holder contains the running status. When it is done, will be set to false
        threadSprite.setHolder(true);
        CompletableFuture future = null;
        switch (type) {
            case "run-async": {
                CompletableFuture[] futures = new CompletableFuture[1];
                futures[0] = future = CompletableFuture.runAsync(() -> {
                    while (threadSprite.getHolder()) {
                        Thread.yield();
                    }
                    threadContext.stopThread(threadSprite);
                });
                break;
            }
            case "supply-async": {
                CompletableFuture[] futures = new CompletableFuture[1];
                futures[0] = future = CompletableFuture.supplyAsync(() -> {
                    while (threadSprite.getHolder()) {
                        Thread.yield();
                    }
                    threadContext.stopThread(threadSprite);
                    String value;
                    if (!futures[0].isCancelled()) {
                        value = "value " + valueIdGenerator.incrementAndGet();
                    }
                    else {
                        value = "canceled";
                    }
                    return value;
                });
                break;
            }
        }
        completableFutures.add(future);
        futureSprite.setFuture(future);
        futureSprite.setHeight(completableFutureHeight);
        threadContext.addSprite(0, futureSprite);
        threadSprite.attachAndStartRunnable(() -> {
            while (threadSprite.isRunning()) {
                Thread.yield();
            }
            println(threadSprite + " exiting");
        }, true);
        threadContext.addSprite(threadSprite);
    }

    /**
     * Start a new completable future based on the current futures
     */
    private void addCompletableFutureSprite(CompletableFuture future, String label) {
        CompletableFutureSprite futureSprite = (CompletableFutureSprite) applicationContext.getBean("completableFutureSprite");
        futureSprite.setLabel(label);
        completableFutures.clear();
        futureSprite.setFuture(future);
        if (firstThread != null) {
            futureSprite.setYPosition(firstThread.getYPosition());
        }
        futureSprite.setHeight((threadCount - 1) * pixelsPerYStep);
        int width = futureSprite.getWidth();
        futureSprite.setWidth(width + 20);
        futureSprite.setYMargin(20);
        futureSprite.setXMargin(20);
        // waste some Y space
        threadContext.addYPixels(15);
        firstThread = null;
        threadCount = 0;
        futureSprite.setDisplayValue(false);
        threadContext.addSprite(0, futureSprite);
        bigFutureSprites.add(futureSprite);
    }

    public void reset() {
        super.reset();
        highlightSnippet(0);
        threadCanvas.hideMonolith(true);
        threadContext.setSlideLabel("CompletableFuture");
        setSnippetFile("completable-future.html");
        setImage("images/future.jpg", .7f);
        firstThread = null;
        threadCount = 0;
        completableFutures.clear();
        bigFutureSprites.clear();
        smallFutureSprites.clear();
        valueIdGenerator.set(0);
        smallFuturePointer = 0;
    }

}
