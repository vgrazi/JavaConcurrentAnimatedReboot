package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Waiting extends ThreadState implements InitializingBean {
    @Autowired
    ThreadContext threadContext;
    @Value("${monolith-left-border}")
    private int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${arrow-length}")
    private int arrowLength;

    private int waitingXPos;

    @Override
    public void advancePosition(Sprite sprite) {
        RelativePosition relativePosition = sprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                calculateNextPositionBefore(sprite);
                break;
            case At:
            case After:
                // nothing to do, this thread is waiting
                break;
            case In:
                // if sprite hasn't reached its final resting place, let it keep inching forward. (If it is already backing up to the left, just force it to its resting position)
                if (sprite.getDirection() == Sprite.Direction.left || sprite.getXPosition() + arrowLength >= waitingXPos) {
                    sprite.setXPosition(waitingXPos);
                }
                else {
                    calculateNextPositionIn(sprite);
                }
                break;
        }
    }


    @Override
    public void afterPropertiesSet() {
        waitingXPos = (monolithLeftBorder + monolithRightBorder + arrowLength) / 2;
    }
}
