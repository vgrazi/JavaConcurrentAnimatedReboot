package com.vgrazi.jca;


import com.vgrazi.jca.slides.SynchronizedSlide;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Component
public class JCAFrame extends JFrame {

    /*

    .....................................................
    .                 .     Buttons                     .
    .                 ...................................
    .   Menu          .   Messages                      .
    .                 ...................................
    .                 .                .                .
    .                 .   Animation    .  Snippet       .
    .                 .                .                .
    .                 .                .                .
    .....................................................



     */

    @Autowired
    private ThreadCanvas threadCanvas;
    @Autowired()
    private SynchronizedSlide synchronizedSlide;

    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JLabel messages = new JLabel();

    public JCAFrame() throws HeadlessException {
        super("Java Concurrent Animated - Reboot!");
    }

    @PostConstruct
    public void afterPropertiesSet() {
        JPanel snippetPanel = new JPanel();
        snippetPanel.setBackground(Color.yellow);

        JSplitPane animationAndSnippet = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, threadCanvas, snippetPanel);
        animationAndSnippet.setDividerLocation(.67);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messages);
        messages.setText("this is the message label");
        JPanel buttonsAndMessages = new JPanel(new GridLayout(2, 1));
        buttonsAndMessages.add(buttonPanel);
        buttonsAndMessages.add(messagePanel);

        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonsAndMessages, animationAndSnippet);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        Button aSynchronized = new Button("Synchronized");
        aSynchronized.addActionListener(e -> {
            synchronizedSlide.run();
        });
        menu.add(aSynchronized);
        menu.add(new Button("Phaser"));
        menu.add(new Button("CompletableFuture"));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.add(new Button(""));
        menu.setBackground(Color.cyan);


        JSplitPane wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menu, rightSide);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                animationAndSnippet.setDividerLocation(.6);
                wholePane.setDividerLocation(.20);
            }
        });
        add(wholePane);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }
}
