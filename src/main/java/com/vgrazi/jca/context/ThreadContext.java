package com.vgrazi.jca.context;

import com.vgrazi.jca.JCAFrame;
import com.vgrazi.jca.states.*;
import com.vgrazi.jca.util.Logging;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vgrazi.jca.util.ColorParser.parseColor;

/**
 * Maintains the list of ThreadSprites, position of monolith, color schemes,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext implements InitializingBean {
    /**
     * We either color by thread state or thread instance (eg in ForkJoin)
     */
    private ColorationScheme colorScheme = ColorationScheme.byState;

    public List<Sprite> getAllSprites() {
        return sprites;
    }

    private enum ColorationScheme {
        byState, byInstance
    }

    public void colorByThreadState() {
        this.colorScheme = ColorationScheme.byState;
    }
    public void colorByThreadInstance() {
        this.colorScheme = ColorationScheme.byInstance;
    }

    @Autowired
    Blocked blocked;
    @Autowired
    Running runnable;
    @Autowired
    Waiting waiting;
    @Autowired
    Terminated terminated;

    @Autowired
    Getting getting;

    @Value("${initial-y-position}")
    private int initialYPos;

    @Value("${initial-bottom-y-position}")
    private int initialBottomYPos;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;
    private Color blockedColor;
    private Color runnableColor;
    private Color waitingColor;
    private Color timedWaitingColor;
    private Color terminatedColor;
    private Color defaultColor;
    private Color unknownColor;

    private List<Sprite> sprites = new CopyOnWriteArrayList<>();

    private final Map<Thread, Color> threadColors = new HashMap<>();
    @Autowired
    ApplicationContext context;

    @Autowired
    ThreadCanvas canvas;

    @Autowired
    JCAFrame frame;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Value("${monolith-left-border}")
    public int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    public int pixelsPerStep;

    @Value("${arrow-length}")
    private int arrowLength;
    @Value("${frame-x}")
    private int frameX;
    @Value("${frame-y}")
    private int frameY;
    @Value("${frame-width}")
    private int frameWidth;
    @Value("${frame-height}")
    private int frameHeight;

    public ThreadContext() {
    }

    public void reset() {
        threadColors.clear();
    }
    @Value("${BLOCKED_COLOR}")
    public void setBlockedColor(String color) {
        this.blockedColor = parseColor(color);
    }

    @Value("${RUNNABLE_COLOR}")
    public void setRunnableColor(String color) {
        this.runnableColor = parseColor(color);
    }

    @Value("${WAITING_COLOR}")
    public void setWaitingColor(String color) {
        this.waitingColor = parseColor(color);
    }

    @Value("${TIMED_WAITING_COLOR}")
    public void setTimedWaitingColor(String color) {
        this.timedWaitingColor = parseColor(color);
    }

    @Value("${TERMINATED_COLOR}")
    public void setTerminatedColor(String color) {
        this.terminatedColor = parseColor(color);
    }

    @Value("${DEFAULT_COLOR}")
    public void setDefaultColor(String color) {
        this.defaultColor = parseColor(color);
    }

    @Value("${UNKNOWN_COLOR}")
    public void setUnknownColor(String color) {
        this.unknownColor = parseColor(color);
    }

    /**
     * Continually repaints the canvas
     */
    private void render() {
        Thread thread = new Thread(() -> {
            while (true) {
//                canvas.paintImmediately(canvas.getBounds());
                canvas.repaint(canvas.getBounds());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });
        thread.start();
    }

    private final Color[] colors = {Color.red, Color.CYAN, Color.BLUE, Color.DARK_GRAY,
            Color.gray, Color.GREEN, Color.YELLOW
    };

    private int colorPointer = 0;

    private Color getNextColor() {
        Color color = colors[(colorPointer++) % colors.length];
        return color;
    }

    public synchronized void addSprite(ThreadSprite sprite) {
        addSprite((Sprite)sprite);
        threadColors.put(sprite.getThread(), getNextColor());
    }
    public synchronized void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    /**
     * sets the supplied sprite to not running, and removes it from this context (allowing sufficient time
     * to animate off the screen)
     * @param threadSprite
     */
    public synchronized void stopThread(ThreadSprite threadSprite) {
        threadSprite.setRunning(false);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                sprites.remove(threadSprite);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void run() throws InterruptedException {
        executor.scheduleAtFixedRate(this::advanceSprites, 0, 100, TimeUnit.MILLISECONDS);

        while (true) {
            printAllThreads();
            Thread.sleep(100);
        }
    }

    /**
     * Returns the first running thread, or null if none
     */
    public ThreadSprite getRunningThread() {
        List<ThreadSprite> threads = getThreadsOfState(runnable);

        ThreadSprite threadSprite = null;
        if (!threads.isEmpty()) {
            threadSprite = threads.get(0);
        }
        return threadSprite;
    }

    private List<FutureSprite> getAllFutureSprites() {
        List<FutureSprite> collect = sprites.stream()
                .filter(sprite->sprite instanceof FutureSprite)
                .map(sprite -> (FutureSprite)sprite)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * If there is exactly one running thread, returns it.
     * Otherwise throws an IllegalArgumentException
     * @return
     */
    public List<ThreadSprite> getRunningThreads() {
        List<ThreadSprite> threads = getThreadsOfState(runnable);
        return threads;
    }


    /**
     * Returns a list of all threads that are not of the specified state
     */
    public List<ThreadSprite> getThreadsNotOfState(ThreadState threadState) {
        List<ThreadSprite> collect = sprites.stream()
                .filter(sprite->sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite)sprite)
                .filter(sprite -> sprite.getState() != threadState).collect(Collectors.toList());
        return collect;
    }

    public void printAllThreads() {
        sprites.forEach(Logging::log);
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    private List<ThreadSprite> getThreadsOfState(ThreadState threadState) {
        List<ThreadSprite> collect = sprites.stream()
                .filter(sprite->sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite)sprite)
                .filter(sprite -> sprite.getState() == threadState)
                .collect(Collectors.toList());
        return collect;
    }


    /**
     * Advance the position of each sprite, based on its current position and state
     */
    private void advanceSprites() {
        sprites.forEach(Sprite::setNextXPosition);
    }

    public int getNextYPosition(int height) {
        int initialYPos = this.initialYPos;
        this.initialYPos += height;
        return initialYPos;
    }

    public int getYPosition() {
        return initialYPos;
    }

    public int getNextBottomYPosition(int height) {
        int initialBottomYPos = this.initialBottomYPos;
        this.initialBottomYPos += height;
        return initialBottomYPos;
    }

    public List<ThreadSprite> getAllThreads() {
        return sprites.stream()
                .filter(sprite->sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite)sprite)
                .collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(frameX, frameY, frameWidth, frameHeight);
        render();
    }

    public void addButton(String text, Runnable runnable) {
        JButton button = new JButton(text);
        button.addActionListener(e -> SwingUtilities.invokeLater(runnable));
        frame.getButtonPanel().add(button);
        frame.getButtonPanel().revalidate();
        frame.revalidate();
        frame.addNotify();
    }

    public void setVisible() {
        frame.setVisible(true);
    }

    public Color getColor(ThreadSprite threadSprite) {
        Color color;

        if (isColorByThreadState()) {
            color = getColorByThreadState(threadSprite);
        } else if (isColorByThreadInstance()) {
            color = threadColors.get(threadSprite.getThread());
            if (color == null) {
                color = unknownColor;
            }
        } else {
            throw new IllegalArgumentException("Must set coloration scheme - by state or by instance");
        }
        return color;
    }

    private boolean isColorByThreadState() {
        return colorScheme == ColorationScheme.byState;
    }

    private boolean isColorByThreadInstance() {
        return colorScheme == ColorationScheme.byInstance;
    }

    /**
     * We support coloring by different schemes. This scheme colors threads by their state, blue for blocked, green
     * for Runnable, etc.
     *
     * @param threadSprite
     */
    private Color getColorByThreadState(ThreadSprite threadSprite) {
        Color color;
        Thread.State state = threadSprite.getThreadState();
        if (state == Thread.State.BLOCKED) {
            color = blockedColor;
        } else if (state == Thread.State.RUNNABLE) {
            color = runnableColor;
        } else if (state == Thread.State.WAITING) {
            color = waitingColor;
        } else if (state == Thread.State.TIMED_WAITING) {
            color = timedWaitingColor;
        } else if (state == Thread.State.TERMINATED) {
            // we should display it as runnable (green) until it flies
            // past the monolith
            if (threadSprite.getRelativePosition() == RelativePosition.After)
                color = terminatedColor;
            else {
                color = runnableColor;
            }
        } else {
            color = defaultColor;
        }
        return color;
    }

}
