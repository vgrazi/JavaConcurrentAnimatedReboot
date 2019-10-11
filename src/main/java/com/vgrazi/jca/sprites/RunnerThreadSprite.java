package com.vgrazi.jca.sprites;

import com.vgrazi.jca.context.RelativePosition;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class RunnerThreadSprite extends ThreadSprite{
    private int margin;
    private int leftBound;
    private int rightBound;
    private int width;
    private int topBound;
    private int bottomBound;
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

    @Value("${ball-radius}")
    protected int ballRadius;

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
        width = rightBound - leftBound;
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
        drawThreadCap(graphics);
    }

    /**
     * Draws the ball (or whatever) at the end of the thread line
     */
    protected void drawThreadCap(Graphics2D graphics) {
        graphics.setColor(getThreadContext().getColorByInstance(this));
        int yPos = getBallYPosition(leftBound, rightBound, topBound, bottomBound, this);
//        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength : 0;
        graphics.fillOval(getXPosition() - 8, yPos, ballRadius, ballRadius);
    }

//      <------------------------W------------------------->
//              (-W, 0)
//               ___________________________________(0,0)
//            . |lineStart         ^        lineEnd|    .
//       .      |                  |               |       .
//     .        |                  |               |         .
//    . ________|________________  | ______________|_________ .
//    .         |                  2R = yDelta     |          .
//     .        |                  |               |         .
//        .     |                  |               |       .
//             .| _________________V_______________|__  .

//             (-W, 2R)                              (0,2R)

    /**
     * Renders the ball at the correct position
     *
     * @param leftBound   the left-most position of the ellipse
     * @param rightBound  the right-most position of the ellipse
     * @param topBound    the y position of the top-most position of the ellipse
     * @param bottomBound the y position of the bottom-most position of the ellipse
     * @param sprite      the sprite we are animating
     */
    private int getBallYPosition(int leftBound, int rightBound, int topBound, int bottomBound, Sprite sprite) {
        int xPos = sprite.getXPosition();
        int ellipseRadius = (bottomBound - topBound) / 2;
        int xAxis = (bottomBound + topBound) / 2;
        // this is the x-position where the left semi-circle stops and the straight horizontal line start
        int lineStart = leftBound + ellipseRadius;
        // this is the x-position where the straight horizontal line ends and the right semi-circle starts
        int lineEnd = rightBound - ellipseRadius;
        int yPos;
        switch (sprite.getDirection()) {
            case right:
                if (xPos <= leftBound) {
                    yPos = xAxis;
                } else if (xPos < lineStart) {
                    int legLength = ellipseRadius - xPos;
                    yPos = xAxis + (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                } else if (xPos < lineEnd) {
                    yPos = topBound;
                } else { // we are in the right semi circle
                    int legLength = xPos - lineEnd;
                    yPos = xAxis - (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                }
                break;
            case left:
                if (xPos < lineStart) {
                    int legLength = ellipseRadius - xPos;
                    yPos = xAxis - (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                } else if (xPos < lineEnd) {
                    yPos = bottomBound;
                } else { // we are in the right bottom semi circle
                    int legLength = xPos - lineEnd;
                    yPos = xAxis + (int) Math.sqrt(ellipseRadius * ellipseRadius - legLength * legLength);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown direction-should never happen - did you add a direction besides left and right??");
        }
        yPos-= ballRadius/2;
        return yPos;
    }

}
