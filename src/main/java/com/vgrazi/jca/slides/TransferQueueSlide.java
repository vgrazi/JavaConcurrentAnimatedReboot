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

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    @Autowired
    private ApplicationContext applicationContext;

    private TransferQueue transferQueue = new LinkedTransferQueue();

    public void run() {
        reset();
        threadContext.addButton("transfer()", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            threadContext.addSprite(objectSprite);
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition() - pixelsPerYStep / 2);
                objectSprite.setXPosition(leftBorder);
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.transfer("xxx");
                    threadContext.stopThread(objectSprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        threadContext.addButton("tryTransfer()", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.tryTransfer("xxx", 5000, TimeUnit.MILLISECONDS);
//                    threadContext.stopThread(objectSprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threadContext.addSprite(objectSprite);
        });

        threadContext.addButton("take()", () -> {
                    // If there are waiting objects, don't create a new sprite
                    ObjectSprite objectSprite = threadContext.getFirstWaitingObjectSprite();
                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                transferQueue.take();
                                threadContext.stopThread(getter);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                getter.setYPosition(objectSprite.getYPosition() + pixelsPerYStep / 2);
                                transferQueue.take();
                                threadContext.stopThread(objectSprite);
                                threadContext.stopThread(getter);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                    threadContext.addSprite(getter);
                }
        );

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    protected void reset() {
        super.reset();
        threadContext.setSlideLabel("TransferQueue");
        transferQueue = new LinkedTransferQueue();
    }
}
