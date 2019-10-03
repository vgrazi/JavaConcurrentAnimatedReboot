package com.vgrazi.jca;


import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.slides.CompletableFutureSlide;
import com.vgrazi.jca.slides.PhaserSlide;
import com.vgrazi.jca.slides.Slide;
import com.vgrazi.jca.slides.SynchronizedSlide;
import com.vgrazi.jca.view.ButtonLayout;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    ThreadContext threadContext;

    @Autowired
    private ThreadCanvas threadCanvas;
    @Autowired()
    private SynchronizedSlide synchronizedSlide;

    @Autowired()
    private PhaserSlide phaserSlide;

    @Autowired()
    private CompletableFutureSlide completableFutureSlide;

    @Value("${menu-button-vgap}")
    private int vgap;

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
        menu.setLayout(new ButtonLayout(vgap));

        addButton("Synchronized", synchronizedSlide, menu);
        addButton("Phaser", phaserSlide, menu);
        addButton("CompletableFuture", completableFutureSlide, menu);

        menu.setBackground(Color.cyan);
        JSplitPane wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menu, rightSide);

        ComponentAdapter adapter = new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                animationAndSnippet.setDividerLocation(.6);
                wholePane.setDividerLocation(100);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                animationAndSnippet.setDividerLocation(.6);
                wholePane.setDividerLocation(150);
            }
        };
        addComponentListener(adapter);
        menu.addComponentListener(adapter);
        add(wholePane);
    }

    private void addButton(String label, Slide slide, JPanel menu) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            buttonPanel.removeAll();
            repaint();
            slide.run();
        });
        menu.add(button);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }
}
