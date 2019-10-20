package com.vgrazi.jca.sprites;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

public class WriteThreadSprite<S> extends RunnerThreadSprite implements InitializingBean {

    @Autowired
    private Stroke writerStroke;
    @Autowired
    private Stroke writerHeadStroke;

    @Override
    public void render(Graphics2D graphics) {
        super.render(graphics);
    }

    @Override
    protected void drawThreadCap(Graphics2D g) {
        Graphics2D graphics = (Graphics2D) g.create();
        int yPos = getCapYPosition(leftBound, rightBound, topBound, bottomBound, this) + 5;
        int offset = isRetreating() && getDirection() == Direction.left ? arrowLength:0;
        int x = getXPosition() -offset;

        // docstore.mik.ua/orelly/java-ent/jfc/ch04_05.htm
        graphics.setStroke(writerHeadStroke);
        graphics.setColor(Color.yellow);
        Polygon polygon;
        if(getDirection() == Direction.right) {
            polygon = new Polygon(
                    new int[]{x - 11, x + 1, x - 11},
                    new int[]{yPos - 6, yPos, yPos + 6},
                    3);
        }
        else {
            polygon = new Polygon(
                    new int[]{x + 1, x - 11, x + 1},
                    new int[]{yPos - 6, yPos, yPos + 6},
                    3);
        }
        graphics.fillPolygon(polygon);
        graphics.setColor(Color.red);
        graphics.drawPolygon(polygon);
        graphics.dispose();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        setStroke(writerStroke);
    }
}
