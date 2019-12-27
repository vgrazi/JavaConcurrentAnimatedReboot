package com.vgrazi.jca.slides;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

@Component
public class CompletionServiceSlide extends Slide {
    private CompletionService completionService;
    @Override
    public void run() {
        reset();
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("CompletionService");
        completionService = new ExecutorCompletionService(Executors.newFixedThreadPool(4));
        setSnippetFile("completion-service.html");
    }

}
