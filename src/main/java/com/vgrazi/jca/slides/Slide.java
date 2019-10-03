package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * All slides extend this class
 */
public abstract class Slide {
    @Autowired
    protected ThreadContext threadContext;
    public abstract void run();

    protected void reset() {
        threadContext.reset();
    }
}
