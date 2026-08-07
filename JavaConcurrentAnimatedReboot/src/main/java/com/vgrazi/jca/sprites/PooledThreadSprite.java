package com.vgrazi.jca.sprites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.awt.*;

public class PooledThreadSprite<S> extends RunnerThreadSprite<S> {
    /**
     * If the sprite is pooled (ie not in action) then set to true. Otherwise it is active
     */
    private boolean pooled = true;
    private int yPositionActive;
    private int yPositionPooled;
    @Autowired
    ApplicationContext context;

    /**
     * Sets the pooled status, and also re-initializes all of the positioning fields in the super class
     */
    public void setPooled(boolean pooled) {
        this.pooled = pooled;
//        re-initialize all of the positioning fields in the super class, based on if we are rendering above or below
        if(pooled) {
            setYPosition(yPositionPooled);
        }
        else {
            setYPosition(yPositionActive);
        }
    }

    @Override
    public int getYPosition() {
        if(pooled) {
            return yPositionPooled;
        }
        else {
            return yPositionActive;
        }
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.yPositionPooled = getThreadContext().getNextPooledYPosition();

        // yPosition is already set by superclass
        this.yPositionActive = yPosition;
    }

    @Override
    protected void drawThreadCap(Graphics2D graphics) {
        if (getThreadState() == Thread.State.WAITING) {
            graphics.setColor(getThreadContext().getColorByInstance(this));
            graphics.fillOval(leftBound + getXOffset(), topBound, ballDiameter, ballDiameter);

        } else {
            super.drawThreadCap(graphics);
        }
    }

    @Override
    protected int getNextYPositionFromContext() {
        // do nothing - y position is set from the runnable
        return 0;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public String toString() {
        return "PooledThreadSprite{" +
                "ID=" + getID() +
                ", state=" + getState() +
                ", native-state=" + thread.getState() +
                ", pooled=" + pooled +
//                ", x-position=" + getXPosition() +
                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
                '}';
    }
}
