package com.vgrazi.jca;

import com.vgrazi.jca.states.State;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the thread itself, the shape, position, and the targetState, which is called by
 * the used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class ThreadSprite {
    private Thread thread;
    private int position;
    private int ID = IDGenerator.next();
    private TargetState targetState = TargetState.no_change;

    /**
     * In order to change the thread state, call setTargetState() passing in appropriate state.
     * The runnable must be written such that it recognizes the state and responds appropriately
     */
    public enum TargetState {
        no_change, waiting, notifying, release

    }

    public TargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(TargetState targetState) {
        this.targetState = targetState;
    }

    private boolean running = true;

    @Autowired
    private ThreadContext threadContext;
    private boolean waitingSet;

    /**
     * Create the thread associated with this runnable, and starts it
     *
     * @param runnable
     */
    public void setRunnable(Runnable runnable) {
        thread = new Thread(runnable);
        thread.start();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public State getState() {
        switch (thread.getState()) {
            case NEW:
            case RUNNABLE:
                return State.runnable;
            case WAITING:
            case TIMED_WAITING:
                return State.waiting;
            case BLOCKED:
                return State.blocked;
            case TERMINATED:
                return State.terminated;
            default:
                throw new IllegalArgumentException("Unknown thread state " + thread.getState());
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public String toString() {
        return "ThreadSprite{" +
                "ID=" + ID +
                ", position=" + position +
                ", state=" + getState() +
                '}';
    }
}
