package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Pooled extends ThreadState {
    @Autowired
    ThreadContext threadContext;
    @Override
    public void advancePosition(Sprite sprite) {
        ThreadSprite threadSprite = (ThreadSprite) sprite;
        RelativePosition relativePosition = threadSprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                if (threadSprite.getDirection() == Sprite.Direction.right) {
                    threadSprite.setXPosition(monolithLeftBorder + 10);
                }
                else {
                    calculatePreviousPosition(threadSprite);
                }
                break;
            case At:
            case In:
                calculateNextPositionIn(sprite);
                break;
            case After:
                calculateNextPositionAfter(sprite);
                break;
        }
    }
}
