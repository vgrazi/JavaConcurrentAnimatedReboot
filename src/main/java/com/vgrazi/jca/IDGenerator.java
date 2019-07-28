package com.vgrazi.jca;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique IDs beginning from 1
 */
public class IDGenerator {
    private static AtomicInteger ID = new AtomicInteger(1);
    public static int next() {
        return ID.getAndIncrement();
    }
}
