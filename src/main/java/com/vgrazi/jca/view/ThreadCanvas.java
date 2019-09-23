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

    @Value("${arrow-length}")
    private int arrowLength;


    @Override
    protected void paintComponent(Graphics g) {
        setOpaque(true);
        Graphics2D graphics = (Graphics2D) g;
        super.paintComponent(graphics);
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        graphics.setColor(Color.white);
        List<ThreadSprite> threads = threadContext.getAllThreads();
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(ThreadSprite sprite, Graphics graphics) {
        graphics.drawLine(sprite.getXPosition(), sprite.getYPosition(),
                sprite.getXPosition() + arrowLength, sprite.getYPosition());

    }


}
