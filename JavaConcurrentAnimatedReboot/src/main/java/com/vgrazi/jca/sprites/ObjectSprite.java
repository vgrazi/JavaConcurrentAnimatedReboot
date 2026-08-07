package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.states.ThreadState;
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
public class ObjectSprite extends RunnerThreadSprite implements InitializingBean {
    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${future-width}")
    private int width;

    private int height;

    @Override
    public void render(Graphics2D graphics) {
        graphics.setColor(Color.orange);
        int yCenter = getYPosition();
//        Draw a positioning line, for diagnostics
//        graphics.drawLine(0, yCenter, 1000, yCenter);
        int yPos = yCenter - height / 2;
        graphics.fillOval(getXPosition() + getXOffset() - width, yPos + getYMargin(), width, height);
        // if the object is arriving or blocked, we draw the accompanying thread
        RelativePosition relativePosition = getRelativePosition();
        if (relativePosition == RelativePosition.Before || relativePosition == RelativePosition.At)
        {
            Color color = getThreadContext().getColor(this);
            graphics.setColor(color);
            if (relativePosition != RelativePosition.In) {
                int xPosition = getXPosition();
                int yPosition = getYPosition();
                graphics.drawLine(xPosition - arrowLength + getXOffset() - width + 15, yPosition, xPosition + getXOffset() - width, yPosition);
                renderMessage(graphics);
                drawThreadCap(graphics,  -width);
            }
        }
    }

    @Override
    public void setNextXPosition() {
        ThreadState state = getState();
        if (state == getThreadContext().runnable && (getRelativePosition() == RelativePosition.In || getRelativePosition() == RelativePosition.At) && getDirection() == Direction.left) {
            super.setXPosition(rightBorder - 1 - getXMargin());
        } else {
            state.advancePosition(this);
        }
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        setXMargin((rightBorder - leftBorder - width) / 2);
    }

    /**
     * Override the default height
     *
     * @param height todo: add a specialized object height property
     */
    @Value("${future-height}")
    public void setHeight(int height) {
        this.height = height / 2;
    }

    @Override
    public String toString() {
        return "ObjectSprite{" +
                "ID=" + getID() +
                ", state=" + getState() +
//                ", native-state=" + thread.getState() +
//                ", x-position=" + getXPosition() +
//                ", y-position=" + getYPosition() +
                ", relative_position=" + getRelativePosition() +
                '}';
    }
}
