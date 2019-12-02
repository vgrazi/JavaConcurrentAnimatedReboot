package com.vgrazi.jca.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

@Component
public class UIUtils {

    @Autowired
    private JPanel cardPanel;

    public void setImage(String imageName, JLabel jlabel) {
        ImageIcon imageIcon = getImageIcon(imageName);
        jlabel.setIcon(imageIcon);
        Image image = imageIcon.getImage();
        int width = jlabel.getWidth();
        int height = jlabel.getHeight();
        Image scaledImage = getScaledImage(image, width, height);
        if (scaledImage != null) {
            imageIcon.setImage(scaledImage);
            jlabel.setIcon(imageIcon);
        }
    }

    private ImageIcon getImageIcon(String imageName) {
        try {
            URL url = getClass().getClassLoader().getResource(imageName);
            ImageIcon imageIcon = new ImageIcon(url);
            return imageIcon;
        } catch (RuntimeException e) {
            System.out.println("UIUtils.getImageIcon Can't find image at " + imageName);
            throw e;
        }
    }

    private Image getScaledImage(Image srcImg, int width, int height) {
        if(width <= 0 || height <= 0) {
            return null;
        }
        int imageWidth = srcImg.getWidth(null);
        int imageHeight = srcImg.getHeight(null);
        double aspectRatio = imageWidth / (double) imageHeight;
        double labelRatio = cardPanel.getWidth() / (double) cardPanel.getHeight();
        if(aspectRatio < labelRatio) {
            // use the label height: w/label_height = aspectRatio
            width = (int) (aspectRatio * height);
        }
        else {
            // use the label width: label_width/h = aspectRatio
            height = (int) (width/aspectRatio);
        }
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, width, height, null);
        g2.dispose();

        return resizedImg;
    }

}
