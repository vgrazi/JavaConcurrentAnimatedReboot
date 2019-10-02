package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;

import java.awt.*;

/**
 * This sprite renders a "getter" thread - a thread that called a potentially blocking get method
 */
public class GetterThreadSprite extends ThreadSprite {
    public GetterThreadSprite() {

    }

    protected ThreadState getState() {
        Thread thread = getThread();
        if (thread == null) {
            return null;
        }
        switch (thread.getState()) {
            case NEW:
            case WAITING:
            case TIMED_WAITING:
            case BLOCKED:
                return getThreadContext().getting;
            case RUNNABLE:
                return getThreadContext().getting;
            case TERMINATED:
                return getThreadContext().terminated;
            default:
                throw new IllegalArgumentException("Unknown thread state " + thread.getState());
        }
    }

    /**
     * Draws the ball at the end of the thread
     */
    protected void drawBall(Graphics2D graphics) {
        graphics.fillOval(getXPosition() - 6 - arrowLength, getYPosition() - 5, 10, 10);
    }


    @Override
    protected void setNextXPosition() {
        ThreadState state = getState();
        // todo: center this in our future
        state.advancePosition(this);
    }
}
