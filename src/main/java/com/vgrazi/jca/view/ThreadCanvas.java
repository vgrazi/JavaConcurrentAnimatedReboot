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
    private boolean highlightBoxVisible;
    private int highlightBoxX;
    private int highlightBoxY;
    private int highlightBoxWidth;
    private int highlightBoxHeight;
    private Color highlightBoxColor = Color.green;
    private boolean highlightArrowVisible;
    private boolean highlightArrowUseCustomStart;
    private int highlightArrowStartX;
    private int highlightArrowCenterY;
    private Color highlightArrowColor = Color.blue;
    private boolean fading;
    private float fadeAlpha;
    private Timer fadeTimer;
    private boolean animatingArrow;
    private int arrowAnimationOffset;
    private Timer arrowAnimationTimer;

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
        paintHighlightBox(graphics);
        graphics.dispose();
    }

    private void paintHighlightBox(Graphics2D graphics) {
        if (highlightBoxVisible || highlightArrowVisible) {
            Graphics2D graphics1 = (Graphics2D) graphics.create();
            graphics1.setStroke(new BasicStroke(3f));
            if (fading) {
                graphics1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            }
            if (highlightBoxVisible) {
                graphics1.setColor(highlightBoxColor);
                graphics1.drawRect(highlightBoxX, highlightBoxY, highlightBoxWidth, highlightBoxHeight);
            }
            if (highlightArrowVisible) {
                graphics1.setColor(highlightArrowColor);
                int yCenter = highlightArrowUseCustomStart ? highlightArrowCenterY : highlightBoxY + highlightBoxHeight / 2;
                int arrowStartX = highlightArrowUseCustomStart ? highlightArrowStartX : highlightBoxX + highlightBoxWidth;
                if (animatingArrow) {
                    arrowStartX += arrowAnimationOffset;
                }
                int arrowLength = 50;
                int arrowEndX = arrowStartX + arrowLength;
                graphics1.drawLine(arrowStartX, yCenter, arrowEndX, yCenter);
                int arrowHead = 8;
                graphics1.drawLine(arrowEndX, yCenter, arrowEndX - arrowHead, yCenter - arrowHead);
                graphics1.drawLine(arrowEndX, yCenter, arrowEndX - arrowHead, yCenter + arrowHead);
            }
            graphics1.dispose();
        }
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
    public void showHighlightBox(int x, int y, int width, int height, Color color) {
        showHighlightBox(x, y, width, height, color, false);
    }

    public void showHighlightBox(int x, int y, int width, int height, Color color, boolean withRightArrow) {
        this.highlightBoxX = x;
        this.highlightBoxY = y;
        this.highlightBoxWidth = width;
        this.highlightBoxHeight = height;
        this.highlightBoxColor = color;
        this.highlightArrowVisible = withRightArrow;
        this.highlightArrowUseCustomStart = false;
        this.highlightBoxVisible = true;
        repaint();
    }

    public void showHighlightArrow(int startX, int centerY, Color color) {
        this.highlightArrowColor = color;
        this.highlightArrowVisible = true;
        this.highlightArrowUseCustomStart = true;
        this.highlightArrowStartX = startX;
        this.highlightArrowCenterY = centerY;
        this.highlightBoxVisible = false;
        repaint();
    }

    public void clearHighlightBox() {
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }
        if (arrowAnimationTimer != null) {
            arrowAnimationTimer.stop();
            arrowAnimationTimer = null;
        }
        this.highlightBoxVisible = false;
        this.highlightArrowVisible = false;
        this.highlightArrowUseCustomStart = false;
        this.fading = false;
        this.fadeAlpha = 1.0f;
        this.animatingArrow = false;
        this.arrowAnimationOffset = 0;
        repaint();
    }

    public void fadeHighlightBox() {
        if (!highlightBoxVisible) {
            return;
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        if (arrowAnimationTimer != null) {
            arrowAnimationTimer.stop();
        }
        fading = true;
        fadeAlpha = 1.0f;
        animatingArrow = true;
        arrowAnimationOffset = 0;
        fadeTimer = new Timer(20, e -> {
            fadeAlpha -= 0.05f;
            if (fadeAlpha <= 0) {
                fadeAlpha = 0;
                clearHighlightBox();
            } else {
                repaint();
            }
        });
        fadeTimer.start();
        arrowAnimationTimer = new Timer(20, e -> {
            arrowAnimationOffset += 3;
            if (arrowAnimationOffset >= 100) {
                arrowAnimationTimer.stop();
            } else {
                repaint();
            }
        });
        arrowAnimationTimer.start();
    }
}
