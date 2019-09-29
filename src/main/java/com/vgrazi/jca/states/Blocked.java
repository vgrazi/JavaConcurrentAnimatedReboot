package com.vgrazi.jca.states;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.Sprite;

public class Blocked extends ThreadState {

    @Override
    public void advancePosition(Sprite sprite) {
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
            case After:
            case In:
                break;
        }
    }
}
