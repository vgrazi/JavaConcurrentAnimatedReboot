package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.GetterThreadSprite;
import com.vgrazi.jca.sprites.ObjectSprite;
import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
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

    private static final int GETTER_DELTA = 30;
    private int initialGetterYPos = 90 - GETTER_DELTA;


    @Autowired
    private ApplicationContext applicationContext;

    private TransferQueue transferQueue;

    public void run() {
        reset();
        threadContext.addButton("transfer()", () -> {
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

        threadContext.addButton("tryTransfer(2,TimeUnit.SECONDS)", () -> {
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
                        objectSprite.setRetreating(true);
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

        threadContext.addButton("take()", () -> {
                    // If there are waiting objects, don't create a new sprite
                    ObjectSprite objectSprite = threadContext.getFirstWaitingObjectSprite();
                    int ypos = getGetterYPos();

                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                getter.setYPosition(ypos);
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
    /**
     * If there are no getters, returns the initial
     * otherwise returns the bottom one plus delta
     * @return
     */
    private int getGetterYPos() {
        List<GetterThreadSprite> getters = threadContext.getAllGetterThreadSprites();
        int next = getters.stream().mapToInt(Sprite::getYPosition).max().orElse(initialGetterYPos);
        return next + GETTER_DELTA;
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("TransferQueue");
        setSnippetFile("transfer-queue.html");
        transferQueue = new LinkedTransferQueue();
    }
}
