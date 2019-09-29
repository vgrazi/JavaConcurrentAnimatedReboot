package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

public class Running extends ThreadState {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(Sprite sprite) {
        RelativePosition relativePosition = sprite.getRelativePosition();
//        System.out.println(sprite + " " + relativePosition);
        switch (relativePosition) {
            case Before:
                calculateNextPositionBefore(sprite);
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
