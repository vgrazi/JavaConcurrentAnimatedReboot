package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.states.ThreadState;

import java.awt.*;

/**
 * This sprite renders a "getter" thread - a thread that called a potentially blocking get method
 */
public class GetterThreadSprite<S> extends ThreadSprite<S> {
    public GetterThreadSprite() {

    }

    public ThreadState getState() {
        Thread thread = getThread();
        if (thread == null) {
            return null;
        }
        switch (thread.getState()) {
            case NEW:
            case WAITING:
            case TIMED_WAITING:
            case BLOCKED:
            case RUNNABLE:
                return getThreadContext().getting;
            case TERMINATED:
                if (getRelativePosition() == RelativePosition.After)
                    return getThreadContext().terminated;
                else return getThreadContext().getting;
            default:
                throw new IllegalArgumentException("Unknown thread state " + thread.getState());
        }
    }

    @Override
    protected int getNextYPositionFromContext() {
        return yPosition;
    }

    /**
     * Draws the ball at the end of the thread
     */
    protected void drawThreadCap(Graphics2D graphics) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        graphics.fillOval(getXPosition() - 6 - arrowLength, getYPosition() - 5, 10, 10);
    }


    @Override
    public void setNextXPosition() {
        ThreadState state = getState();
        // todo: center this in our future
        state.advancePosition(this);
    }
}
