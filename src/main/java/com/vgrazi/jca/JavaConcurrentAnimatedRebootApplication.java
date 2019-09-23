package com.vgrazi.jca;

import com.vgrazi.jca.context.ThreadSprite;
import com.vgrazi.jca.states.Terminated;
import com.vgrazi.jca.states.Waiting;
import com.vgrazi.jca.states.Blocked;
import com.vgrazi.jca.states.Running;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.swing.*;

@SpringBootApplication
public class JavaConcurrentAnimatedRebootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaConcurrentAnimatedRebootApplication.class, args);
    }

    @Bean
    @Scope("prototype")
    ThreadSprite threadSprite() {
        return new ThreadSprite();
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
    public Waiting waiting () {
        return new Waiting();
    }
}
