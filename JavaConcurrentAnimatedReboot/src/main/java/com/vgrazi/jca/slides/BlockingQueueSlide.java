package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.GetterThreadSprite;
import com.vgrazi.jca.sprites.ObjectSprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class BlockingQueueSlide extends Slide {
    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    @Value("${arrow-length}")
    private int arrowLength;

    @Autowired
    private ApplicationContext applicationContext;

    private BlockingQueue blockingQueue;

    public void run() {
        reset();
        threadContext.addButton("put()", () -> {
            addAction(1, "put");
        });
        threadContext.addButton("add()", () -> {
            addAction(6, "add");
        });

        threadContext.addButton("offer(obj)", () -> {
            addAction(2, "offer");
        });

        threadContext.addButton("offer(obj, 5, TimeUnit.SECONDS)", () -> {
            addAction(4, "timed-offer");
        });

        threadContext.addButton("take()", () -> {
            highlightSnippet(5);
                    // If there are waiting objects, don't create a new sprite
                    ObjectSprite objectSprite = threadContext.getFirstRunningObjectSprite();
                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                threadContext.setGetterNextYPos(getter);
                                blockingQueue.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            threadContext.stopThread(getter);
                        }, true);
                    } else {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                getter.setYPosition(objectSprite.getYPosition());
                                objectSprite.setXPosition(rightBorder - 20);

                                blockingQueue.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            objectSprite.setAction("done");
                            threadContext.stopThread(objectSprite);
                            threadContext.stopThread(getter);
                        }, true);
                    }
                    threadContext.addSprite(getter);
                }
        );

        threadContext.addButton("remove()", ()-> {
            highlightSnippet(5);
            // If there are waiting objects, don't create a new sprite
            ThreadSprite getter=(ThreadSprite) applicationContext.getBean("getterSprite");
            getter.attachAndStartRunnable(() -> {
                try {
                    // remove the next. We don't use the result. If it works great, else throws an exception
                    Object remove=blockingQueue.remove();
                    ObjectSprite objectSprite=threadContext.getFirstRunningObjectSprite();
                    objectSprite.setXPosition(rightBorder - 20);
                    getter.setYPosition(objectSprite.getYPosition());
                    objectSprite.setAction("done");
                    threadContext.stopThread(objectSprite);
                } catch(Exception e) {
                    getter.setXPosition(rightBorder+arrowLength);
                    threadContext.setGetterNextYPos(getter);
                    getter.setMessage(e);
                    setMessage(String.valueOf(e));
                }
                finally {
                    threadContext.stopThread(getter);
                }
            }, true);

            threadContext.addSprite(getter);
        });

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();
    }

    private void addAction(int state, String type) {
        highlightSnippet(state);
        ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
        objectSprite.setAction("running");
        threadContext.addSprite(objectSprite);
        GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
        if (getter != null) {
            objectSprite.setYPosition(getter.getYPosition());
            objectSprite.setXPosition(rightBorder - 10);
            objectSprite.setAction("exit");
        }
        objectSprite.attachAndStartRunnable(() -> {
            switch (type) {
                case "put":
                    try {
                        blockingQueue.put("xxx");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                case "add":
                    try {
                        blockingQueue.add("xxx");
                    } catch(IllegalStateException e) {
                        setMessage(e.toString());
                        objectSprite.setRetreating();
                    }

                    break;
                case "offer": {
                    boolean success = blockingQueue.offer("xxx");
                    setMessage("Success: " + success);
                    if (!success) objectSprite.setRetreating();
                }
                break;
                case "timed-offer":
                    try {
                        boolean success = blockingQueue.offer("xxx", 5, TimeUnit.SECONDS);
                        setMessage("Success: " + success);
                        if (!success) objectSprite.setRetreating();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
            }
            while ("running".equals(objectSprite.getAction())) {
                Thread.yield();
            }
            threadContext.stopThread(objectSprite);
            if (getter != null) {
                threadContext.stopThread(getter);
            }
        }, true);
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("BlockingQueue");
        setSnippetFile("blocking-queue.html");
        setImage("images/blockingQueue.jpg", .7f);
        blockingQueue  = new ArrayBlockingQueue(4);
    }
}
