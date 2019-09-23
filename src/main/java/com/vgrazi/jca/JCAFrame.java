package com.vgrazi.jca;


import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class JCAFrame extends JFrame {

    @Autowired
    private ThreadCanvas threadCanvas;

    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    public JCAFrame() throws HeadlessException {
        super("Java Concurrent Animated - Reboot!");
    }

    @PostConstruct
    public void afterPropertiesSet() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonPanel, threadCanvas);
        add(splitPane);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }
}
