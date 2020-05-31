package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.slides.Slide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Component
public class ControlPanel extends JPanel {
    @Autowired
    private ThreadContext context;

    static int currentFontSize = 18;
    @Autowired
    public ControlPanel() {
        super(new GridLayout(2, 3));
        JButton aA = new JButton("Aa") {
            @Override
            public void setToolTipText(String text) {
                super.setToolTipText(text);
            }
        };
        aA.setToolTipText("Current font size:" + 20);
        addButton(aA, l1 -> {
            Slide slide = context.getSlide();
            currentFontSize +=2;
            if(currentFontSize > 24) {
                currentFontSize = 14;
            }
            aA.setToolTipText("Current font size:" + currentFontSize);
            slide.setSnippetFontSize(currentFontSize);
        }, "Current font size:" + currentFontSize);
        addButton(new JButton("||"), l -> context.setSpeed("pause"), "Pause");
        addButton(new JButton(">"), l ->  context.setSpeed("slow"), "Slow");
        addButton(new JButton(">>"),l ->  context.setSpeed("normal"), "Normal");
        addButton(new JButton("T"), l ->  context.setDisplayThreadNames(!context.isDisplayThreadNames()), "Display Thread Names");
        addButton(new JButton(":)"),l ->  context.toggleGraphics(), "Toggle Graphics");
    }

    private void addButton(JButton button, ActionListener l, String toolTipText) {
        add(button);
        button.addActionListener(l);
        button.setToolTipText(toolTipText);
    }

    public float getFontSize() {
        return currentFontSize;
    }
}
