package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ObjectSprite;
import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

@Component
public class TransferQueueSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    private TransferQueue transferQueue = new LinkedTransferQueue();
    public void run() {
        reset();
        threadContext.addButton("transfer()", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.transfer("xxx");
                    threadContext.stopThread(objectSprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threadContext.addSprite(objectSprite);
        });
        threadContext.addButton("tryTransfer()", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.tryTransfer("xxx", 5000 , TimeUnit.MILLISECONDS);
                    threadContext.stopThread(objectSprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threadContext.addSprite(objectSprite);
        });

        threadContext.addButton("take()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("getterSprite");
            sprite.attachAndStartRunnable(() -> {
                try {
                    transferQueue.take();
                    threadContext.stopThread(sprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    protected void reset() {
        super.reset();
        transferQueue = new LinkedTransferQueue();
    }
}
