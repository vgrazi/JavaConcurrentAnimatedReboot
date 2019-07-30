package com.vgrazi.jca.states;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Terminated extends State {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(ThreadSprite thread) {
        thread.setXPosition(thread.getXPosition() + threadContext.pixelsPerStep);
    }
}
