package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Component
public class ControlPanel extends JPanel {
    @Autowired
    private ThreadContext context;

    static int nextSize = 10;
    @Autowired
    private SnippetCanvas snippetCanvas;
    public ControlPanel() {
        super(new GridLayout(2, 3));
        addButton(new JButton("Aa"), l1 -> {
            snippetCanvas.setFontSize(nextSize);
            nextSize +=2;
            if(nextSize > 20) {
                nextSize = 10;
            }
        }, "font size");
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
}
