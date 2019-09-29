package com.vgrazi.jca.context;

import com.vgrazi.jca.states.ThreadState;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static com.vgrazi.jca.util.ColorParser.parseColor;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the Java thread itself, the shape, xPosition, and the action, which is called by slide,
 * and is used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class FutureSprite extends Sprite implements InitializingBean {

    private Thread thread;
    private Color futureDefaultColor;
    private Color futureDoneColor;
    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${future-width}")
    private int futureWidth;

    private CompletableFuture future;
    @Value("${future-height}")
    private int height;

    protected void setNextXPosition() {
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(future.isDone() ? futureDoneColor : futureDefaultColor);
        graphics.fill3DRect(getXPosition(), getYPosition(), futureWidth, height, true);
    }

    protected ThreadState getState() {
        if (thread == null) {
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

    @Value("${FUTURE-DEFAULT-COLOR}")
    private void setFutureDefaultColor(String color) {
        futureDefaultColor = parseColor(color);
    }

    @Value("${FUTURE-DONE-COLOR}")
    private void setFutureDoneColor(String color) {
        futureDoneColor = parseColor(color);
    }

    @Override
    public void afterPropertiesSet() {
        setXPosition((rightBorder + leftBorder - futureWidth) / 2);
    }

    /**
     * Override the default height
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
        setYPosition(getThreadContext().getNextYPosition(height));
    }

    @Override
    public String toString() {
        return "FutureSprite{" +
                "ID=" + getID() +
                ", state=" + getState() +
//                ", x-position=" + xPosition +
//                ", y-position=" + yPosition +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }


    public void setFuture(CompletableFuture future) {
        this.future = future;
    }

    /**
     * Returns true of this future has been marked as complete
     *
     * @return
     */
    public boolean isDone() {
        return future.isDone();
    }
}