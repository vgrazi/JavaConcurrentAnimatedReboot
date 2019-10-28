package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class RunnerThreadSprite<S> extends ThreadSprite<S>{
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
            graphics.drawLine(xPosition - arrowLength, yPosition, xPosition, yPosition);
        } else {
            graphics.drawArc(leftBound, topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(rightBound - ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(lineStart, topBound, lineEnd, topBound);
            graphics.drawLine(lineStart, bottomBound, lineEnd, bottomBound);
        }
        renderMessage(graphics);
        drawThreadCap(graphics);
    }

    /**
     * Draws the ball (or whatever) at the end of the thread line
     */
    protected void drawThreadCap(Graphics2D graphics) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        int yPos = getCapYPosition(leftBound, rightBound, topBound, bottomBound, this);
        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength:0;
        graphics.fillOval(getXPosition() -8 -offset, yPos, ballDiameter, ballDiameter);

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

    /**
     * Renders the ball at the correct position
     *
     * @param leftBound   the left-most position of the ellipse
     * @param rightBound  the right-most position of the ellipse
     * @param topBound    the y position of the top-most position of the ellipse
     * @param bottomBound the y position of the bottom-most position of the ellipse
     * @param sprite      the sprite we are animating
     */
    protected int getCapYPosition(int leftBound, int rightBound, int topBound, int bottomBound, Sprite sprite) {
        int xPos = sprite.getXPosition();
        // note that the "ellipse" is really just 2 semi-circles connected by straight horizontal lines,
        // and the radius os 1/2 the height
        int ellipseRadius = (bottomBound - topBound) / 2;
        int xAxis = (bottomBound + topBound) / 2;

        // this is the x-position where the left semi-circle stops and the straight horizontal line start:
        int lineStart = leftBound + ellipseRadius;
        // this is the x-position where the straight horizontal line ends and the right semi-circle starts"
        int lineEnd = rightBound - ellipseRadius;
        int yPos;
        switch (sprite.getDirection()) {
            case right:
                if (xPos <= leftBound) {
                    // we have not entered the left semicircle yet
                    yPos = xAxis;
                } else if (xPos < lineStart) {
                    // we are in the left top semicircle
                    int legLength = ellipseRadius - (xPos - leftBound);
                    yPos = xAxis - (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                } else if (xPos < lineEnd) {
                    // we are in the top line
                    yPos = topBound;
                } else {
                    // we are in the right semi circle
                    int legLength = xPos - lineEnd;
                    yPos = xAxis - (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                }
                break;
            case left:
                if (xPos <= lineStart) {
                    // we are in the left bottom semicircle
                    int legLength = ellipseRadius - (xPos - leftBound);
                    yPos = xAxis + (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                } else if (xPos < lineEnd) {
                    // we are in the bottom line
                    yPos = bottomBound;
                } else {
                    // we are in the right bottom semicircle
                    int legLength = xPos - lineEnd;
                    yPos = xAxis + (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown direction-should never happen - did you add a direction besides left and right??");
        }
        yPos-= ballDiameter /2;
        System.out.printf("old x-pos:%d new xPos:%d line-start:%d  ypos:%d  ellipse radius:%d  xaxis:%d ball-diameter:%d%n",
                 this.xPosition, xPos + ballDiameter/2, lineStart, yPos + ballDiameter /2, ellipseRadius, xAxis, ballDiameter);
        return yPos;
    }

}
