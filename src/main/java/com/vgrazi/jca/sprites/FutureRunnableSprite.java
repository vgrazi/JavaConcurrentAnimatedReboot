package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.states.ThreadState;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.util.concurrent.Future;

/**
 * This sprite renders as follows:<ul>
 * <li>Before the monolith - a runnable with a thread racing towards the mutex</li>
 * <li>In the monolith (running) - renders like a runnable thread</li>
 * <li>In the monolith (complete) - renders as a stopped future</li>
 * <li>After the monolith - renders as an exiting future</li>
 * </ul>
 */
//public class FutureRunnableSprite extends ThreadSprite implements InitializingBean {
public class FutureRunnableSprite extends RunnerThreadSprite{

    private boolean done;

    @Value("${runnable-height}")
    private int runnableHeight;
    private Future future;

    @Override
    public void render(Graphics2D graphics) {
        int yPosition = getYPosition() - runnableHeight / 2;
        int length = arrowLength / 2;
        int xPosition;
        Future future = getFuture();
        ThreadState state = getState();
        // for FutureRunnableSprite, we need to abuse the state to control the rendering.
        // state machine:
        // before we enter, we are waiting
        // when we enter, we are runnable
        // when we complete, we are blocked
        // when we are done, we are terminated
        if (state == getThreadContext().waiting) {
            Color color = getThreadContext().getColor(this);
            graphics.setColor(color);
            RelativePosition relativePosition = getRelativePosition();
            if( relativePosition == RelativePosition.Before) {
                super.render(graphics);
                graphics.fill3DRect(getXPosition() - arrowLength + (arrowLength - length) / 2 - 3, yPosition, length, runnableHeight, true);
            }
            else if( relativePosition == RelativePosition.At) {
                xPosition = monolithLeftBorder;
                graphics.fill3DRect(xPosition - arrowLength + (arrowLength - length) / 2 - 3, yPosition, length, runnableHeight, true);
                super.render(graphics);
            }
        }
        else if (state == getThreadContext().runnable) {
            super.render(graphics);
            RelativePosition relativePosition = getRelativePosition();
            Color color = getThreadContext().getColor(this);
            graphics.setColor(color);
            // render the future
            if (relativePosition != RelativePosition.In) {
                xPosition = getXPosition() - arrowLength + (arrowLength - length) / 2 - 3;
                graphics.fill3DRect(xPosition, yPosition, length, runnableHeight, true);
            }
        }
        else if(state == getThreadContext().blocked){
            xPosition = monolithRightBorder - length;
            graphics.fill3DRect(xPosition, yPosition, length, runnableHeight, true);
        }
        else {
            xPosition = getXPosition() - arrowLength + (arrowLength - length) / 2 - 3;
            graphics.fill3DRect(xPosition, yPosition, length, runnableHeight, true);
        }
    }

    public boolean isDone() {
        return done;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public ThreadState getState() {
        // state is controlled by future.isDone() and Sprite.isDone()
        // if both are false, the the sprite is runnable
        // when the future is done, we still want to keep it in the monolith until the sprite is done
        // when the sprite is also done, we can terminate it
        if(future == null) {
            return getThreadContext().waiting;
        }
        else if(!future.isDone()) {
            // it's still alive
            return getThreadContext().runnable;
        }
        else if(!isDone()) {
            return getThreadContext().blocked;
        }
        else {
            return getThreadContext().terminated;
        }
    }

    @Override
    public String toString() {
        return "FutureRunnableSprite{" +
                "ID=" + getID() +
                ", isDone=" + isDone() +
                ", state=" + getState() +
                ", future=" + (future == null? null:"Done:" + future.isDone()) +
                ", native-state=" + (thread != null?thread.getState().toString():"") +
//                ", x-position=" + getXPosition() +
                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
//                ", " + super.toString() +
                '}';
    }
}
