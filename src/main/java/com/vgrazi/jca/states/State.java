package com.vgrazi.jca.states;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Represents all of the supported thread states, and contains the algorithms for calculating the next position,
 * depending on the current position. For example, the Runnable state will check the position, and if to the left of the
 * monolith, will advance the thread to the right
 */
@Component
public class State {

    /**
     * Based on the current state, calculates the next position of the thread
     * The state is automatically returned by ThreadSprite.getState(), based on the thread's state
     *
     */
    public void advancePosition(ThreadSprite thread){}

    @Autowired
    protected ThreadContext threadContext;
    @Value("${monolith-left-border}")
    public int monolithLeftBorder;
    @Value("${monolith-right-border}")
    public int monolithRightBorder;
    @Value("${pixels-per-step}")
    private int pixelsPerStep;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Given a sprite before the monolith, calculates the next position
     * and stores it in the sprite
     */
    void calculateNextPositionBefore(ThreadSprite sprite) {
        int position = sprite.getXPosition();
        position += pixelsPerStep;
        if (position > monolithLeftBorder) {
            position = monolithLeftBorder;
        }
        sprite.setXPosition(position);
    }

    /**
     * Given a sprite inside the monolith, calculates the next position
     * and stores it in the sprite
     */
    void calculateNextPositionIn(ThreadSprite sprite) {
        int position = sprite.getXPosition();
        ThreadSprite.Direction direction = sprite.getDirection();
        switch (direction) {
            case right:
                position += pixelsPerStep;
                if (position > monolithRightBorder - 5) {
                    position = monolithRightBorder - 5;
                    // todo: build the rotational animation here
                    sprite.setXPosition(position);
                    sprite.setDirection(ThreadSprite.Direction.left);
                } else {
                    sprite.setXPosition(position);
                }
                break;
            case left:
                position -= pixelsPerStep;
                if (position < monolithLeftBorder + pixelsPerStep*2) {
                    position = monolithLeftBorder + pixelsPerStep*2;
                    // todo: build the rotational animation here
                    sprite.setXPosition(position);
                    sprite.setDirection(ThreadSprite.Direction.right);
                } else {
                    sprite.setXPosition(position);
                }
                break;
        }
    }

    void calculateNextPositionAfter(ThreadSprite sprite) {
        sprite.setXPosition(sprite.getXPosition() + threadContext.pixelsPerStep);
    }
}
