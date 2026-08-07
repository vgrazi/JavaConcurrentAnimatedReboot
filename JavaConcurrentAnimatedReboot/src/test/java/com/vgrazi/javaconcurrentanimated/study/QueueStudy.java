package com.vgrazi.javaconcurrentanimated.study;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class QueueStudy {
    private static Logger logger = Logger.getLogger("QueueStudy");
    private static void println(Object message) {
        logger.info(String.valueOf(message));
    }

    public static void main(String[] args) {
        Queue<Long> queue = new LinkedList<>();
        queue.offer(1L);
        queue.offer(2L);
        queue.offer(3L);
        queue.offer(4L);
        queue.offer(5L);

        while (!queue.isEmpty()) {
            Long poll = queue.poll();
            println(poll);
        }
    }
}
