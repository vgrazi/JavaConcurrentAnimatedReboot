package com.vgrazi.jca.states;

import com.vgrazi.jca.sprites.Sprite;

public interface State {
    /**
     * Based on the current state, calculates the next position of the sprite
     * For example, for Threads, the state is automatically returned by ThreadSprite.getState(), based on the sprite's state
     */
    void advancePosition(Sprite sprite);
}
