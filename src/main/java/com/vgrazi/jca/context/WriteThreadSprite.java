package com.vgrazi.jca.context;

import java.awt.*;

public class WriteThreadSprite<S> extends ThreadSprite<S> {
    @Override
    protected void drawThreadCap(Graphics2D g) {
        Graphics2D graphics = (Graphics2D) g.create();
        int x = getXPosition();
        int y = getYPosition();
        Stroke s = new BasicStroke(1.0f, // Width
                BasicStroke.CAP_SQUARE, // End cap
                BasicStroke.JOIN_MITER, // Join style
                10.0f, // Miter limit
                new float[]{2.0f, 4.0f}, // Dash pattern
                0.0f); // Dash phase
        // docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm
        graphics.setStroke(s);
        graphics.setColor(Color.yellow);
        Polygon polygon = new Polygon(
                new int[]{x - 11, x + 1, x - 11},
                new int[]{y - 6, y, y + 6},
                3);
        graphics.fillPolygon(polygon);
        graphics.setColor(Color.red);
        graphics.drawPolygon(polygon);
        graphics.dispose();
    }
}
