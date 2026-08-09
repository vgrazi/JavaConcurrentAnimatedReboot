package com.vgrazi.jca;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class JavaConcurrentAnimatedRebootApplication {

    public static void main(String[] args) {
        configureVirtualThreadSchedulerLimits();
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s%n");
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JavaConcurrentAnimatedRebootApplication.class);
        builder.headless(false).run(args);
    }

    private static void configureVirtualThreadSchedulerLimits() {
        // Allow explicit JVM -D settings to win; otherwise use a conservative default for demos.
        if (System.getProperty("jdk.virtualThreadScheduler.parallelism") == null) {
            int defaultParallelism = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors()));
            System.setProperty("jdk.virtualThreadScheduler.parallelism", String.valueOf(defaultParallelism));
        }
        if (System.getProperty("jdk.virtualThreadScheduler.maxPoolSize") == null) {
            int parallelism = Integer.parseInt(System.getProperty("jdk.virtualThreadScheduler.parallelism"));
            System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", String.valueOf(parallelism));
        }
    }
}
