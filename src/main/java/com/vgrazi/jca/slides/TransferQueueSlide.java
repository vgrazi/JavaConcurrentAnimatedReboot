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

        threadContext.addButton("transferQueue.transfer(object)", () -> {
            setState(1);
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            threadContext.addSprite(objectSprite);
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(rightBorder - 10);
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.transfer("xxx");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                threadContext.stopThread(objectSprite);
                if (getter != null) {
                    threadContext.stopThread(getter);
                }
            });
        });

        threadContext.addButton("transferQueue.tryTransfer(object)", () -> {
            setState(3);
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            threadContext.addSprite(objectSprite);
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(rightBorder - 10);
            }
            objectSprite.attachAndStartRunnable(() -> {
                boolean success = transferQueue.tryTransfer("xxx");
                if(!success) {
                    objectSprite.setRetreating();
                }
                threadContext.stopThread(objectSprite);
                if (getter != null) {
                    threadContext.stopThread(getter);
                }
            });
        });

        threadContext.addButton("transferQueue.tryTransfer(object, 2,TimeUnit.SECONDS)", () -> {
            setState(5);
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            threadContext.addSprite(objectSprite);
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(rightBorder - 10);
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    boolean success = transferQueue.tryTransfer("xxx", 2000, TimeUnit.MILLISECONDS);
                    if(!success) {
                        objectSprite.setRetreating();
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

//        threadContext.addButton("transferQueue.put(object)", () -> {
//            setState(6);
//            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
//            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
//            threadContext.addSprite(objectSprite);
//            if (getter != null) {
//                objectSprite.setYPosition(getter.getYPosition());
//                objectSprite.setXPosition(rightBorder - 10);
//            }
//            objectSprite.attachAndStartRunnable(() -> {
//                try {
//                    transferQueue.put("xxx");
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            });
//        });

        threadContext.addButton("transferQueue.take()", () -> {
                    // If there are waiting objects, don't create a new sprite
                    setState(2);
                    ObjectSprite objectSprite = threadContext.getFirstWaitingObjectSprite();

                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                threadContext.setGetterNextYPos(getter);
                                transferQueue.take();
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

                                transferQueue.take();
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

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("TransferQueue");
        setSnippetFile("transfer-queue.html");
        transferQueue = new LinkedTransferQueue();
    }
}
