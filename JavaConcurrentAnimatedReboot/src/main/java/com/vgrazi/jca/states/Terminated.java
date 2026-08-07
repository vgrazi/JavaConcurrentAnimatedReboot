package com.vgrazi.jca.states;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.ObjectSprite;
import com.vgrazi.jca.sprites.Sprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Terminated extends ThreadState {
    @Autowired
    private ThreadContext threadContext;

    @Value("${monolith-right-border}")
    private int monolithRightBorder;

    @Override
    public void advancePosition(Sprite sprite) {
        // if the thread is terminated, we want to get it out of
        // the monolith as fast as possible, so it does not
        // appear to be occupying the lock at the same time as some other
        // runnable thread
        int xPosition = sprite.getXPosition();
        if (!(sprite instanceof ObjectSprite)) {
            if(xPosition < monolithRightBorder) {
                xPosition = monolithRightBorder;
            }
        }
        // Some sprites, eg ReadWriteLock the direction is important - we don't want the arrow point left, when the sprite is moving right!
        if(sprite.getDirection() == Sprite.Direction.left) {
            sprite.setDirection(Sprite.Direction.right);
        }
        sprite.setXPosition(xPosition + threadContext.pixelsPerStep);
    }
}
