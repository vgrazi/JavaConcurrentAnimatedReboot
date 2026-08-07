package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class CountDownLatchSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${monolith-right-border}")
    private int rightBorder;

    private CountDownLatch countDownLatch = new CountDownLatch(4);
    private int totalCount = 4;

    public void run() {
        reset();
        threadContext.addButton("await()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.attachAndStartRunnable(() -> {
                try {
                    highlightSnippet(1);
                    countDownLatch.await();
                    threadContext.stopThread(sprite);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    sprite.setMessage(e);
                    sprite.setRetreating();
                }
            }, true);
            threadContext.addSprite(sprite);
        });
        threadContext.addButton("await(time, TimeUnit)", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.attachAndStartRunnable(() -> {
                boolean success;
                try {
                    highlightSnippet(3);
                    success = countDownLatch.await(3, TimeUnit.SECONDS);
                    if(!success) {
                        sprite.setRetreating();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    sprite.setRetreating();
                    sprite.setMessage(e);
                } finally {
//                    threadContext.stopThread(sprite);
                }
            }, true);
            threadContext.addSprite(sprite);
        });
        threadContext.addButton("countDown()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.attachAndStartRunnable(() -> {
                highlightSnippet(2);
                countDownLatch.countDown();
                while(countDownLatch.getCount() >0 && sprite.getXPosition() < rightBorder-10) {
                    Thread.yield();
                }
                threadContext.stopThread(sprite);
            }, true);
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("interrupt", () -> {
            highlightSnippet(2);
            ThreadSprite sprite=threadContext.getFirstWaitingThread();
            if(sprite != null) {
                sprite.getThread().interrupt();
            }
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        threadCanvas.setThinMonolith();
        threadContext.setSlideLabel("CountDownLatch");
        setSnippetFile("countdown-latch.html");
        setImage("images/countdownLatch.jpg", .7f);
        countDownLatch = new CountDownLatch(totalCount);
    }
}
