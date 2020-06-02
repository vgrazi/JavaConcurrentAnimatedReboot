package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import com.vgrazi.jca.util.RenderUtils;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

import static com.vgrazi.jca.util.Parsers.parseColor;
import static com.vgrazi.jca.util.Parsers.parseFont;
import static com.vgrazi.jca.util.UIUtils.applyAlpha;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class RunnerThreadSprite<S> extends ThreadSprite<S> {
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


    @Override
    public void render(Graphics2D graphics) {
// Render a bounding box around where the rounded circle should be.
//        Graphics2D graphicsDebug = (Graphics2D) graphics.create();
//        graphicsDebug.setStroke(new BasicStroke(1));
//        graphicsDebug.setColor(Color.yellow);
//        graphicsDebug.drawRect(leftBound, topBound, width, runnerEllipseHeight);

        RelativePosition relativePosition = getRelativePosition();
        Color color = getThreadContext().getColor(this);
//        Color color = getColorByThreadState();
        graphics.setColor(color);
        if (relativePosition != RelativePosition.In) {// && relativePosition != RelativePosition.At) {
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
        } else {
            graphics.drawArc(leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(lineStart + getXOffset(), topBound, lineEnd + getXOffset(), topBound);
            graphics.drawLine(lineStart + getXOffset(), bottomBound, lineEnd + getXOffset(), bottomBound);
        }
        renderMessage(graphics);
        drawThreadCap(graphics);
    }

    /**
     * Same as drawThreadCap, however applies an offset to the x location
     */
    protected void drawThreadCap(Graphics2D graphics){
        drawThreadCap(graphics, 0);
    }

    /**
     * Draws the ball (or whatever) at the end of the thread line
     * If the sprite is attached to a condition, draws a C with the
     * condition id (1-based serial), instead of the ball
     */
    protected void drawThreadCap(Graphics2D graphics, int capOffset) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        int yPos = RenderUtils.getCapYPosition(leftBound, rightBound, topBound, bottomBound, ballDiameter, this);
        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength : 0;
        if (hasCondition()) {
            Graphics2D graphicsCopy = (Graphics2D) graphics.create();
            graphicsCopy.setFont(conditionFont);
            graphicsCopy.setColor(conditionColor);
            FontMetrics fm = graphicsCopy.getFontMetrics();
            int height = fm.getHeight();

            graphicsCopy.drawString("C" + getConditionId(), getXPosition() + getXOffset() - 8 - offset + capOffset, yPos + height/2);
            graphicsCopy.dispose();
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
