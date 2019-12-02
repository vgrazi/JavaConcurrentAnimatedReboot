package com.vgrazi.jca.util;

import javax.swing.*;
import java.net.URL;

public class UIUtils {
    public ImageIcon getImageIcon(String imageName) {
        try {
            URL url = getClass().getClassLoader().getResource(imageName);
            ImageIcon imageIcon = new ImageIcon(url);
            return imageIcon;
        } catch (RuntimeException e) {
            System.out.println("UIUtils.getImageIcon Can't find image at " + imageName);
            throw e;
        }
    }

}
