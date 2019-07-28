package com.vgrazi.jca.states;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public abstract class State {
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
