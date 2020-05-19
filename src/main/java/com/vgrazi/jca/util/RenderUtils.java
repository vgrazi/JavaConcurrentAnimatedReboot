package com.vgrazi.jca.util;

import com.vgrazi.jca.sprites.Sprite;

public class RenderUtils {
    /**
     * Renders the ball at the correct position
     *  @param leftBound   the left-most position of the ellipse
     * @param rightBound  the right-most position of the ellipse
     * @param topBound    the y position of the top-most position of the ellipse
     * @param bottomBound the y position of the bottom-most position of the ellipse
     * @param ballDiameter the diameter of the actual cap
     * @param sprite      the sprite we are animating
     */
    public static int getCapYPosition(int leftBound, int rightBound, int topBound, int bottomBound, int ballDiameter, Sprite sprite) {
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
        yPos -= ballDiameter / 2;
//        System.out.printf("old x-pos:%d new xPos:%d line-start:%d  ypos:%d  ellipse radius:%d  xaxis:%d ball-diameter:%d%n",
//                 this.xPosition, xPos + ballDiameter/2, lineStart, yPos + ballDiameter /2, ellipseRadius, xAxis, ballDiameter);
        return yPos;
    }
}
