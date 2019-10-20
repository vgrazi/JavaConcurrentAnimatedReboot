package com.vgrazi.jca.sprites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class PooledThreadSprite<S> extends RunnerThreadSprite<S> {
    /**
     * If the sprite is pooled (ie not in action) then set to true. Otherwise it is active
     */
    private boolean pooled = true;
    private int yPositionActive;
    private int yPositionPooled;
    @Autowired
    ApplicationContext context;

    public int getyPositionPooled() {
        return yPositionPooled;
    }

    public boolean isPooled() {
        return pooled;
    }

    /**
     * Sets the pooled status, and also re-initializes all of the positioning fields in the super class
     * @param pooled
     */
    public void setPooled(boolean pooled) {
        this.pooled = pooled;
//        re-initialize all of the positioning fields in the super class, based on if we are rendering above or below
        if(pooled) {
            setYPosition(getyPositionPooled());
        }
        else {
            setYPosition(getYPositionActive());
        }
    }

    @Override
    public int getYPosition() {
        if(isPooled()) {
            return getyPositionPooled();
        }
        else {
            return getYPositionActive();
        }
    }

    public int getYPositionActive() {
        return yPositionActive;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.yPositionPooled = getThreadContext().getNextPooledYPosition(pixelsPerYStep);

        // yPosition is already set by superclass
        this.yPositionActive = yPosition;
    }

    @Override
    public void setXPosition(int xPosition) {
        super.setXPosition(xPosition);
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
                ", pooled=" + isPooled() +
//                ", x-position=" + getXPosition() +
                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }
}
