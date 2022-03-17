package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.GetterThreadSprite;
import com.vgrazi.jca.sprites.ObjectSprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

@Component
public class TransferQueueSlide extends Slide {
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

    private TransferQueue transferQueue;

    public void run() {
        reset();

        threadContext.addButton("transferQueue.transfer(object)", () -> addAction(1, "transfer"));

        threadContext.addButton("transferQueue.tryTransfer(object)", () -> addAction(3, "try-transfer"));

        threadContext.addButton("transferQueue.tryTransfer(object, 2,TimeUnit.SECONDS)", () -> addAction(5, "try-timed-transfer"));

        threadContext.addButton("transferQueue.put()", () -> {
            highlightSnippet(6);
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            threadContext.addSprite(objectSprite);
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(rightBorder - 10);
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.put("xxx");
                    if (getter == null) {
                        objectSprite.setAction("waiting");
                        while("waiting".equals(objectSprite.getAction())){
                            Thread.yield();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                threadContext.stopThread(objectSprite);
                if (getter != null) {
                    threadContext.stopThread(getter);
                }

            });
        });

        threadContext.addButton("transferQueue.take()", () -> {
                    // If there are waiting objects, don't create a new sprite
                    highlightSnippet(2);
                    ObjectSprite objectSprite = threadContext.getFirstWaitingObjectSprite();

                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                threadContext.setGetterNextYPos(getter);
                                Object take = transferQueue.take();

                                println("Took " + take);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            threadContext.stopThread(getter);
                        });
                    } else {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                getter.setYPosition(objectSprite.getYPosition());
                                objectSprite.setXPosition(rightBorder - 20);
                                objectSprite.setAction("done");

                                Object take = transferQueue.take();
                                println("Took " + take);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            threadContext.stopThread(objectSprite);
                            threadContext.stopThread(getter);
                        });
                    }
                    threadContext.addSprite(getter);
                }
        );

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();
    }

    private void addAction(int state, String type) {
        highlightSnippet(state);
        ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
        GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
        threadContext.addSprite(objectSprite);
        if (getter != null) {
            objectSprite.setYPosition(getter.getYPosition());
            objectSprite.setXPosition(rightBorder - 10);
        }
        objectSprite.attachAndStartRunnable(() -> {
            if (type.equals("try-transfer")) {
                boolean success = transferQueue.tryTransfer("xxx");
                if (!success) {
                    objectSprite.setRetreating();
                }
            } else if (type.equals("transfer")) {
                try {
                    transferQueue.transfer("xxx");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (type.equals("try-timed-transfer")) {
                try {
                    boolean success = transferQueue.tryTransfer("xxx", 2000, TimeUnit.MILLISECONDS);
                    if (!success) {
                        objectSprite.setRetreating();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            threadContext.stopThread(objectSprite);
            if (getter != null) {
                threadContext.stopThread(getter);
            }
        });
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("TransferQueue");
        setSnippetFile("transfer-queue.html");
        transferQueue = new LinkedTransferQueue();
    }
}
