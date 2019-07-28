package com.vgrazi.jca;

import com.vgrazi.jca.states.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
    private Direction direction = Direction.right;
    @Value("${monolith-left-border}")
    private int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    private int pixelsPerStep;

    private boolean running = true;

    @Autowired
    private ThreadContext threadContext;

    public RelativePosition getRelativePosition() {
        int position = getPosition();
        if(position < monolithLeftBorder - pixelsPerStep) {
            return RelativePosition.Before;
        }
        else if (position > monolithLeftBorder - pixelsPerStep && position <= monolithLeftBorder) {
            return RelativePosition.At;
        }
        else if (position > monolithLeftBorder && position < monolithRightBorder) {
            return RelativePosition.In;
        }
        else {
            return RelativePosition.After;
        }
    }

    void setNextPosition() {
        getState().advancePosition(this);
    }

    public enum Direction {
        right, down, left, up
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

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

    /**
     * Create the thread associated with this runnable, and starts it
     */
    public void setRunnable(Runnable runnable) {
        thread = new Thread(runnable);
        thread.start();
    }

    public boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    State getState() {
        switch (thread.getState()) {
            case NEW:
            case RUNNABLE:
                return threadContext.runnable;
            case WAITING:
            case TIMED_WAITING:
                return threadContext.waiting;
            case BLOCKED:
                return threadContext.blocked;
            case TERMINATED:
                return threadContext.terminated;
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
                ", relative_position=" + getRelativePosition() +
                ", state=" + getState() +
                ", " + super.toString() +
                '}';
    }
}
