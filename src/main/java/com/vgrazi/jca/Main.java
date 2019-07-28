package com.vgrazi.jca;

import com.vgrazi.jca.slides.Slide;
import com.vgrazi.jca.slides.SynchronizedSlide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Runs the Slide selected from the menu
 */
@Component
public class Main implements CommandLineRunner {
    @Autowired
    private ThreadContext threadContext;

    @Autowired
    SynchronizedSlide synchronizedSlide;

    @Autowired
    ApplicationContext applicationContext;
    @Override
    public void run(String... args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    threadContext.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
        synchronizedSlide.run();

    }
}
