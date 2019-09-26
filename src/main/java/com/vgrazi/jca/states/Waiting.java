package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Waiting extends State implements InitializingBean {
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
    public void advancePosition(ThreadSprite sprite) {
        RelativePosition relativePosition = sprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                calculateNextPositionBefore(sprite);
                break;
            case At:
            case In:
                // todo: only stop when it arrives at the waitingXPos
                sprite.setXPosition(waitingXPos);
                break;
            case After:
                // nothing to do, this thread is waiting
                break;
        }
    }

    @Override
    public void afterPropertiesSet() {
        waitingXPos = (monolithLeftBorder + monolithRightBorder + arrowLength)/2;
    }
}
