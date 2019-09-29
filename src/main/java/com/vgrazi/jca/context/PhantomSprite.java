package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;

import java.awt.*;

/**
 * This sprite doesn't actually render anything
 * It just acts as a holder for some action
 */
public class PhantomSprite extends Sprite{
    @Override
    protected void setNextXPosition() {

    }

    @Override
    public void render(Graphics2D graphics) {

    }
}
