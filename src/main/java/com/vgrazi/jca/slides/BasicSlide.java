package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Use this as a starting point for constructing new slides. To use, uncomment the addButton for basicSlide in JCAFrame
 */
@Component
public class BasicSlide extends Slide {

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${monolith-left-border}")
    private int leftBorder;

    private ApplicationContext applicationContext;

    public BasicSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        reset();
        threadContext.addButton("Add thread", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.attachAndStartRunnable(()->{
                while(true) {
                    Thread.yield();
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }


    public void reset() {
        super.reset();
        threadContext.setSlideLabel("Basic Slide");
//        setSnippetFile("some.html");
    }
}
