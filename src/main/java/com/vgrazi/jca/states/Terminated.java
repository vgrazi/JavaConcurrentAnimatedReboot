package com.vgrazi.jca.states;

import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Terminated extends ThreadState {
    @Autowired
    private ThreadContext threadContext;

    @Value("${monolith-right-border}")
    private int monolithRightBorder;

    @Override
    public void advancePosition(Sprite sprite) {
        // if the sprite is terminated, we want to get it out of
        // the monolith as fast as possible, so it does not
        // appear to be occupying the lock at the same time as some other
        // runnable thread
        int xPosition = sprite.getXPosition();
        if(xPosition < monolithRightBorder) {
            xPosition = monolithRightBorder;
        }
        sprite.setXPosition(xPosition + threadContext.pixelsPerStep);
    }
}
