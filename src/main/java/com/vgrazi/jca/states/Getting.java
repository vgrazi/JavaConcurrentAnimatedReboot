package com.vgrazi.jca.states;

import com.vgrazi.jca.context.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class Getting extends ThreadState {
    @Autowired
    ThreadContext threadContext;

    @Value("${monolith-right-border}")
    private int leftBorder;

    @Value("${arrow-length}")
    private int arrowLength;
    @Override
    public void advancePosition(Sprite sprite) {
        sprite.setXPosition(leftBorder + arrowLength);
    }
}
