package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.RenderUtils;
import com.vgrazi.jca.util.UIUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

import static com.vgrazi.jca.util.Parsers.parseColor;
import static com.vgrazi.jca.util.Parsers.parseFont;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class RunnerThreadSprite<S> extends ThreadSprite<S> implements InitializingBean {
    protected Font conditionFont;
    protected Color conditionColor;
    private int margin;
    protected int leftBound;
    protected int rightBound;
    protected int topBound;
    protected int bottomBound;
    private int ellipseRadius;
    private int lineStart;
    private int lineEnd;

    @Autowired
    private UIUtils uiUtils;

    @Value("${monolith-left-border}")
    protected int monolithLeftBorder;

    @Value("${monolith-right-border}")
    protected int monolithRightBorder;

    @Value("${runner-ellipse-height}")
    protected int runnerEllipseHeight;

    @Value("${pixels-per-y-step}")
    protected int pixelsPerYStep;

    @Value("${ball-diameter}")
    protected int ballDiameter;

    @Value("${stroke-width}")
    private int strokeWidth;

    @Value("${arrow-length}")
    protected int arrowLength;

    @Value("${condition-font}")
    private void setConditionFont(String font) {
        conditionFont = parseFont(font);
    }

    private Image flagImage;

    @Value("${condition-color}")
    private void setConditionColor(String color) {
        conditionColor = parseColor(color);
    }

    @Override
    public void setYPosition(int yPosition) {
        super.setYPosition(yPosition);
        margin = (pixelsPerYStep - runnerEllipseHeight) / 2;
        leftBound = monolithLeftBorder + margin;// + strokeWidth;
        rightBound = monolithRightBorder - margin;// - strokeWidth;
        topBound = yPosition - runnerEllipseHeight / 2;
        bottomBound = yPosition + runnerEllipseHeight / 2;
        ellipseRadius = (bottomBound - topBound) / 2;
//        this is the x-position where the left semi-circle stops and the straight horizontal line start
        lineStart = leftBound + ellipseRadius - 1;
//        this is the x-position where the straight horizontal line ends and the right semi-circle starts
        lineEnd = rightBound - ellipseRadius + 1;

    }

    /**
     * Sets the interrupted Flag image
     */
    @Override
    public void afterPropertiesSet(){
        super.afterPropertiesSet();
        flagImage = uiUtils.getImage("images/flag.png");
    }

    @Override
    public void render(Graphics2D graphics) {
// Render a bounding box around where the rounded circle should be.
//        Graphics2D graphicsDebug = (Graphics2D) graphics.create();
//        graphicsDebug.setStroke(new BasicStroke(1));
//        graphicsDebug.setColor(Color.yellow);
//        graphicsDebug.drawRect(leftBound, topBound, width, runnerEllipseHeight);

        Color color = getThreadContext().getColor(this);
//        Color color = getColorByThreadState();
        graphics.setColor(color);
        if(isInMonolith()){
            graphics.drawArc(leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(lineStart + getXOffset(), topBound, lineEnd + getXOffset(), topBound);
            graphics.drawLine(lineStart + getXOffset(), bottomBound, lineEnd + getXOffset(), bottomBound);

        }else{// && relativePosition != RelativePosition.At) {
            // render the runner thread before it enters the monolith
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
        }

        renderMessage(graphics);
        drawThreadCap(graphics);
        renderInterruptedFlag(graphics);
    }

    /**
     * Same as drawThreadCap, however applies an offset to the x location
     */
    protected void drawThreadCap(Graphics2D graphics){
        drawThreadCap(graphics, 0);
    }

    /**
     * First checks if the thread is interrupted. If it is, renders an interrupt flag
     */
    private void renderInterruptedFlag(Graphics2D graphics){
        if(getThread().isInterrupted()) {
            if(isInMonolith()){
                graphics.drawImage(flagImage, (rightBound + leftBound)/2-flagImage.getWidth(null)/2, topBound, null);
            }else {
                graphics.drawImage(flagImage, xPosition - arrowLength/4*3, yPosition-20, null);
            }
        }
    }


    /**
     * Draws the ball at the end of the thread shaft, and the condition if any
     * If the sprite is attached to a condition, draws a C with the
     * condition id (1-based serial), instead of the ball
     */
    protected void drawThreadCap(Graphics2D graphics, int capOffset) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        int yPos = RenderUtils.getCapYPosition(leftBound, rightBound, topBound, bottomBound, ballDiameter, this);
        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength : 0;
//        Graphics2D graphicsCopy = (Graphics2D) graphics.create();
        if (hasCondition()){
            graphics.setFont(conditionFont);
            graphics.setColor(conditionColor);
            FontMetrics fm = graphics.getFontMetrics();
            int height = fm.getHeight();
            graphics.drawString("C" + getConditionId(), getXPosition() + getXOffset() - 8 - offset + capOffset, yPos + height / 2);
        } else {
            graphics.fillOval(getXPosition() + getXOffset() - 8 - offset + capOffset, yPos, ballDiameter, ballDiameter);
        }
    }

// leftBound     ___________________________________     rightBound
//    |       . |lineStart         ^        lineEnd|   .      |
//    |   .     |                  |               |      .   |
//    | .       |                  |               |        . |
//    |. _______|________________  | ______________|________ .|
//    |.        |                  |               |         .|
//    | .       |                  |               |        . |
//    |    .    |                  |               |      .   |
//    |        .| _________________V_______________|__ .      |

}
