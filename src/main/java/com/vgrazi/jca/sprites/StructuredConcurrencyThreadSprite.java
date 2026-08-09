package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.ThreadUtils;

import java.awt.*;

/**
 * Sprite for structured-concurrency tasks that renders as a filled yellow circle
 * orbiting around a green oval in the monolith.
 */
public class StructuredConcurrencyThreadSprite extends VirtualRunnerThreadSprite {
    private static final int CIRCLE_DIAMETER = 12;
    private static final Color YELLOW_DOT = Color.YELLOW;
    private static final Color GREEN_OVAL = Color.GREEN;

    public StructuredConcurrencyThreadSprite() {
        super(-10, "o", true);
    }

    @Override
    public void render(Graphics2D graphics) {
        // Draw green oval for the task orbit
        graphics.setColor(GREEN_OVAL);
        if(isInMonolith()){
            graphics.drawArc(-100+leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(-100+rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(-100+lineStart + getXOffset(), topBound, -100 + lineEnd + getXOffset(), topBound);
            graphics.drawLine(-100+lineStart + getXOffset(), bottomBound, -100 + lineEnd + getXOffset(), bottomBound);
        }else{
            // render the runner thread before it enters the monolith
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(-100+xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
        }

        renderMessage(graphics);
        drawThreadCap(graphics);
        renderInterruptedFlag(graphics);
    }

    @Override
    protected void drawHead(Graphics2D graphics, int capOffset, int offset, int yPos) {
        if (isInMonolith()) {
            // Draw filled yellow circle while orbiting in the monolith
            int renderOffset = -100;
            int cxLeft = renderOffset + leftBound + getXOffset() + ellipseRadius;
            int cxRight = renderOffset + rightBound + getXOffset() - ellipseRadius;
            int cy = (topBound + bottomBound) / 2;
            int radius = ellipseRadius;
            int topY = topBound;
            int bottomY = bottomBound;

            boolean hasCarrier = !ThreadUtils.getCarrier(getThread()).isEmpty();

            double x;
            double y;
            if (hasCarrier || animateWithoutCarrier) {
                // Animate around the oval when mounted on a carrier, or when a slide
                // explicitly opts into render-only animation independent of carrier state.
                double topLength = Math.max(1, cxRight - cxLeft);
                double arcLength = Math.PI * radius;
                double perimeter = topLength + arcLength + topLength + arcLength;

                double speedPixelsPerSecond = 90.0;
                double phaseOffset = (getID() * 37.0) % perimeter;
                double distance = (System.nanoTime() / 1_000_000_000.0 * speedPixelsPerSecond + phaseOffset) % perimeter;

                if (distance < topLength) {
                    x = cxLeft + distance;
                    y = topY;
                } else if (distance < topLength + arcLength) {
                    double t = (distance - topLength) / arcLength;
                    double angle = -Math.PI / 2 + t * Math.PI;
                    x = cxRight + radius * Math.cos(angle);
                    y = cy + radius * Math.sin(angle);
                } else if (distance < topLength + arcLength + topLength) {
                    double t = distance - topLength - arcLength;
                    x = cxRight - t;
                    y = bottomY;
                } else {
                    double t = (distance - topLength - arcLength - topLength) / arcLength;
                    double angle = Math.PI / 2 + t * Math.PI;
                    x = cxLeft + radius * Math.cos(angle);
                    y = cy + radius * Math.sin(angle);
                }
            } else {
                // No carrier — park the circle at the top-left of the oval, stationary
                x = cxLeft;
                y = topY;
            }

            // Draw filled yellow circle
            graphics.setColor(YELLOW_DOT);
            int circleX = (int) Math.round(x) - CIRCLE_DIAMETER / 2;
            int circleY = (int) Math.round(y) - CIRCLE_DIAMETER / 2;
            graphics.fillOval(circleX, circleY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        } else {
            // Outside monolith, draw a small filled circle
            graphics.setColor(YELLOW_DOT);
            int xpos = getXPosition() + getXOffset() - offset + capOffset;
            graphics.fillOval(xpos, yPos, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        }
    }
}





