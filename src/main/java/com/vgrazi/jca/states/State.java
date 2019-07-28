package com.vgrazi.jca.states;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Represents all of the supported thread states, and contains the algorithms for calculating the next position,
 * depending on the current position. For example, the Runnable state will check the position, and if to the left of the
 * monolith, will advance the thread to the right
 */
public abstract class State {

    /**
     * Based on the current state, calculates the next position of the thread
     * The state is automatically returned by ThreadSprite.getState(), based on the thread's state
     * @param thread
     * @return
     */
    public abstract int storeNextPosition(ThreadSprite thread);
    @Autowired
    protected ThreadContext threadContext;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static Blocked blocked = new Blocked();
    public static Running runnable = new Running();
    public static Waiting waiting = new Waiting();
    public static Stopped terminated = new Stopped();
}
