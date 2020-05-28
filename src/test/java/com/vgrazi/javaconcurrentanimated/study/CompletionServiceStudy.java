package com.vgrazi.javaconcurrentanimated.study;

import java.util.concurrent.*;

public class CompletionServiceStudy {
    private static Thread.State lastState;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CompletionService<String> completionService = new ExecutorCompletionService(Executors.newCachedThreadPool());
        Future<String> take = completionService.take();
        System.out.println(take);
        printThreadState();
        completionService.submit(() -> {
            try {
                for(int i = 0; i < 5; i++) {
                    printThreadState();
                    System.out.println("Still there");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            printThreadState();
            System.out.println("Exiting");
            return "Complete";
        });
        System.out.println("Waiting for result");
        String rval = completionService.take().get();
        System.out.println("returned " + rval);
    }

    private static void printThreadState() {
        Thread.State state = Thread.currentThread().getState();
        if(state != lastState) {
            System.out.println(state);
            lastState = state;
        }
    }
}
