package com.vgrazi.jca;

import com.vgrazi.jca.states.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maintains the list of ThreadSprites, position of Monolith,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext  {
    private List<ThreadSprite> threads = new ArrayList<>();
    @Autowired
    ApplicationContext context;

    @Value("${monolith-left-border}")
    private int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    private int pixelsPerStep;

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

    public Position getPosition(ThreadSprite threadSprite) {
        int position = threadSprite.getPosition();
        if(position < monolithLeftBorder - pixelsPerStep) {
            return Position.Before;
        }
        else if (position > monolithLeftBorder - pixelsPerStep && position <= monolithLeftBorder) {
            return Position.At;
        }
        else if (position > monolithLeftBorder && position < monolithRightBorder) {
            return Position.In;
        }
        else {
            return Position.After;
        }
    }

    public void run() throws InterruptedException {
        while(true) {
            threads.forEach(System.out::println);
            Thread.sleep(1000);
        }
    }

    /**
     * If there is exactly one running thread, returns it.
     * Otherwise throws an IllegalArgumentException
     * @return
     */
    public ThreadSprite getRunningThread() {
        List<ThreadSprite> threads = getThreadsOfState(State.runnable);
        if(threads.size() != 1) {
            throw new IllegalArgumentException("Expected one running thread but found " + threads.size());
        }

        return threads.get(0);
    }


    /**
     * Returns a list of all threads that are not of the specified state
     * @return
     */
    public List<ThreadSprite> getThreadsNotOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() != state).collect(Collectors.toList());
        return collect;
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    public List<ThreadSprite> getThreadsOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() == state).collect(Collectors.toList());
        return collect;
    }
}
