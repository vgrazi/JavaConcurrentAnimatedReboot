package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;
import com.vgrazi.jca.util.IDGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Base class for sprites, such as ThreadSprite, FutureSprite, ObjectSprite, etc.
 */
public abstract class Sprite implements InitializingBean {
    private int xPosition;

    private int ID = IDGenerator.next();

    private Direction direction = Direction.right;
    @Value("${monolith-left-border}")
    private int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    private int pixelsPerStep;

    @Autowired
    private ThreadContext threadContext;
    private int yPosition;
    private String action = "default";

    /**
     * You can change the ID to something more meaningful
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    public RelativePosition getRelativePosition() {
        int position = getXPosition();
        if(position < monolithLeftBorder) {
            return RelativePosition.Before;
        }
        else if (position <= monolithLeftBorder + pixelsPerStep) {
            return RelativePosition.At;
        }
        else if (position > monolithLeftBorder +  pixelsPerStep && position < monolithRightBorder) {
            return RelativePosition.In;
        }
        else {
            return RelativePosition.After;
        }
    }

    protected abstract void setNextPosition();

    public void setYPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public ThreadContext getThreadContext() {
        return threadContext;
    }

    public void setThreadContext(ThreadContext threadContext) {
        this.threadContext = threadContext;
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
     * In order to change the thread state, call setAction() passing in appropriate state.
     * The runnable must be written such that it recognizes the state and responds appropriately
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns our internal thread state, reflecting the native thread state, with some adjustments (new and runnable
     * are both considered runnable, and waiting and timed-waiting are both considered waiting.
     */
    protected abstract ThreadState getState();

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }


    @Override
    public void afterPropertiesSet() {
        setYPosition(getThreadContext().getNextYPosition());
    }

    @Override
    public String toString() {
        return "Sprite{" +
                "ID=" + ID +
                ", state=" + getState() +
//                ", x-position=" + xPosition +
//                ", y-position=" + yPosition +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }

}
