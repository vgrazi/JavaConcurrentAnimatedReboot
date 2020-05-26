package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    private final List<FutureSprite> bigFutures = new ArrayList<>();

    @Value("${future-top-margin}")
    private int futureTopMargin;
    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;
    private AtomicInteger valueIdGenerator = new AtomicInteger(0);

    public CompletableFutureSlide() {
    }

    public void run() {
        reset();

        threadContext.addButton("supplyAsync", () -> {
            setState(2);
            ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
            if (firstThread == null) {
                firstThread = threadSprite;
            }
            threadSprite.setXPosition(leftBorder + arrowLength);
            threadCount++;
            // we need to create a future, a thread to attach to it, and sprites for each of those
            FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
            futureSprite.setXMargin(15);
            futureSprite.setXRightMargin(15);
            futureSprite.setYMargin(+6);
            futureSprite.setYPosition(threadSprite.getYPosition() - futureTopMargin);
            // the holder contains the running status. When it is done, will be set to false
            threadSprite.setHolder(true);
            CompletableFuture future = CompletableFuture.supplyAsync(() -> {
                while (threadSprite.getHolder()) {
                    Thread.yield();
                }
                threadContext.stopThread(threadSprite);
                return "value " + valueIdGenerator.incrementAndGet();
            });
            System.out.println("CREATED FUTURE:" + future);
            completableFutures.add(future);
            futureSprite.setFuture(future);
            futureSprite.setHeight(completableFutureHeight);
            threadContext.addSprite(0, futureSprite);
            threadSprite.attachAndStartRunnable(() -> {
                while (threadSprite.isRunning()) {
                    Thread.yield();
                }
                System.out.println(threadSprite + " exiting");
            });
            threadContext.addSprite(threadSprite);
        });

        threadContext.addButton("runAsync", () -> {
            setState(1);
            ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
            if (firstThread == null) {
                firstThread = threadSprite;
            }
            threadSprite.setXPosition(leftBorder + arrowLength);
            threadCount++;
            // we need to create a future, a thread to attach to it, and sprites for each of those
            FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
            futureSprite.setXMargin(15);
            futureSprite.setXRightMargin(15);
            futureSprite.setYMargin(+6);
            futureSprite.setYPosition(threadSprite.getYPosition() - futureTopMargin);
            // the holder contains the running status. When it is done, will be set to false
            threadSprite.setHolder(true);
            CompletableFuture future = CompletableFuture.runAsync(() -> {
                while (threadSprite.getHolder()) {
                    Thread.yield();
                }
                threadContext.stopThread(threadSprite);
            });
            completableFutures.add(future);
            futureSprite.setFuture(future);
            futureSprite.setHeight(completableFutureHeight);
            threadContext.addSprite(0, futureSprite);
            threadSprite.attachAndStartRunnable(() -> {
                while (threadSprite.isRunning()) {
                    Thread.yield();
                }
                System.out.println(threadSprite + " exiting");
            });
            threadContext.addSprite(threadSprite);
        });

        threadContext.addButton("CompletableFuture.anyOf()", () -> {
            if (!completableFutures.isEmpty()) {
                setState(4);
            }
            CompletableFuture<?> future = CompletableFuture.anyOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            addCompletableFutureSprite(future);
        });

        threadContext.addButton("CompletableFuture.allOf()", () -> {
            if (!completableFutures.isEmpty()) {
                setState(3);
            }
            CompletableFuture<Void> future = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            addCompletableFutureSprite(future);
        });

        threadContext.addButton("join()", () -> {
            if (!bigFutures.isEmpty()) {
                setState(6);
                GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
                FutureSprite futureSprite = bigFutures.get(bigFutures.size() - 1);
                getter.setYPosition(futureSprite.getYCenter());
                getter.attachAndStartRunnable(() -> {
                    CompletableFuture future = futureSprite.getFuture();
                    Object value = future.join();
                    getter.setLabel(String.valueOf(value));
                    threadContext.stopThread(getter);
                });
                threadContext.addSprite(getter);
            }
        });

        threadContext.addButton("get()", () -> {
            if (!bigFutures.isEmpty()) {
                setState(5);
                GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
                FutureSprite futureSprite = bigFutures.get(bigFutures.size() - 1);
                getter.setYPosition(futureSprite.getYCenter());
                getter.attachAndStartRunnable(() -> {
                    CompletableFuture future = futureSprite.getFuture();
                    try {
                        Object value = future.get();
                        getter.setLabel(String.valueOf(value));
                        threadContext.stopThread(getter);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                threadContext.addSprite(getter);
            }
        });

        threadContext.addButton("getNow()", () -> {
            if (!bigFutures.isEmpty()) {
                setState(7);
                GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
                FutureSprite futureSprite = bigFutures.get(bigFutures.size() - 1);
                getter.setYPosition(futureSprite.getYCenter());
                getter.attachAndStartRunnable(() -> {
                    CompletableFuture<String> future = futureSprite.getFuture();
                    System.out.println("Getter attached to " + future);
                    String value = future.getNow("\"valueIfAbsent\"");
                    getter.setLabel(value);
                    threadContext.addSprite(getter);
                    threadContext.stopThread(getter);
                });
            }
        });

        threadContext.addButton("thenRun", () -> {

            if (!bigFutures.isEmpty()) {

                setState(2);
// RunnerThreadSprite<Boolean> threadSprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");

                FutureRunnableSprite threadSprite = (FutureRunnableSprite) applicationContext.getBean("futureRunnableSprite");
                threadSprite.setRunning(true);
                threadSprite.setHolder(true);
                int spriteXOffset = rightBorder - leftBorder + arrowLength + 15;
                threadSprite.setXOffset(spriteXOffset);
                FutureSprite bigFutureSprite = bigFutures.get(bigFutures.size() - 1);
                threadSprite.setYPosition(bigFutureSprite.getYPosition() - futureTopMargin + 10);

/*
                // we need to create a future, a thread to attach to it, and sprites for each of those
                FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
                futureSprite.setHeight(completableFutureHeight);
                futureSprite.setXMargin(15);
                futureSprite.setXRightMargin(15);
                futureSprite.setYMargin(6);
                futureSprite.setXOffset(spriteXOffset);
                futureSprite.setYPosition(threadSprite.getYPosition() - 8);
                CompletableFuture completableFuture = bigFutureSprite.getFuture();
                CompletableFuture thenRun = completableFuture.thenRun(() -> {
//                    threadSprite.moveMonolithBorder(rightBorder);
//                            threadSprite.setYPosition(futureSprite.getYPosition());
//                            threadSprite.setXPosition(rightBorder+arrowLength);
//                            System.out.println("Setting xPos  for " + threadSprite);
//                    threadSprite.setXPosition(200);
                            while (threadSprite.isRunning()) {
                                Thread.yield();
                            }
                        }
                );

                futureSprite.setFuture(thenRun);
                threadSprite.attachAndStartRunnable(() -> {
                    while (threadSprite.isRunning()) {
                        Thread.yield();
                    }
                    System.out.println(threadSprite + " exiting");
                });
                threadContext.addSprite(0, futureSprite);
*/

                CompletableFuture completableFuture = bigFutureSprite.getFuture();
                CompletableFuture thenRun = completableFuture.thenRun(() -> {
                            while (threadSprite.isRunning()) {
                                Thread.yield();
                            }
                        }
                );
                threadSprite.attachAndStartRunnable(() -> {
                    while (threadSprite.isRunning()) {
                        Thread.yield();
                    }
                    System.out.println(threadSprite + " exiting");
                });
                threadContext.addSprite(threadSprite);
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

    /**
     * Start a new completable future based on the current futures
     */
    private void addCompletableFutureSprite(CompletableFuture future) {
        FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
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
        bigFutures.add(futureSprite);
    }

    public void reset() {
        super.reset();
        setState(0);
        threadCanvas.hideMonolith(true);
        threadContext.setSlideLabel("CompletableFuture");
        setSnippetFile("completable-future.html");
        setImage("images/future.jpg");
        firstThread = null;
        threadCount = 0;
        completableFutures.clear();
        bigFutures.clear();
        valueIdGenerator.set(0);
    }

}
