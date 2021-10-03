package com.vgrazi.javaconcurrentanimated.study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;
import java.util.stream.IntStream;


public class ExchangerStudy {
    static Logger logger = LoggerFactory.getLogger(ExchangerStudy.class);
    final static Random random = new Random();
    public static void main(String[] args) {
        new ExchangerStudy().launch();

        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setVisible(true);
    }

    private void launch() {
        Exchanger<Integer> exchanger = new Exchanger<>();
        runIt(exchanger, 0);
        runIt(exchanger, 1);
    }

    private void runIt(Exchanger<Integer> exchanger, int seed) {
        CompletableFuture.runAsync(()-> IntStream.iterate(seed, i -> i + 2).forEach(i ->
        {
            try {
                if (seed == 0) {
                    int timeout = 1000 + random.nextInt(1000);
                    synchronized (this) {
                        logger.info(String.format("Thread (%s) waiting %d", seed,timeout));
                        wait(timeout);
                        logger.info(String.format("Thread (%s) woke", seed));
                    }
                }
                logger.info(String.format("(%s) exchanging %d", seed, i));
                int from = exchanger.exchange(i);
                logger.info(String.format("(%s) Exchanged %s for %s", seed, i, from));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
