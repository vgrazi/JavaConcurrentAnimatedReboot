package com.vgrazi.jca.view;

import com.vgrazi.jca.context.Sprite;
import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.vgrazi.jca.util.ColorParser.parseColor;

@Component
public class ThreadCanvas extends JPanel implements InitializingBean {

    @Autowired
    private ThreadContext threadContext;

    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${initial-y-position}")
    private int initialYPosition;

    @Value("${arrow-length}")
    private int arrowLength;

    private Color monolithColor;

    @Value("${MONOLITH-COLOR}")
    public void setMonolithColor(String color) {
        this.monolithColor = parseColor(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        setOpaque(true);

        Graphics2D graphics = (Graphics2D) g;
        super.paintComponent(graphics);

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        // todo: make this a case statement depending on the kind of monolith
        if (true) {
            paintMonolith(graphics);
        }
        graphics.setColor(Color.CYAN);

        List<Sprite> threads = threadContext.getAllSprites();
        graphics.setStroke(new BasicStroke(4));
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(Sprite sprite, Graphics2D graphics) {
        sprite.render(graphics);
    }


    private void paintMonolith(Graphics2D g) {
        g.setColor(monolithColor);
        g.fill3DRect(leftBorder, initialYPosition - 20, rightBorder - leftBorder, 5000, true);
    }


    @Override
    public void afterPropertiesSet() {
    }
}
