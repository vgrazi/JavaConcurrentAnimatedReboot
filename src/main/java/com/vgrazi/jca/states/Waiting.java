package com.vgrazi.jca.states;

import com.vgrazi.jca.RelativePosition;
import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Waiting extends State {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(ThreadSprite sprite) {
        RelativePosition relativePosition = sprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                calculateNextPositionBefore(sprite);
                break;
            case At:
            case In:
            case After:
                // nothing to do, this thread is waiting
                break;
        }
    }
}
