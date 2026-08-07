package com.vgrazi.jca.util;

import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.sprites.ThreadSprite;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Logging {
    static Map<Sprite, String> messageCache = new HashMap<>();
    static private long stepDelay = 2000;
    static Logger logger = Logger.getLogger("Logging");

    public static void logAndSleep(long delay, String message) throws InterruptedException {
        Thread.sleep(delay);
        println(message);
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
            log(newMessage);
        }
    }

    public static void log(Sprite sprite) {
        String message = sprite.toString();
        String cachedMessage = messageCache.get(sprite);
        if (cachedMessage == null || !cachedMessage.equals(message)) {
            messageCache.put(sprite, message);
            println(message);
        }
    }

    public static void log(String message) {
        println(message);
    }

    public static void println(String message) {
        logger.info(message);
    }
}
