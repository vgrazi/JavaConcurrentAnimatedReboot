package com.vgrazi.jca.slides;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class IntroSlide extends Slide {

    @Autowired
    private JPanel cardPanel;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Timer animationTimer;
    private BufferedImage meshImage;
    private int meshOffsetX = 0;
    private int meshOffsetY = 0;

    public void run() {
        reset();
        threadContext.addButton("reset()", this::reset);
        threadContext.setVisible();
        executor.schedule(this::reset, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void reset() {
        super.reset();
        resetImage();
        ((CardLayout) cardPanel.getLayout()).next(cardPanel);
    }

    public void resetImage() {
        loadMeshImage();
        startMeshAnimation();
    }

    private void loadMeshImage() {
        if (meshImage != null) return;
        try {
            URL url = getClass().getClassLoader().getResource("images/mesh.png");
            if (url != null) {
                meshImage = ImageIO.read(url);
            }
        } catch (IOException e) {
            // fall back to static image if loading fails
            setImage("images/mesh.png", 1);
        }
    }

    private void startMeshAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        meshOffsetX = 0;
        meshOffsetY = 0;
        animationTimer = new Timer(30, e -> {
            meshOffsetX = (meshOffsetX + 1) % Math.max(1, meshImage != null ? meshImage.getWidth() : 1);
            meshOffsetY = (meshOffsetY + 1) % Math.max(1, meshImage != null ? meshImage.getHeight() : 1);
            renderMeshFrame();
        });
        animationTimer.start();
    }

    private void renderMeshFrame() {
        JLabel label = getImageLabel();
        if (meshImage == null || label == null) return;
        int w = label.getWidth();
        int h = label.getHeight();
        if (w <= 0 || h <= 0) return;

        int imgW = meshImage.getWidth();
        int imgH = meshImage.getHeight();
        final double splitRatio = 0.35;
        int srcMidX = (int) (imgW * splitRatio);
        int dstMidX = (int) (w * splitRatio);
        int srcRightW = Math.max(1, imgW - srcMidX);

        // Compose as two strict panels: left static, right animated.
        BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        // Left panel: static (left half of source into left half of destination)
        g.drawImage(meshImage,
                0, 0, dstMidX, h,
                0, 0, srcMidX, imgH,
                null);

        // Right panel: top is original content, bottom is vertically flipped content.
        // Scroll through this mirrored texture so top/bottom fit cleanly.
        int textureH = imgH * 2;
        int offsetY = meshOffsetY % textureH;
        BufferedImage rightTexture = new BufferedImage(srcRightW, textureH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D textureGraphics = rightTexture.createGraphics();
        textureGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // Top half: original right-side source
        textureGraphics.drawImage(meshImage,
                0, 0, srcRightW, imgH,
                srcMidX, 0, imgW, imgH,
                null);
        // Bottom half: vertically flipped copy of right-side source
        textureGraphics.drawImage(meshImage,
                0, imgH, srcRightW, textureH,
                srcMidX, imgH, imgW, 0,
                null);
        textureGraphics.dispose();

        BufferedImage rightFrame = new BufferedImage(srcRightW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D rightGraphics = rightFrame.createGraphics();
        rightGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        rightGraphics.drawImage(rightTexture,
                0, -offsetY, srcRightW, textureH - offsetY,
                0, 0, srcRightW, textureH,
                null);
        rightGraphics.drawImage(rightTexture,
                0, textureH - offsetY, srcRightW, textureH - offsetY + textureH,
                0, 0, srcRightW, textureH,
                null);
        rightGraphics.dispose();
        g.drawImage(rightFrame,
                dstMidX, 0, w, h,
                0, 0, srcRightW, imgH,
                null);

        drawAnimatedWord(g, dstMidX, h);
        drawGitHubUrl(g, dstMidX, w, h);
        g.dispose();
        label.setIcon(new ImageIcon(frame));
    }

    private void drawGitHubUrl(Graphics2D graphics, int rightPanelStart, int totalWidth, int panelHeight) {
        final String url = "http://github/vgrazi/JavaConcurrentAnimatedReboot";
        int fontSize = 20;
        Font font = new Font("SansSerif", Font.PLAIN, fontSize);

        int rightPanelWidth = totalWidth - rightPanelStart;

        // Measure text on a throwaway opaque context so LCD AA metrics are accurate
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D pg = probe.createGraphics();
        pg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        pg.setFont(font);
        FontMetrics metrics = pg.getFontMetrics();
        pg.dispose();

        int textWidth  = metrics.stringWidth(url);
        int stripH     = metrics.getHeight() + 8;
        int textX      = Math.max(0, (rightPanelWidth - textWidth) / 2);
        int textY      = metrics.getAscent() + 4;
        int destStripY = (int) (panelHeight * 0.85) - metrics.getAscent() - 4;

        // Render onto TYPE_INT_RGB (opaque) — LCD subpixel AA requires an opaque surface
        BufferedImage strip = new BufferedImage(rightPanelWidth, stripH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = strip.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, rightPanelWidth, stripH);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(url, textX, textY);
        g.dispose();

        // Blit the opaque strip at pixel-perfect 1:1 scale — no interpolation blur
        graphics.drawImage(strip, rightPanelStart, destStripY, null);
    }

    private void drawAnimatedWord(Graphics2D graphics, int leftPanelWidth, int panelHeight) {
        final String word = "Animated";
        int fontSize = Math.max(28, Math.min(62, leftPanelWidth / 3));
        Font font = new Font("SansSerif", Font.BOLD | Font.ITALIC, fontSize);

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        int textWidth = metrics.stringWidth(word);
        int startX = Math.max(8, (leftPanelWidth - textWidth) / 2);
        int baseY = (int) (panelHeight * 0.50) - 50;
        long now = System.currentTimeMillis();

        int x = startX;
        for (int i = 0; i < word.length(); i++) {
            String letter = String.valueOf(word.charAt(i));
            int letterWidth = metrics.stringWidth(letter);
            float wobble = (float) Math.sin((now * 0.006) + (i * 0.8));
            int y = baseY + Math.round(wobble * 10f);
            float hue = (float) ((now * 0.00010 + (i * 0.08)) % 1.0);
            Color letterColor = Color.getHSBColor(hue, 0.85f, 1.0f);

            Graphics2D letterGraphics = (Graphics2D) g.create();
            AffineTransform transform = letterGraphics.getTransform();
            double angle = Math.sin((now * 0.0035) + (i * 0.6)) * 0.10;
            letterGraphics.rotate(angle, x + letterWidth / 2.0, y - metrics.getAscent() / 2.0);

            // soft glow behind each letter
            letterGraphics.setColor(new Color(letterColor.getRed(), letterColor.getGreen(), letterColor.getBlue(), 90));
            letterGraphics.drawString(letter, x - 1, y - 1);
            letterGraphics.drawString(letter, x + 1, y + 1);

            // main letter and subtle shadow for contrast on dark background
            letterGraphics.setColor(new Color(0, 0, 0, 170));
            letterGraphics.drawString(letter, x + 2, y + 2);
            letterGraphics.setColor(letterColor);
            letterGraphics.drawString(letter, x, y);

            letterGraphics.setTransform(transform);
            letterGraphics.dispose();
            x += letterWidth;
        }
        g.dispose();
    }
}
