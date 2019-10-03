package com.vgrazi.jca.view;

import java.awt.*;
import java.util.Arrays;

/**
 * Lays out buttons all same width and height, starting from the top of the target container, allowing some gap
 */
public class ButtonLayout extends FlowLayout {
    private int vgap;

    public ButtonLayout(int vgap) {
        super();
        this.vgap = vgap;
    }

    @Override
    public void layoutContainer(Container target) {
        int y = 2;
        int max = Arrays.stream(target.getComponents()).mapToInt(comp -> comp.getPreferredSize().width).max().orElse(20);
        for (Component component : target.getComponents()) {
            int height = component.getPreferredSize().height;
            component.setBounds(2, y, max, height);
            y += height + vgap;
        }
    }
}
