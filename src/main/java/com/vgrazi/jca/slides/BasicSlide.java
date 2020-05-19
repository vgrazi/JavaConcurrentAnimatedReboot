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
        threadContext.addButton("Add running thread", () -> {
            // create a new sprite
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            // give it an action tag to test for
            sprite.setAction("someRunningTag");
            // always attach a runnable, and then start the thread
            sprite.attachAndStartRunnable(()->{
                while(sprite.getAction().equals("someRunningTag")) {
                    // Even tho it is consuming CPU, we need to leave it running so as not to change the thread state
                    // in any case, let it yield to running threads
                    Thread.yield();
                }
                threadContext.stopThread(sprite);
            });
            // Always add the sprite to the thread context.
            threadContext.addSprite(sprite);
        });
        threadContext.addButton("Stop thread", () -> {
            // get a running thread, if any
            ThreadSprite sprite = threadContext.getRunningThread();
            // stop it
            if(sprite != null) {
                // set it to anything except "someRunningTag" so it will exit the loop above
                sprite.setAction("done");
            }
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }


    public void reset() {
        super.reset();
        threadContext.setSlideLabel("Basic Slide");
        setSnippetFile("some.html");
    }
}
