package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import static com.vgrazi.jca.util.ColorParser.*;

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

    @Value("${BLOCKED_COLOR}")
    private String blockedColorString;
    private Color blockedColor;
    @Value("${RUNNABLE_COLOR}")
    private String runnableColorString;
    private Color runnableColor;
    @Value("${WAITING_COLOR}")
    private String waitingColorString;
    private Color waitingColor;
    @Value("${TIMED_WAITING_COLOR}")
    private String timedWaitingColorString;
    private Color timedWaitingColor;
    @Value("${TERMINATED_COLOR}")
    private String terminatedColorString;
    private Color terminatedColor;
    @Value("${DEFAULT_COLOR}")
    private String defaultColorString;
    private Color defaultColor;

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
        graphics.setStroke(new BasicStroke(3));
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(ThreadSprite sprite, Graphics2D graphics) {
        Color color = getColorByThreadState(sprite);

        graphics.setColor(color);
        graphics.drawLine(sprite.getXPosition() - arrowLength, sprite.getYPosition(), sprite.getXPosition(), sprite.getYPosition());
    }

    /**
     * We support coloring by different schemes. This scheme colors threads by their state, blue for blocked, green
     * for Runnable, etc.
     */
    private Color getColorByThreadState(ThreadSprite sprite) {
        Color color;
        Thread.State state = sprite.getThreadState();
        if(state == Thread.State.BLOCKED) {
            color = blockedColor;
        }
        else if(state == Thread.State.RUNNABLE) {
            color = runnableColor;
        }
        else if(state == Thread.State.WAITING) {
            color = waitingColor;
        }
        else if(state == Thread.State.TIMED_WAITING) {
            color = timedWaitingColor;
        }
        else if(state == Thread.State.TERMINATED) {
            color = terminatedColor;
        }
        else {
            color = defaultColor;
        }
        return color;
    }

    private void paintMutex(Graphics2D g) {
        g.setColor(Color.white);
        g.fill3DRect(leftBorder, initialYPosition - 20, rightBorder - leftBorder, 5000, true);
    }


    @Override
    public void afterPropertiesSet() {
        blockedColor = parseColor(blockedColorString);
        runnableColor = parseColor(runnableColorString);
        waitingColor = parseColor(waitingColorString);
        timedWaitingColor = parseColor(timedWaitingColorString);
        terminatedColor = parseColor(terminatedColorString);
    }
}
