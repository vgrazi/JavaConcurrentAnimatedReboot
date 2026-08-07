package com.vgrazi.jca.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;

@Component
public class UIUtils {
    private static Logger logger = Logger.getLogger("UIUtils");
    private static void println(String message) {
        logger.info(message);
    }

    @Autowired
    private JPanel cardPanel;

    public void setImage(String imageName, JLabel jlabel, float scaling) {
        ImageIcon imageIcon = getImageIcon(imageName);
        jlabel.setIcon(imageIcon);
        Image image = imageIcon.getImage();
        int width = (int) (jlabel.getWidth()*.7);
        int height = (int) (jlabel.getHeight()*.7);
        Image scaledImage = getScaledImage(image, width, height, scaling);
        if (scaledImage != null) {
            imageIcon.setImage(scaledImage);
            jlabel.setIcon(imageIcon);
        }
    }

    public ImageIcon getImageIcon(String imageName) {
        try {
            URL url = getClass().getClassLoader().getResource(imageName);
            ImageIcon imageIcon = new ImageIcon(url);
            return imageIcon;
        } catch (RuntimeException e) {
            println("UIUtils.getImageIcon Can't find image at " + imageName);
            throw e;
        }
    }
    public Image getImage(String imageName) {
        try {
            ImageIcon imageIcon = getImageIcon(imageName);
            return imageIcon.getImage();
        } catch (RuntimeException e) {
            println("UIUtils.getImageIcon Can't find image at " + imageName);
            throw e;
        }
    }

    private Image getScaledImage(Image srcImg, int width, int height, float scaling) {
        if(width <= 0 || height <= 0) {
            return null;
        }
        int imageWidth = srcImg.getWidth(null);
        int imageHeight = srcImg.getHeight((img, infoflags, x, y, width1, height1) -> false);
        double aspectRatio = imageWidth / (double) imageHeight;
        double labelRatio = cardPanel.getWidth() / (double) cardPanel.getHeight();
        if(aspectRatio < labelRatio) {
            // use the label width: label_width/image-height = aspectRatio
            width = cardPanel.getWidth();
            height = (int) (width/aspectRatio);
        }
        else {
            // use the label height: image-width/label_height = aspectRatio
            height = cardPanel.getHeight();
            width = (int) (aspectRatio * height);
        }
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.black);
        g2.fillRect(0,0, 5000,5000);
        g2.drawImage(srcImg, (int) (width * (1-scaling)/2), (int) (height*(1-scaling)/2), (int) (width*scaling), (int) (height*scaling), null);
        g2.dispose();

        return resizedImg;
    }

    public static Color applyAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getBlue(), color.getGreen(), alpha);
    }
}
