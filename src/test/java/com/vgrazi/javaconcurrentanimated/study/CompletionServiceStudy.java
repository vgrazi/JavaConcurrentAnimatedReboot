package com.vgrazi.javaconcurrentanimated.study;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class CompletionServiceStudy {

    private static Logger logger = Logger.getLogger("CompletionServiceStudy");
    private static void println(Object message) {
        logger.info(String.valueOf(message));
    }
    private static Thread.State lastState;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CompletionService<String> completionService = new ExecutorCompletionService(Executors.newCachedThreadPool());
        Future<String> take = completionService.take();
        println(take);
        printThreadState();
        completionService.submit(() -> {
            try {
                for(int i = 0; i < 5; i++) {
                    printThreadState();
                    println("Still there");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            printThreadState();
            println("Exiting");
            return "Complete";
        });
        println("Waiting for result");
        String rval = completionService.take().get();
        println("returned " + rval);
    }

    private static void printThreadState() {
        Thread.State state = Thread.currentThread().getState();
        if(state != lastState) {
            println(state);
            lastState = state;
        }
    }
}
