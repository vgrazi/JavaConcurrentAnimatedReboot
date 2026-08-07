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
import static com.vgrazi.jca.util.StringUtils.isBlank;

@Component
public class ThreadCanvas extends JPanel implements InitializingBean {
    @Autowired
    private ThreadContext threadContext;

    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${monolith-right-border}")
    private int rightBorder;

    /**
     * We create a copy so that if we reset the right border to a thin monolith, we have the original right border
     */
    @Value("${monolith-right-border}")
    private int rightDefaultBorder;

    @Value("${initial-y-position}")
    private int initialYPosition;

    @Value("${initial-bottom-y-position}")
    private int initialBottomYPosition;

    private String bottomLabel;
    @Value("${arrow-length}")
    private int arrowLength;

    @Value("${slide-label-font-name}")
    private String labelFontName;

    private int labelFontStyle;

    @Value("${slide-label-font-size}")
    private int labelFontSize;

    @Value("${slide-bottom-label-font-name}")
    private String bottomLabelFontName;

    private int bottomLabelFontStyle;

    @Value("${slide-bottom-label-font-size}")
    private int bottomLabelFontSize;

    private Color monolithColor;
    private boolean hideMonolith;
    private Color slideLabelColor;
    private Color bottomLabelColor;
    private String[] slideLabel = {"",""};

    @Value("${MONOLITH-COLOR}")
    public void setMonolithColor(String color) {
        this.monolithColor = parseColor(color);
    }

    @Value("${SLIDE-LABEL-COLOR}")
    public void setSlideLabelColor(String color) {
        this.slideLabelColor = parseColor(color);
    }

    @Value("${BOTTOM-LABEL-COLOR}")
    public void setBottomLabelColor(String color) {
        this.bottomLabelColor = parseColor(color);
    }

    public int getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(int rightBorder) {
        this.rightBorder = rightBorder;
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

        paintBottomLabel(graphics);
        List<Sprite> threads = threadContext.getAllSprites();
        threads.forEach(sprite -> render(sprite, graphics));
        graphics.dispose();
    }

    private void render(Sprite sprite, Graphics2D graphics) {
        Stroke stroke = sprite.getStroke();
        graphics.setStroke(stroke);
        sprite.render(graphics);
    }

    private void paintSlideLabel(Graphics2D g) {
        g.setColor(slideLabelColor);
        g.setFont(new Font(labelFontName, labelFontStyle, labelFontSize));
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(slideLabel[0]);
        int height = fm.getHeight();
        if(isBlank(slideLabel[1])){
            g.drawString(slideLabel[0], (rightBorder + leftBorder - width) / 2, initialYPosition - 20 - height / 2 + fm.getDescent());
        }
        else {

            int width2 = fm.stringWidth(slideLabel[1]);
            g.drawString(slideLabel[0], (rightBorder + leftBorder - width) / 2, initialYPosition - 20 -3* height / 2 + fm.getDescent()+10);
            g.drawString(slideLabel[1], (rightBorder + leftBorder - width2) / 2, initialYPosition - 20 - height / 2 + fm.getDescent()+10);
        }
    }

    private void paintBottomLabel(Graphics2D g) {
        if (bottomLabel != null) {
            String[] split = bottomLabel.split("\n");
            g.setColor(bottomLabelColor);
            g.setFont(new Font(bottomLabelFontName, bottomLabelFontStyle, bottomLabelFontSize));
            FontMetrics fm = g.getFontMetrics();
            int height = fm.getHeight();
            int fontHeight = 20 + height / 2 - fm.getDescent();
            for (int i = 0; i < split.length; i++) {
                int width = fm.stringWidth(split[i]);
                g.drawString(split[i], (rightBorder + leftBorder - width) / 2, initialBottomYPosition + (i-2) * fontHeight);
            }
            g.fillRect(leftBorder + 2, initialBottomYPosition - fontHeight + 2,  rightBorder - leftBorder - 4, 3);
        }
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

    @Value("${slide-bottom-label-font-style}")
    public void setBottomFontStyle(String style) {
        bottomLabelFontStyle = Parsers.parseFontStyle(style);
    }


    public void setSlideLabel(String slideLabel) {
        setSlideLabel(slideLabel, 0);
        setSlideLabel("", 1);
    }

    public void setSlideLabel(String slideLabel, int line) {
        this.slideLabel[line] = slideLabel;
    }

    public void setBottomLabel(String label) {
        this.bottomLabel = label;
    }

    public int getLeftBorder() {
        return leftBorder;
    }

    /**
     * False by default, call this with true to prevent the monolith from drawing
     */
    public void hideMonolith(boolean b) {
        hideMonolith = b;
    }

    public void setThinMonolith() {
        setRightBorder(getLeftBorder() + 20);
    }

    public void setStandardMonolith() {
        setRightBorder(rightDefaultBorder);
    }
}
