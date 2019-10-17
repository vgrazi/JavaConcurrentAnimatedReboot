package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * All slides extend this class
 */
public abstract class Slide {
    @Autowired
    protected ThreadContext threadContext;

    @Autowired
    protected ThreadCanvas threadCanvas;
    public abstract void run();

    protected void reset() {
        threadContext.reset();
        threadCanvas.hideMonolith(false);
    }
}
