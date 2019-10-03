package com.vgrazi.jca.context;

import java.awt.*;

public class WriteThreadSprite<S> extends ThreadSprite<S> {
    @Override
    protected void drawThreadCap(Graphics2D g) {
        Graphics2D graphics = (Graphics2D) g.create();
        int x = getXPosition();
        int y = getYPosition();
        Stroke s = new BasicStroke(2.0f, // Width
                 BasicStroke.CAP_SQUARE, // End cap
                 BasicStroke.JOIN_MITER, // Join style
                 10.0f, // Miter limit
                 new float[] {2.0f,4.0f}, // Dash pattern
                 0.0f); // Dash phase
        // docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm
        graphics.setStroke(s);
        graphics.setColor(Color.yellow);
        graphics.fillPolygon(
                new int[]{x - 12,    x,  x + 12,  x},
                new int[]{y,        y - 6,  y,      y + 6},
                4);
        graphics.setColor(Color.red);
        graphics.drawPolygon(
                new int[]{x - 12,    x,  x + 12,  x},
                new int[]{y,        y - 6,  y,      y + 6},
                4);
        graphics.dispose();
    }
}
