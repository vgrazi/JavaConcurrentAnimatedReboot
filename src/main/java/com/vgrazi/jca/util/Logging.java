package com.vgrazi.jca.util;

import com.vgrazi.jca.ThreadSprite;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Logging {
    static Map<ThreadSprite, String> messageCache = new HashMap<>();
    static private long stepDelay = 2000;

    public static void sleepAndLog(long delay, String message) throws InterruptedException {
        Thread.sleep(delay);
        System.out.println(LocalTime.now() + " " + message);
    }

    public static void sleepAndLog(String message, ThreadSprite sprite) {
        sleepAndLog(stepDelay, message, sprite);
    }

    public static void sleepAndLog(long stepDelay, String message, ThreadSprite sprite) {
        try {
            Thread.sleep(stepDelay);
            String cachedMessage = messageCache.get(sprite);
            if (cachedMessage == null || !cachedMessage.equals(message)) {
                messageCache.put(sprite, message);
                String newMessage = message + " " + sprite;
                System.out.println(LocalTime.now() + " " + newMessage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepAndLog(String message) throws InterruptedException {
        sleepAndLog(stepDelay, message);
    }
}
