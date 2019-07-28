package com.vgrazi.jca;

import com.vgrazi.jca.states.State;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then
 */
public class ThreadSprite {
    private Thread thread;
    private int position;
    private int ID = IDGenerator.next();
    private SetState setState = SetState.none;

    /**
     * In order to change the thread state, call setSetState() passing in appropriate state.
     * The runnable must be written such that it recognizes the state and responds appropriately
     */
    public enum SetState {
        none, waiting, notifying, release

    }

    public SetState getSetState() {
        return setState;
    }

    public void setSetState(SetState setState) {
        this.setState = setState;
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
