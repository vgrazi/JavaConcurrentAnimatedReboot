package com.vgrazi.jca.states;

import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

public class Terminated extends ThreadState {
    @Autowired
    ThreadContext threadContext;

    @Override
    public void advancePosition(Sprite sprite) {
        sprite.setXPosition(sprite.getXPosition() + threadContext.pixelsPerStep);
    }
}
