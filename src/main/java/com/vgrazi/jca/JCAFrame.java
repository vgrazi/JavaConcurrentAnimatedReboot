package com.vgrazi.jca;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.vgrazi.jca.view.ThreadCanvas;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

@Component
public class JCAFrame extends JFrame {

    @Autowired
    private ThreadCanvas threadCanvas;

    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    @Value("${divider-location}")
    private int dividerLocation;

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
