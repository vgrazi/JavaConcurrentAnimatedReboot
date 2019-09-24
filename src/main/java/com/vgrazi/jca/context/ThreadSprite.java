package com.vgrazi.jca.context;

import com.vgrazi.jca.states.State;
import com.vgrazi.jca.util.IDGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the Java thread itself, the shape, xPosition, and the targetState, which is called by slide,
 * and is used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class ThreadSprite implements InitializingBean {
    private Thread thread;
    private int xPosition;

    private int ID = IDGenerator.next();

    private TargetState targetState = TargetState.default_state;
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
    private int yPosition;

    public ThreadSprite() {
    }

    /**
     * You can change the ID to something more meaningful
     * @param ID
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    public RelativePosition getRelativePosition() {
        int position = getXPosition();
        if(position < monolithLeftBorder) {
            return RelativePosition.Before;
        }
        else if (position >= monolithLeftBorder&& position <= monolithLeftBorder +  pixelsPerStep) {
            return RelativePosition.At;
        }
        else if (position > monolithLeftBorder +  pixelsPerStep && position < monolithRightBorder) {
            return RelativePosition.In;
        }
        else {
            return RelativePosition.After;
        }
    }

    void setNextPosition() {
        getState().advancePosition(this);
    }

    public void setYPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public Thread.State getThreadState() {
        return thread.getState();
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
     * todo: should TargetState be renamed to action? (Since it is really an action to be performed, more than it is a state.)
     */
    public enum TargetState {
        default_state, waiting, notifying, readLock, writeLock, releaseWriteLock, releaseReadLock, awaitAdvance, arrive, release

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
    public void attachAndStartRunnable(Runnable runnable) {
        thread = new Thread(runnable);
        thread.start();
    }

    public boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Returns our internal thread state, reflecting the native thread state, with some adjustments (new and runnable
     * are both considered runnable, and waiting and timed-waiting are both considered waiting.
     * @return
     */
    State getState() {
        if(thread == null) {
            return null;
        }
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

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void afterPropertiesSet() {
        setYPosition(threadContext.getNextYPosition());
    }

    @Override
    public String toString() {
        return "ThreadSprite{" +
                "ID=" + ID +
                ", state=" + getState() +
//                ", x-position=" + xPosition +
//                ", y-position=" + yPosition +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }
}
