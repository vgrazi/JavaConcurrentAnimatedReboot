package com.vgrazi.jca;

import com.vgrazi.jca.context.FutureSprite;
import com.vgrazi.jca.context.GetterThreadSprite;
import com.vgrazi.jca.context.ThreadSprite;
import com.vgrazi.jca.states.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

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
    @Scope("prototype")
    GetterThreadSprite getterSprite() {
        return new GetterThreadSprite();
    }

    @Bean
    @Scope("prototype")
    FutureSprite futureSprite() {
        return new FutureSprite();
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

    @Bean
    public Getting getting () {
        return new Getting();
    }
}
