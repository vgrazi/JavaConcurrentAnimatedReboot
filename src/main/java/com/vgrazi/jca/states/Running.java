package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Running extends State {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(ThreadSprite sprite) {
        RelativePosition relativePosition = sprite.getRelativePosition();
//        System.out.println(sprite + " " + relativePosition);
        switch (relativePosition) {
            case Before:
                calculateNextPositionBefore(sprite);
                break;
            case At:
                break;
            case In:
                calculateNextPositionIn(sprite);
                break;
            case After:
                calculateNextPositionAfter(sprite);
                break;
        }
    }

}
