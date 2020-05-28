package com.vgrazi.javaconcurrentanimated.study;

import java.util.LinkedList;
import java.util.Queue;

public class QueueStudy {
    public static void main(String[] args) {
        Queue<Long> queue = new LinkedList<>();
        queue.offer(1L);
        queue.offer(2L);
        queue.offer(3L);
        queue.offer(4L);
        queue.offer(5L);

        while (!queue.isEmpty()) {
            Long poll = queue.poll();
            System.out.println(poll);
        }
    }
}
