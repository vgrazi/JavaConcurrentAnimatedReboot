package com.vgrazi.jca;

import com.vgrazi.jca.slides.PhaserSlide;
import com.vgrazi.jca.slides.ReadWriteLockSlide;
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

    @Autowired()
    SynchronizedSlide synchronizedSlide;

    @Autowired
    ReadWriteLockSlide readWriteLockSlide;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PhaserSlide phaserSlide;

    @Override
    public void run(String... args) throws InterruptedException {
        new Thread(() -> {
            try {
                threadContext.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        switch (args[0]) {
            case "synchronized":
                synchronizedSlide.run();
                break;
            case "read-write-lock":
                readWriteLockSlide.run();
                break;
            case "phaser":
                phaserSlide.run();
                break;
        }

    }
}
