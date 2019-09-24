package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Component
public class ThreadCanvas extends JPanel {

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


    @Override
    protected void paintComponent(Graphics g) {
        setOpaque(true);

        Graphics2D graphics = (Graphics2D) g;
        super.paintComponent(graphics);

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        // todo: make this a case statement depending on the kind of monolith
        if (true) {
            paintMutex(graphics);
        }
        graphics.setColor(Color.CYAN);

        List<ThreadSprite> threads = threadContext.getAllThreads();
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(ThreadSprite sprite, Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(3));
        graphics.drawLine(sprite.getXPosition() - arrowLength, sprite.getYPosition(), sprite.getXPosition(), sprite.getYPosition());
    }

    private void paintMutex(Graphics2D g) {
        g.setColor(Color.white);
        g.fill3DRect(leftBorder, initialYPosition - 20, rightBorder - leftBorder, 5000, true);
    }


}
