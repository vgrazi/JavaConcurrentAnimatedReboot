package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.util.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

import static com.vgrazi.jca.util.Parsers.parseFont;

/**
 * Base class for sprites, such as ThreadSprite, FutureSprite, ObjectSprite, etc.
 */
public abstract class Sprite<T> {
    protected int xPosition;
    @Autowired
    @Qualifier("basicStroke")
    private Stroke stroke;

    private Font messageFont;

    private int ID = IDGenerator.next();

    private Direction direction = Direction.right;
    @Value("${monolith-left-border}")
    private int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;

    private String message;

    /**
     * Number of pixels to allow at top - this is subtracted from the set ypos
     */
    private int xMargin;

    /**
     * Number of pixels to allow at left - this is subtracted from the set xpos
     */
    private int yMargin;


    @Autowired
    private ThreadContext threadContext;
    protected int yPosition;
    private volatile String action = "default";

    private boolean running = true;
    private volatile T holder;
    private int xRightMargin;
    private long specialId;

    @Value("${arrow-length}")
    private int arrowLength;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


    public int getID() {
        return ID;
    }

    /**
     * You can change the ID to something more meaningful
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    public RelativePosition getRelativePosition() {
        int position = getXPosition();
        if (position < monolithLeftBorder) {
            return RelativePosition.Before;
        }
        else if (position == monolithLeftBorder) {
            return RelativePosition.At;
        }
        else if (position > monolithLeftBorder && position < monolithRightBorder) {
            return RelativePosition.In;
        }
        else {
            return RelativePosition.After;
        }
    }

    public abstract void setNextXPosition();

    public int getXMargin() {
        return xMargin;
    }

    public void setXMargin(int xMargin) {
        this.xMargin = xMargin;
    }

    public int getXRightMargin() {
        return xRightMargin;
    }

    public void setXRightMargin(int xRightMargin) {
        this.xRightMargin = xRightMargin;
    }

    public int getYMargin() {
        return yMargin;
    }

    public void setYMargin(int yMargin) {
        this.yMargin = yMargin;
    }

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

    public abstract void render(Graphics2D graphics);

    public T getHolder() {
        return holder;
    }

    /**
     * If the message is non-null, renders the message above the thread
     * @param graphics
     */
    public void renderMessage(Graphics2D graphics) {
        if(getMessage() != null) {
            Graphics graphics1 = graphics.create();
            graphics1.setColor(Color.white);
            graphics1.setFont(messageFont);
            graphics1.drawString(message, getXPosition() - arrowLength, getYPosition() - 8);
            graphics1.dispose();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A convenience method for holding arbitrary data in a sprite
     */
    public void setHolder(T holder) {
        this.holder = holder;
    }

    public long getSpecialId() {
        return specialId;
    }

    /**
     * Some slides require distinguishing kinds of threads. For example, ReentrantLockSlide requires special handling of interruptible threads
     * Use this method to set a special id, that distinguishes threads of the special type
     */
    public void setSpecialId(long specialId) {
        this.specialId = specialId;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public enum Direction {
        right, left;
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

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "ID=" + ID +
//                ", x-position=" + xPosition +
//                ", y-position=" + yPosition +
                ", relative_position=" + getRelativePosition() +
                ", " + super.toString() +
                '}';
    }

    @Value("${thread-message-font}")
    public void setMessageFont(String fontDescriptor) {
        messageFont = parseFont(fontDescriptor);
    }

}
