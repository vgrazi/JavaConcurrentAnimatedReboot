package com.vgrazi.jca.states;

import com.vgrazi.jca.RelativePosition;
import com.vgrazi.jca.ThreadSprite;

public class Blocked extends State {

    @Override
    public void advancePosition(ThreadSprite sprite) {
        int position = sprite.getXPosition();
        RelativePosition relativePosition = sprite.getRelativePosition();
        switch (relativePosition) {
            case Before:
                position += threadContext.pixelsPerStep;
                if(position > threadContext.monolithLeftBorder) {
                    position = threadContext.monolithLeftBorder;
                }
                sprite.setXPosition(position);
                break;
            case At:
                break;
            case In:
                break;
            case After:
                break;
        }
    }
}
