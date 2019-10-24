package com.vgrazi.jca.sprites;

import com.vgrazi.jca.states.ThreadState;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

import static com.vgrazi.jca.util.Parsers.parseColor;

public class RunnableSprite<S> extends ThreadSprite<S> {
    private Color futureDefaultColor;
    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${future-width}")
    private int width;
    private int height;
    private boolean done;

    @Value("${pixels-per-y-step}")
    public void setHeight(int height) {
        this.height = height / 4;
    }

    @Override
    public void setNextXPosition() {
        ThreadState state = getState();
        state.advancePosition(this);
    }

    @Override
    public void setXPosition(int xPosition) {
        if (getState() == getThreadContext().runnable && ((getDirection() == Direction.left || xPosition >= xRestingPos && xPosition <= rightBorder))) {
            super.setXPosition(xRestingPos);
        } else {
            super.setXPosition(xPosition);
        }
    }

    private int xRestingPos;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        xRestingPos = rightBorder - (rightBorder - leftBorder - width) / 2;
    }

    @Override
    public void render(Graphics2D graphics) {
        // todo: make a property
        graphics.setColor(Color.orange);
        graphics.fill3DRect(getXPosition() - width - 1, getYPosition() - height / 2, width, height, true);
    }

    @Value("${FUTURE-DEFAULT-COLOR}")
    private void setFutureDefaultColor(String color) {
        futureDefaultColor = parseColor(color);
    }

    @Override
    public ThreadState getState() {
        if (done) {
            return getThreadContext().terminated;
        }
        else if (thread == null) {
            return getThreadContext().waiting;
        } else {
            return super.getState();
        }
    }

    @Override
    public String toString() {
        return "RunnableSprite{" +
                "ID=" + getID() +
                ", state=" + getState() +
                ",thread=" + thread +

//                ", x-position=" + getXPosition() +
//                ", y-position=" + yPosition +
                ", relative_position=" + getRelativePosition() +
                '}';
    }

    public void setDone() {
        this.done = true;
        // set thread to null, so that our context search by thread for the pooled thread returns the pooled thread and not this runnable
        setThread(null);
    }
}
