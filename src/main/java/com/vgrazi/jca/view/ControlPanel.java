package com.vgrazi.jca.view;

import com.vgrazi.jca.context.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionListener;

@Component
public class ControlPanel extends JPanel {
    @Autowired
    private ThreadContext context;
    public ControlPanel() {
        addButton(new JButton("||"), l1 -> context.setSpeed("pause"), "Pause");
        addButton(new JButton(">"), l1 -> context.setSpeed("slow"), "Slow");
        addButton(new JButton(">>"),l->context.setSpeed("normal"), "Normal");
        addButton(new JButton("T"),l->{}, "Display Thread Names");
    }

    private void addButton(JButton button, ActionListener l, String toolTipText) {
        add(button);
        button.addActionListener(l);
        JToolTip toolTip = button.createToolTip();
        toolTip.setTipText(toolTipText);
    }
}
