package com.vgrazi.jca;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import com.vgrazi.jca.slides.SynchronizedSlide;
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
}
