package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.ThreadUtils;

import java.awt.*;

/**
 * Abstract base class for runner thread sprites that animate objects orbiting around an oval.
 * Handles the common rendering logic for the oval track and orbital animation calculations.
 * Subclasses implement the specific rendering of the orbiting object (character, circle, etc).
 */
public abstract class OrbitalRunnerThreadSprite<S> extends RunnerThreadSprite<S> {
    protected final boolean animateWithoutCarrier;
    public static final Font CARRIER_FONT = new Font("Arial", Font.PLAIN, 24);

    public OrbitalRunnerThreadSprite(boolean animateWithoutCarrier) {
        this.animateWithoutCarrier = animateWithoutCarrier;
    }

    @Override
    public void render(Graphics2D graphics) {
        // Draw the orbital track
        graphics.setColor(getOvalColor());
        if(isInMonolith()){
            graphics.drawArc(-100+leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(-100+rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(-100+lineStart + getXOffset(), topBound, -100 + lineEnd + getXOffset(), topBound);
            graphics.drawLine(-100+lineStart + getXOffset(), bottomBound, -100 + lineEnd + getXOffset(), bottomBound);
        } else {
            // render the runner thread before it enters the monolith
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(-100+xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
        }

        renderMessage(graphics);
        drawThreadCap(graphics);
        renderInterruptedFlag(graphics);
    }

    /**
     * Returns the color to use for drawing the oval track.
     * Override to customize the track color for different sprite types.
     */
    protected abstract Color getOvalColor();

    /**
     * Calculates the position of an object orbiting around the oval.
     * @return A Point2D.Double with the x and y coordinates
     */
    protected OrbitalPosition calculateOrbitalPosition() {
        if (!isInMonolith()) {
            return new OrbitalPosition(0, 0, false); // Not used outside monolith
        }

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
            // No carrier — park at the top-left of the oval, stationary
            x = cxLeft;
            y = topY;
        }

        return new OrbitalPosition(x, y, true);
    }

    /**
     * Holder class for orbital position calculations
     */
    protected static class OrbitalPosition {
        public final double x;
        public final double y;
        public final boolean isValid;

        public OrbitalPosition(double x, double y, boolean isValid) {
            this.x = x;
            this.y = y;
            this.isValid = isValid;
        }
    }
}

