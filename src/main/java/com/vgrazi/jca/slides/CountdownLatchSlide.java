package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class CountdownLatchSlide extends Slide {

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
                    countDownLatch.await();
                    threadContext.stopThread(sprite);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threadContext.addSprite(sprite);
        });
        threadContext.addButton("countDown()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.attachAndStartRunnable(() -> {
                countDownLatch.countDown();
                while(countDownLatch.getCount() >0 && sprite.getXPosition() < rightBorder-10) {
                    Thread.yield();
                }
                threadContext.stopThread(sprite);
            });
            threadContext.addSprite(sprite);
        });
        ;

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("CountdownLatch");
        countDownLatch = new CountDownLatch(totalCount);
    }
}
