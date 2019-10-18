package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.view.SnippetCanvas;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * All slides extend this class
 */
public abstract class Slide {
    @Autowired
    protected ThreadContext threadContext;

    @Value("${selected-font-color-style}")
    protected String selectedFontColorStyle;

    @Value("${deselected-font-color-style}")
    protected String deselectedFontColorStyle;

    @Autowired
    protected ThreadCanvas threadCanvas;

    @Autowired
    protected SnippetCanvas snippetCanvas;

    private Set<String> styleSelectors;

    public abstract void run();

    /**
     * Sets all of the css style selectors, extracted from the snippet file
     */
    protected void setStyleSelectors(Set<String> styleSelectors) {
        this.styleSelectors = styleSelectors;
    }

    /**
     * Sets the specified selectors as selected, and everything else unselected
     */
    protected void setCssSelected(Set<String> selectors) {
        styleSelectors.forEach(selector-> {
            if(selectors.contains(selector)) {
                threadContext.addStyleRule("." + selector + selectedFontColorStyle);
            }
            else {
                threadContext.addStyleRule("." + selector + deselectedFontColorStyle);
            }
        });
        snippetCanvas.applyStyles();
    }

    /**
     * Sets the specified selector as selected, and everything else unselected
     */
    protected void setCssSelected(String selectedSelector) {
        styleSelectors.forEach(selector-> {
            if(selectedSelector.equals(selector)) {
                threadContext.addStyleRule("." + selector + selectedFontColorStyle);
            }
            else {
                threadContext.addStyleRule("." + selector + deselectedFontColorStyle);
            }
        });
        snippetCanvas.applyStyles();
    }

    protected void reset() {
        threadContext.reset();
        threadCanvas.hideMonolith(false);
    }
}
