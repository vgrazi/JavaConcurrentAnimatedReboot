package com.vgrazi.jca.sprites;

import java.awt.*;

/**
 * Sprite for structured-concurrency tasks that renders as a filled yellow circle
 * orbiting around a green oval in the monolith.
 * Extends OrbitalRunnerThreadSprite to share common orbital animation logic.
 */
public class StructuredConcurrencyThreadSprite extends OrbitalRunnerThreadSprite<Object> {
    private static final int CIRCLE_DIAMETER = 12;
    private static final Color YELLOW_DOT = Color.YELLOW;
    private static final Color GREEN_OVAL = Color.GREEN;

    public StructuredConcurrencyThreadSprite() {
        super(true);
    }

    @Override
    protected Color getOvalColor() {
        return GREEN_OVAL;
    }

    @Override
    protected void drawHead(Graphics2D graphics, int capOffset, int offset, int yPos) {
        if (isInMonolith()) {
            // Draw filled yellow circle while orbiting in the monolith
            OrbitalPosition pos = calculateOrbitalPosition();
            graphics.setColor(YELLOW_DOT);
            int circleX = (int) Math.round(pos.x) - CIRCLE_DIAMETER / 2;
            int circleY = (int) Math.round(pos.y) - CIRCLE_DIAMETER / 2;
            graphics.fillOval(circleX, circleY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        } else {
            // Outside monolith, draw a small filled circle
            graphics.setColor(YELLOW_DOT);
            int xpos = getXPosition() + getXOffset() - offset + capOffset;
            graphics.fillOval(xpos, yPos, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        }
    }
}





