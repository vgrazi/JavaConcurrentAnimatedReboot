package com.vgrazi.jca.states;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;

public class Running extends State {
    @Autowired
    ThreadContext threadContext;

    @Override
    public int storeNextPosition(ThreadSprite thread) {
        return 0;
    }
}
