package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.Sprite;
import com.vgrazi.jca.util.Parsers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.vgrazi.jca.util.Parsers.parseColor;

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

    @Value("${slide-label-font-name}")
    private String labelFontName;

    private int labelFontStyle;

    @Value("${slide-label-font-size}")
    private int labelFontSize;

    private Color monolithColor;
    private boolean hideMonolith;
    private Color slideLabelColor;
    private String slideLabel = "";

    @Value("${MONOLITH-COLOR}")
    public void setMonolithColor(String color) {
        this.monolithColor = parseColor(color);
    }

    @Value("${SLIDE-LABEL-COLOR}")
    public void setSlideLabelColor(String color) {
        this.slideLabelColor = parseColor(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        setOpaque(true);

        Graphics2D graphics = (Graphics2D) g;
        super.paintComponent(graphics);

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        paintSlideLabel(graphics);
        // todo: make this a case statement depending on the kind of monolith
        if (!hideMonolith) {
            paintMonolith(graphics);
        }
        graphics.setColor(Color.CYAN);

        List<Sprite> threads = threadContext.getAllSprites();
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(Sprite sprite, Graphics2D graphics) {
//        graphics.setStroke(sprite.getStroke());
        sprite.render(graphics);
    }

    private void paintSlideLabel(Graphics2D g) {
        g.setColor(slideLabelColor);
        g.setFont(new Font(labelFontName, labelFontStyle, labelFontSize));
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(slideLabel);
        int height = fm.getHeight();
        g.drawString(slideLabel, (rightBorder + leftBorder - width)/2, initialYPosition - 20-height/2 + fm.getDescent());
    }

    private void paintMonolith(Graphics2D g) {
        g.setColor(monolithColor);
        g.fill3DRect(leftBorder, initialYPosition - 20, rightBorder - leftBorder, 5000, true);
    }

    @Override
    public void afterPropertiesSet() {
    }

    @Value("${slide-label-font-style}")
    public void setFontStyle(String style) {
        labelFontStyle = Parsers.parseFontStyle(style);
    }

    /**
     * False by default, call this with true to prevent the monolith from drawing
     */
    public void hideMonolith(boolean b) {
        hideMonolith = b;
    }

    public void setSlideLabel(String slideLabel) {
        this.slideLabel = slideLabel;
    }
}
