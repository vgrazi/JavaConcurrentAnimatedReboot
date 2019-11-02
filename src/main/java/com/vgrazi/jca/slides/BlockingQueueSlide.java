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

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    @Autowired
    private ApplicationContext applicationContext;

    private BlockingQueue blockingQueue;

    public void run() {
        reset();
        threadContext.addButton("offer()", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.setAction("running");
            threadContext.addSprite(objectSprite);
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(leftBorder);
                objectSprite.setAction("exit");
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    blockingQueue.put("xxx");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                while("running".equals(objectSprite.getAction())){
                    Thread.yield();
                }
                threadContext.stopThread(objectSprite);
                if (getter != null) {
                    threadContext.stopThread(getter);
                }
            });
        });
        threadContext.addButton("offer(obj, 5, TimeUnit.SECONDS)", () -> {
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.setAction("running");
            GetterThreadSprite getter = threadContext.getFirstGetterThreadSprite();
            if (getter != null) {
                objectSprite.setYPosition(getter.getYPosition());
                objectSprite.setXPosition(leftBorder);
                objectSprite.setAction("exit");
            }
            objectSprite.attachAndStartRunnable(() -> {
                try {
                    boolean success = blockingQueue.offer("xxx", 5, TimeUnit.SECONDS);
                    if(!success) objectSprite.setRetreating(true);
//                    threadContext.stopThread(objectSprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                while("running".equals(objectSprite.getAction())){
                    Thread.yield();
                }
                threadContext.stopThread(objectSprite);
                if (getter != null) {
                    threadContext.stopThread(getter);
                }
            });
            threadContext.addSprite(objectSprite);
        });

        threadContext.addButton("take()", () -> {
                    // If there are waiting objects, don't create a new sprite
                    ObjectSprite objectSprite = threadContext.getFirstRunningObjectSprite();
                    ThreadSprite getter = (ThreadSprite) applicationContext.getBean("getterSprite");
                    if (objectSprite == null) {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                blockingQueue.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            threadContext.stopThread(getter);
                        });
                    } else {
                        getter.attachAndStartRunnable(() -> {
                            try {
                                getter.setYPosition(objectSprite.getYPosition());
                                blockingQueue.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            objectSprite.setAction("done");
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
        threadContext.setSlideLabel("BlockingQueue");
        blockingQueue  = new ArrayBlockingQueue(1);
    }
}
