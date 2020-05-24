package com.vgrazi.jca.slides;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class IntroSlide extends Slide {

    @Autowired
    private JPanel cardPanel;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    public void run() {
        reset();

        threadContext.addButton("reset()", this::reset);
        threadContext.setVisible();
        // give it a few cycles to load the graphic
        executor.schedule(this::reset, 100, TimeUnit.MILLISECONDS);
    }
    @Override
    public void reset() {
        super.reset();
        resetImage();
        ((CardLayout) cardPanel.getLayout()).next(cardPanel);
    }

    public void resetImage() {
        setImage("images/concurrentText.jpg");
    }
}
