package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Retreating extends ThreadState {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(Sprite sprite) {
        ThreadSprite threadSprite = (ThreadSprite) sprite;
        RelativePosition relativePosition = threadSprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                if (threadSprite.getDirection() != Sprite.Direction.left) {
                    calculateNextPositionBefore(threadSprite);
                }
                else {
                    calculatePreviousPosition(threadSprite);
                }
                break;
            case At:
            case In:
            case After:
                threadSprite.setDirection(Sprite.Direction.left);
                calculatePreviousPosition(threadSprite);
                break;
        }
    }

}
