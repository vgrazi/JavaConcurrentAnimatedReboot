package com.vgrazi.jca.config;

import com.vgrazi.jca.sprites.*;
import com.vgrazi.jca.states.*;
import com.vgrazi.jca.util.Parsers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.swing.*;
import java.awt.*;

@Configuration
public class Config {

    @Bean
    @Scope("prototype")
    ThreadSprite threadSprite() {
        return new ThreadSprite();
    }

    @Bean
    @Scope("prototype")
    ObjectSprite objectSprite() {
        return new ObjectSprite();
    }

    @Bean
    @Scope("prototype")
    GetterThreadSprite getterSprite() {
        return new GetterThreadSprite();
    }

    @Bean
    @Scope("prototype")
    WriteThreadSprite writeThreadSprite() {
        return new WriteThreadSprite();
    }

    @Bean
    @Scope("prototype")
    CompletableFutureSprite completableFutureSprite() {
        return new CompletableFutureSprite();
    }

    @Bean
    @Scope("prototype")
    RunnerThreadSprite runnerThreadSprite() {
        return new RunnerThreadSprite();
    }

    @Bean
    @Scope("prototype")
    PooledThreadSprite pooledThreadSprite() {
        return new PooledThreadSprite();
    }

    @Bean
    @Scope("prototype")
    RunnableSprite runnableSprite() {
        return new RunnableSprite();
    }

    @Bean
    @Scope("prototype")
    FutureRunnableSprite futureRunnableSprite() {
        return new FutureRunnableSprite();
    }

    @Bean
    public Blocked blocked() {
        return new Blocked();
    }

    @Bean
    public Running running() {
        return new Running();
    }

    @Bean
    public Terminated terminated() {
        return new Terminated();
    }

    @Bean
    public Waiting waiting() {
        return new Waiting();
    }

    @Bean
    public Getting getting() {
        return new Getting();
    }

    @Bean
    public Retreating retreating() {
        return new Retreating();
    }

    @Bean
    public Stroke basicStroke() {
        return new BasicStroke(4);
    }

    @Bean Stroke writerStroke() {
        return new BasicStroke(3.0f, // Width
                BasicStroke.CAP_SQUARE, // End cap
                BasicStroke.JOIN_MITER, // Join style
                10.0f, // Miter limit
                new float[]{2.0f, 4.0f}, // Dash pattern
                0.0f);
    }

    @Bean
    public Stroke writerHeadStroke() {
        return new BasicStroke(1);
    }

    @Bean
    public Stroke dottedStroke() {
        return new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);
    }

    @Bean
    public Stroke dottedStroke1() {
        return new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 10);
    }

    @Bean
    public Stroke dottedStroke2() {
        return new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 20);
    }

    @Bean
    public JLabel messages() {
        return new JLabel();
    }

    @Bean
    /**
     * This is the card panel that flips to display
     * the animation or the slide intro
     */
    public JPanel cardPanel() {
        JPanel cardPanel = new JPanel(new CardLayout());
        return cardPanel;
    }

    @Bean
    public JLabel imageLabel() {
        JLabel imageLabel = new JLabel();
        imageLabel.setBackground(Color.white);
        return imageLabel;
    }


    @Value("${message-font}")
    public void setMessageFont(String messageFont) {
        Font font = Parsers.parseFont(messageFont);
        messages().setFont(font);
    }

}
