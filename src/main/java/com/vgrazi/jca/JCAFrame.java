package com.vgrazi.jca;


import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.slides.*;
import com.vgrazi.jca.view.ButtonLayout;
import com.vgrazi.jca.view.ControlPanel;
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
    ControlPanel controlPanel = new ControlPanel();

    @Autowired
    private ThreadCanvas threadCanvas;
    @Autowired
    private SynchronizedSlide synchronizedSlide;

    @Autowired
    private ExecutorsSlide executorsSlide;

    @Autowired
    private PhaserSlide phaserSlide;

    @Autowired
    private CyclicBarrierSlide cyclicBarrierSlide;

    @Autowired
    private CountdownLatchSlide countdownLatchSlide;

    @Autowired
    private CompletableFutureSlide completableFutureSlide;

    @Autowired
    private ReadWriteLockSlide readWriteLockSlide;

    @Autowired
    private TransferQueueSlide transferQueueSlide;

    @Autowired
    private SemaphoreSlide semaphoreSlide;

    @Autowired
    private ReentrantLockSlide reentrantLockSlide;

    @Autowired
    private AtomicIntegerSlide atomicIntegerSlide;

    @Autowired
    private StampedLockSlide stampedLockSlide;

    @Autowired
    private BlockingQueueSlide blockingQueueSlide;

    @Value("${menu-button-vgap}")
    private int vgap;

    @Value("${animation-pane-to-snippet-divider-ratio}")
    private double animationPaneToSnippetDividerRatio;

    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    @Autowired
    private JLabel messages;
    private final JPanel menuPanel = new JPanel();

    @Autowired
    private JTextPane snippetPanel;

    public JCAFrame() throws HeadlessException {
        super("Java Concurrent Animated - Reboot!");
    }

    @PostConstruct
    public void afterPropertiesSet() {
        snippetPanel.setBackground(Color.white);
        JScrollPane snippetScrollPane = new JScrollPane(snippetPanel);
        snippetScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        snippetScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JSplitPane animationAndSnippet = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, threadCanvas, snippetScrollPane);
        animationAndSnippet.setDividerSize(2);
        animationAndSnippet.setDividerLocation(.67);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messages);
        messages.setText("this is the message label");
        JPanel buttonsAndMessages = new JPanel(new GridLayout(2, 1));
        buttonsAndMessages.add(buttonPanel);
        buttonsAndMessages.add(messagePanel);

        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonsAndMessages, animationAndSnippet);
        rightSide.setDividerSize(2);

        menuPanel.setLayout(new ButtonLayout(vgap));

        addButton("Executors", executorsSlide);
        addButton("Synchronized", synchronizedSlide);
        addButton("ReentrantLock", reentrantLockSlide);
        addButton("Phaser", phaserSlide);
        addButton("CyclicBarrier", cyclicBarrierSlide);
        addButton("CountdownLatch", countdownLatchSlide);
        addButton("CompletableFuture", completableFutureSlide);
        addButton("ReadWriteLock", readWriteLockSlide);
        addButton("StampedLock", stampedLockSlide);
        addButton("BlockingQueue", blockingQueueSlide);
        addButton("TransferQueue", transferQueueSlide);
        addButton("Semaphore", semaphoreSlide);
        addButton("AtomicInteger", atomicIntegerSlide);

        menuPanel.add(controlPanel);

        menuPanel.setBackground(Color.black);
        JSplitPane wholePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, rightSide);
        wholePane.setDividerSize(2);

        ComponentAdapter adapter = new ComponentAdapter() {
            private final int location = controlPanel.getPreferredSize().width + 6;

            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                animationAndSnippet.setDividerLocation(animationPaneToSnippetDividerRatio);
                wholePane.setDividerLocation(location);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                animationAndSnippet.setDividerLocation(animationPaneToSnippetDividerRatio);
                wholePane.setDividerLocation(location);
            }
        };
        addComponentListener(adapter);
        menuPanel.addComponentListener(adapter);
        add(wholePane);
    }

    private void addButton(String label, Slide slide) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            buttonPanel.removeAll();
            threadContext.registerSlide(slide);
            repaint();
        });
        menuPanel.add(button);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }
}
