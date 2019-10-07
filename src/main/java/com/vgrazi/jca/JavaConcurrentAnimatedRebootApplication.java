package com.vgrazi.jca;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class JavaConcurrentAnimatedRebootApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JavaConcurrentAnimatedRebootApplication.class);
        builder.headless(false).run(args);
    }
}
