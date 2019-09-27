package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the Java thread itself, the shape, xPosition, and the action, which is called by slide,
 * and is used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class ThreadSprite extends Sprite {

    private Thread thread;
    @Value("${arrow-length}")
    private int arrowLength;
    public Thread getThread() {
        return thread;
    }

    public Thread.State getThreadState() {
        return thread.getState();
    }
    /**
     * Create the thread associated with this runnable, and starts it
     */
    public void attachAndStartRunnable(Runnable runnable) {
        thread = new Thread(runnable);
        thread.start();
    }

    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    protected void setNextPosition() {
        getState().advancePosition(this);
    }

    @Override
    public void render(Graphics2D graphics) {

        Color color = getThreadContext().getColor(this);
//        Color color = getColorByThreadState();
        graphics.setColor(color);
        graphics.drawLine(getXPosition() - arrowLength, getYPosition(), getXPosition(), getYPosition());
    }

    protected ThreadState getState() {
        if(thread == null) {
            return null;
        }
        switch (thread.getState()) {
            case NEW:
            case RUNNABLE:
                return getThreadContext().runnable;
            case WAITING:
            case TIMED_WAITING:
                return getThreadContext().waiting;
            case BLOCKED:
                return getThreadContext().blocked;
            case TERMINATED:
                return getThreadContext().terminated;
            default:
                throw new IllegalArgumentException("Unknown thread state " + thread.getState());
        }
    }


}
