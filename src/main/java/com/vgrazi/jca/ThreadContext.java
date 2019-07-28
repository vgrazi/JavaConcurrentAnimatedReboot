package com.vgrazi.jca;

import com.vgrazi.jca.states.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Maintains the list of ThreadSprites, position of monolith,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext  {
    @Autowired
    Blocked blocked;
    @Autowired
    Running runnable;
    @Autowired
    Waiting waiting;
    @Autowired
    Terminated terminated;

    private List<ThreadSprite> threads = new CopyOnWriteArrayList<>();
    @Autowired
    ApplicationContext context;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Value("${monolith-left-border}")
    public int monolithLeftBorder;
    @Value("${monolith-right-border}")
    public int monolithRightBorder;
    @Value("${pixels-per-step}")
    public int pixelsPerStep;

    public synchronized void addThread(ThreadSprite thread) {
        threads.add(thread);
    }

    public synchronized void stopThread(ThreadSprite threadSprite) {
        threadSprite.setRunning(false);
        new Thread(()->{
            try {
                Thread.sleep(5000);
                threads.remove(threadSprite);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    void run() throws InterruptedException {
        executor.scheduleAtFixedRate(this::advanceSprites, 0, 100, TimeUnit.MILLISECONDS);

        while(true) {
            threads.forEach(System.out::println);
            Thread.sleep(1000);
        }
    }

    /**
     * If there is exactly one running thread, returns it.
     * Otherwise throws an IllegalArgumentException
     */
    public ThreadSprite getRunningThread() {
        List<ThreadSprite> threads = getThreadsOfState(runnable);
        if(threads.size() != 1) {
            throw new IllegalArgumentException("Expected one running thread but found " + threads.size());
        }

        return threads.get(0);
    }


    /**
     * Returns a list of all threads that are not of the specified state
     */
    public List<ThreadSprite> getThreadsNotOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() != state).collect(Collectors.toList());
        return collect;
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    private List<ThreadSprite> getThreadsOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() == state).collect(Collectors.toList());
        return collect;
    }

    /**
     * Advance the position of each sprite, based on its current position and state
     */
    private void advanceSprites() {
        threads.forEach(ThreadSprite::setNextPosition);
    }
}
