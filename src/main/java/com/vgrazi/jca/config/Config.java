package com.vgrazi.jca.config;

import com.vgrazi.jca.sprites.*;
import com.vgrazi.jca.states.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
    FutureSprite futureSprite() {
        return new FutureSprite();
    }

    @Bean
    @Scope("prototype")
    RunnerThreadSprite runnerThreadSprite() {
        return new RunnerThreadSprite();
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
        return new BasicStroke(3);
    }

    @Bean
    public Stroke dottedStroke() {
        return new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);
    }

    @Bean
    public Stroke dottedStroke1() {
        return new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 10);
    }

    @Bean
    public Stroke dottedStroke2() {
        return new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 20);
    }

}
