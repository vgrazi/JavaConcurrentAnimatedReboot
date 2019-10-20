package com.vgrazi.jca.sprites;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

/**
 * A ThreadSprite represents one thread, and retains all of the state related to that thread,
 * including the Java thread itself, the shape, xPosition, and the action, which is called by slide,
 * and is used to change the state
 * Note: We should really create the thread in the constructor, but its Runnable needs access to this class's
 * running flag. So construct the sprite, then add the Runnable.
 */
public class ObjectSprite extends ThreadSprite implements InitializingBean {
    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${future-width}")
    private int width;

    private int height;

    @Override
    public void render(Graphics2D graphics) {
        Color color = getThreadContext().getColor(this);
        graphics.setColor(color);
        int yCenter = getYPosition();
//        Draw a positioning line, for diagnostics
//        graphics.drawLine(0, yCenter, 1000, yCenter);
        int yPos = yCenter - height/2;
        graphics.fillOval(getXPosition() + getXMargin() , yPos + getYMargin(), width, height);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        setXMargin((rightBorder - leftBorder - width)/2);
    }

    /**
     * Override the default height
     * @param height
     * todo: add a specialized object height property
     */
    @Value("${future-height}")
    public void setHeight(int height) {
        this.height = height/2;
    }

    @Override
    public String toString() {
        return "ObjectSprite{" +
                "ID=" + getID() +
//                ", x-position=" + getXPosition() +
//                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
                '}';
    }
}
