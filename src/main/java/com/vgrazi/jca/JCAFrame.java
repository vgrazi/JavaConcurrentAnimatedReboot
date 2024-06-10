package com.vgrazi.jca;


import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.slides.*;
import com.vgrazi.jca.util.Parsers;
import com.vgrazi.jca.util.UIUtils;
import com.vgrazi.jca.view.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

@Component
public class JCAFrame extends JFrame {

    /*

    .....................................................
    .                 .     Buttons                     .
    .                 ...................................
    .   Menu          .   Messages                      .
    .                 ...................................
    .                 .   CardPanel                     .
    .                 . ..............................  .
    .                 . . Animation    .  Snippet    .  .
    .                 . .              .             .  .
    .                 . ..............................  .
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
    private CountDownLatchSlide countdownLatchSlide;

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
    private CompletionServiceSlide completionServiceSlide;

    @Autowired
    private StampedLockSlide stampedLockSlide;

    @Autowired
    private BasicSlide basicSlide;

    @Autowired
    private BlockingQueueSlide blockingQueueSlide;

    @Autowired
    private SaturationPolicySlide saturationPolicySlide;

    @Autowired
    private JPanel cardPanel;

    @Autowired
    private IntroSlide introSlide;

    @Value("${menu-button-vgap}")
    private int vgap;

    private Color buttonPanelColor;

    @Value("${animation-pane-to-snippet-divider-ratio}")
    private double animationPaneToSnippetDividerRatio;

    @Autowired
    private UIUtils uiUtils;

    private JScrollPane snippetScrollPane;


    private final JPanel buttonPanel = new JPanel(new ButtonPanelLayout(2, 2));
    @Autowired
    private JLabel messages;
    private final JPanel menuPanel = new JPanel();

    @Autowired
    private SnippetCanvas snippetCanvas;

    @Autowired
    private JLabel imageLabel;

    public JCAFrame() throws HeadlessException {
        super("Java Concurrent Animated - Reboot! https://github.com/vgrazi/JavaConcurrentAnimatedReboot");
    }

    @Value("${BUTTON-PANEL-COLOR}")
    public void setButtonPanelColor(String color) {
        buttonPanelColor = Parsers.parseColor(color);
    }

    @PostConstruct
    public void afterPropertiesSet() throws IOException {
        snippetCanvas.setBackground(Color.white);
        snippetCanvas.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        snippetCanvas.setFontSize(18);
        snippetScrollPane = new JScrollPane(snippetCanvas);
        snippetScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        snippetScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        snippetScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        JSplitPane animationAndSnippet = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, threadCanvas, snippetScrollPane);
        animationAndSnippet.setDividerSize(2);
        animationAndSnippet.setDividerLocation(.67);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messages);
        messages.setText("this is the message label");
        JPanel buttonsAndMessages = new JPanel(new GridLayout(2, 1));
        buttonPanel.setBackground(buttonPanelColor);
        messagePanel.setBackground(buttonPanelColor);
        buttonsAndMessages.add(buttonPanel);
        buttonsAndMessages.add(messagePanel);
        buttonsAndMessages.setBackground(buttonPanelColor);
        cardPanel.add(animationAndSnippet, "animation-pane");
        JPanel graphicsPanel = new JPanel(new BorderLayout());
        graphicsPanel.add(imageLabel);
        cardPanel.add(graphicsPanel, "graphics-pane");
        JSplitPane rightSide = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonsAndMessages, cardPanel);
        rightSide.setDividerSize(2);

        menuPanel.setLayout(new ButtonLayout(vgap));

        addButton("Synchronized", synchronizedSlide);
        addButton("ReentrantLock", reentrantLockSlide);
        addButton("Semaphore", semaphoreSlide);
        addButton("ReadWriteLock", readWriteLockSlide);
        addButton("StampedLock", stampedLockSlide);
        addButton("Executors", executorsSlide);
        addButton("Saturation Policy", saturationPolicySlide);
        addButton("CyclicBarrier", cyclicBarrierSlide);
        addButton("CountDownLatch", countdownLatchSlide);
        addButton("Phaser", phaserSlide);
        addButton("BlockingQueue", blockingQueueSlide);
        addButton("TransferQueue", transferQueueSlide);
        addButton("CompletableFuture", completableFutureSlide);
        addButton("CompletionService", completionServiceSlide);
        addButton("AtomicInteger", atomicIntegerSlide);
        addButton("Credits", basicSlide);
        addButton("Titles", introSlide);

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
                Slide slide = threadContext.getSlide();
                if(slide instanceof IntroSlide) {
                    ((IntroSlide) slide).resetImage();
                }
            }
        };

        addComponentListener(adapter);
        menuPanel.addComponentListener(adapter);

        /*
         * set the red laser pointer in the thread canvas
         */
        SwingUtilities.invokeLater(() -> {
            ImageIcon icon = uiUtils.getImageIcon("images/cursor.png");
            Image image = icon.getImage();
            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(1, 1), "customCursor");
            threadCanvas.setCursor(cursor);
        });

        add(wholePane);
    }

    private void addButton(String label, Slide slide) {
        JButton button = new JButton(label);
        button.addActionListener(e -> {
            buttonPanel.removeAll();
            threadContext.registerSlide(slide);
            slide.setSnippetFontSize(controlPanel.getFontSize());
            repaint();
        });
        menuPanel.add(button);
    }

    public JPanel getButtonPanel() {
        return buttonPanel;
    }
}
