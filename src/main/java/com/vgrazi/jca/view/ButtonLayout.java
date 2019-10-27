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
        int width = Arrays.stream(target.getComponents()).mapToInt(comp -> comp.getPreferredSize().width).max().orElse(20);
        for (Component component : target.getComponents()) {
            if(component instanceof ControlPanel) {
                layoutControlPanel((ControlPanel)component, target, width);
            }
            else {
                int height = component.getPreferredSize().height;
                component.setBounds(2, y, width, height);
                y += height + vgap;
            }
        }
    }

    private void layoutControlPanel(ControlPanel controlPanel, Container target, int width) {
        int height = controlPanel.getPreferredSize().height;
        int y = target.getHeight() - height - 2;
        controlPanel.setBounds(2, y, width, height);

    }
}
