package com.vgrazi.jca.states;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Terminated extends ThreadState {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(ThreadSprite thread) {
        thread.setXPosition(thread.getXPosition() + threadContext.pixelsPerStep);
    }
}
