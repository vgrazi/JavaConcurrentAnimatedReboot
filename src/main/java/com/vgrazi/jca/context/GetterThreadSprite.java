package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;

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

    @Override
    protected void setNextXPosition() {
        ThreadState state = getState();
        state.advancePosition(this);
    }
}
