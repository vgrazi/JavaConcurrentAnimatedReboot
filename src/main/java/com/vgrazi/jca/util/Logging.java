package com.vgrazi.jca.util;

import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Logging {
    static Map<Sprite, String> messageCache = new HashMap<>();
    static private long stepDelay = 2000;

    public static void logAndSleep(long delay, String message) throws InterruptedException {
        Thread.sleep(delay);
        System.out.println(LocalTime.now() + " " + message);
    }

    public static void logAndSleep(String message, ThreadSprite sprite) {
        logAndSleep(stepDelay, message, sprite);
    }

    public static void logAndSleep(String message) throws InterruptedException {
        logAndSleep(stepDelay, message);
    }

    public static void logAndSleep(long stepDelay, String message, ThreadSprite sprite) {
        try {
            message(message, sprite);
            Thread.sleep(stepDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message, Sprite sprite) {
        message(message, sprite);
    }

    private static void message(String message, Sprite sprite) {
        String cachedMessage = messageCache.get(sprite);
        if (cachedMessage == null || !cachedMessage.equals(message)) {
            messageCache.put(sprite, message);
            String newMessage = message + " " + sprite;
            log(LocalTime.now() + " " + newMessage);
        }
    }

    public static void log(Sprite sprite) {
        String message = sprite.toString();
        String cachedMessage = messageCache.get(sprite);
        if (cachedMessage == null || !cachedMessage.equals(message)) {
            messageCache.put(sprite, message);
            System.out.println(LocalTime.now() + " " + message);
        }
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
