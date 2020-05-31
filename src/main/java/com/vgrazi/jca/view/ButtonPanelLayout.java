package com.vgrazi.jca.view;

import java.awt.*;

/**
 * Lays out the buttons in proper FlowLayout fashion, one after another until the end of line is reached, then continuing on the next line
 */
public class ButtonPanelLayout extends FlowLayout {
    public ButtonPanelLayout(int hgap, int vgap) {
        super(LEFT, hgap, vgap);
    }

    @Override
    public void layoutContainer(Container target) {
        int containerWidth = target.getWidth();
        int height = 0;
        int delta = 0;
        int xPos = 0;
        int yPos = 0;
        for (Component component : target.getComponents()) {
            if(height == 0) {
                height = delta = (int)component.getPreferredSize().getHeight();
            }
            int componentHeight = (int)component.getPreferredSize().getHeight();
            int componentWidth = (int)component.getPreferredSize().getWidth();
            if(componentWidth + xPos > containerWidth - getHgap()) {
                yPos += delta + getVgap();
                xPos = getHgap();
            }
            component.setBounds(xPos, yPos, componentWidth, componentHeight);
            xPos += componentWidth + getHgap();
        }
        int newHeight = yPos + delta;
        if(newHeight== 0) {
            newHeight = 26;
        }
//        println("Setting bounds to " + target.getWidth() + "," + newHeight);
        target.setSize(target.getWidth(), newHeight);
    }
}
