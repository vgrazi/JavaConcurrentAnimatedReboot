package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AtomicIntegerSlide extends Slide {

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${monolith-left-border}")
    private int leftBorder;

    @Autowired
    private ApplicationContext applicationContext;

    private AtomicInteger counter = new AtomicInteger();
    public void run() {
        reset();
        threadContext.addButton("getAndIncrement()", () -> {
            for (int i = 0; i < 5; i++) {
                ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
                sprite.attachAndStartRunnable(()-> {
                    int count = counter.getAndIncrement();
                    while(true) {
                        Thread.yield();
                        if(sprite.getXPosition() >= leftBorder -20) {
                            sprite.setMessage(String.valueOf(count));
                        }

                        if(sprite.getXPosition() >= rightBorder -20) {
                            threadContext.stopThread(sprite);
                            break;
                        }
                    }
                });
                threadContext.addSprite(sprite);
            }
        });
        ;

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        counter = new AtomicInteger();
        threadContext.setSlideLabel("AtomicInteger");
    }
    public void ping(){

    }
}
