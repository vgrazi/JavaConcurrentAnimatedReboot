package com.vgrazi.jca.context;

import com.vgrazi.jca.JCAFrame;
import com.vgrazi.jca.engine.AnimationEngine;
import com.vgrazi.jca.slides.Slide;
import com.vgrazi.jca.sprites.*;
import com.vgrazi.jca.states.*;
import com.vgrazi.jca.util.Logging;
import com.vgrazi.jca.view.SnippetCanvas;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vgrazi.jca.util.Parsers.parseColor;

/**
 * Maintains the list of ThreadSprites, position of monolith, color schemes,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext<S> implements InitializingBean {
    @Value("${pixels-per-step-runner}")
    public int initialPixelsPerStepRunner;
    public int pixelsPerStepRunner;

    @Value("${snippet-font-size}")
    private int initialFontSize;
    private int fontSize;

    @Autowired
    private JPanel cardPanel;

    /**
     * Used for positioning getter sprites
     */
    private static final int GETTER_DELTA = 30;
    private int initialGetterYPos = 90 - GETTER_DELTA;

    /**
     * We either color by thread state or thread instance (eg in ForkJoin)
     */
    private ColorationScheme colorScheme = ColorationScheme.byState;

    private Slide slide;
    private boolean displayThreadNames;

    /**
     * Returns the thread sprite bound to the supplied thread
     */
    public ThreadSprite getThreadSprite(Thread thread) {
        return (ThreadSprite) sprites.stream().filter(sprite -> sprite instanceof ThreadSprite)
                .filter(sprite -> ((ThreadSprite) sprite).getThread() == thread)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the first pooled thread that is actually running a task
     */
    public PooledThreadSprite getRunningPooledThread() {
        return (PooledThreadSprite) sprites.stream().filter(sprite -> sprite instanceof PooledThreadSprite)
                .filter(Sprite::isRunning)
                .findFirst()
                .orElse(null);
    }

    public RunnerThreadSprite getRunnableThread() {
        return (RunnerThreadSprite) sprites.stream().filter(sprite -> sprite instanceof RunnerThreadSprite)
                .filter(sprite1 -> ((RunnerThreadSprite) sprite1).getState() == runnable)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a list of all thread sprites that have conditions
     */
    public List<ThreadSprite> getAllConditionSprites() {
        return sprites.stream()
                .filter(sprite->sprite instanceof ThreadSprite)
                .map(sprite->(ThreadSprite)sprite)
                .filter(ThreadSprite::hasCondition)
                .collect(Collectors.toList());
    }

    /**
     * Diagnostic - attach to a button to list sprites on demand
     */
    public void listSprites() {
        System.out.println("SPRITES");
        sprites.forEach(System.out::println);
    }

    public boolean isDisplayThreadNames() {
        return displayThreadNames;
    }

    public void setDisplayThreadNames(boolean b) {
        this.displayThreadNames = b;
    }

    /**
     * Toggles the Graphics panel and the animation/snippet
     */
    public void toggleGraphics() {
        ((CardLayout) cardPanel.getLayout()).next(cardPanel);
    }

    public Slide getSlide() {
        return slide;
    }

    /**
     * If there are no getters, positions this at the top
     * otherwise positions it GETTER_DELTA positions below the lowest
     */
    public void setGetterNextYPos(ThreadSprite getter) {
        List<GetterThreadSprite> getters = getAllGetterThreadSprites();
        int next = getters.stream().filter(g-> g!= getter).mapToInt(Sprite::getYPosition).max().orElse(initialGetterYPos);
        getter.setYPosition(next + GETTER_DELTA);
    }

    private enum ColorationScheme {
        byState, byInstance;
    }

    @Autowired
    public Blocked blocked;

    @Autowired
    public Running runnable;
    @Autowired
    public Waiting waiting;
    @Autowired
    public Terminated terminated;
    @Autowired
    public Getting getting;
    @Autowired
    public Retreating retreating;

    @Autowired
    public Pooled pooled;

    @Autowired
    private AnimationEngine engine;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    /**
     * This is the starting y position. It is effectively final
     */
    @Value("${initial-y-position}")
    private int initialYPos;

    /**
     * This is the actual current y position
     */
    private int nextYPos;

    @Value("${initial-bottom-y-position}")
    private int initialPooledYPos;

    private int nextPooledYPos;

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
    private ThreadCanvas canvas;

    @Autowired
    private SnippetCanvas snippetCanvas;

    @Autowired
    private JCAFrame frame;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Value("${monolith-left-border}")
    public int monolithLeftBorder;

    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    public int initialPixelsPerStep;
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

    /**
     * Reset the previous slide, if any, and register and run the supplied one
     */
    public void registerSlide(Slide slide) {
        if(this.slide != null) {
            this.slide.reset();
        }
        this.slide = slide;
        this.slide.run();
    }

    public void reset() {
        threadColors.clear();
        nextYPos = initialYPos;
        nextPooledYPos = initialPooledYPos;
        getAllThreads().stream().filter(sprite->sprite.getThread() != null).forEach(sprite -> sprite.getThread().stop());
        canvas.setSlideLabel("");
        canvas.setBottomLabel(null);
        clearSprites();
        pixelsPerStep = initialPixelsPerStep;
        pixelsPerStepRunner = initialPixelsPerStepRunner;
        displayThreadNames = false;
    }

    public void clearSprites() {
        sprites.clear();
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

    public void setSlideLabel(String label) {
        canvas.setSlideLabel(label);
    }

    public void setBottomLabel(String label) {
        canvas.setBottomLabel(label);
    }

    private volatile int speed = 10;

    public void setSpeed(String speed) {
        switch(speed) {
            case "slow":
                pixelsPerStep = 10;
                pixelsPerStepRunner = 5;
                break;
            case "very-slow":
                pixelsPerStep = 5;
                pixelsPerStepRunner = 2;
                break;
            case "pause":
                pixelsPerStep = 0;
                pixelsPerStepRunner = 0;
                break;
            case "normal":
            default:
                pixelsPerStep = 20;
                pixelsPerStepRunner = 10;
                break;


        }
    }

    /**
     * Continually repaints the canvas
     */
    private void render() {
        engine.render(canvas);
    }

    private final Color[] colors = {Color.red, Color.CYAN, Color.BLUE, Color.DARK_GRAY,
            Color.gray, Color.GREEN, Color.YELLOW
    };

    private int colorPointer = 0;

    private Color getNextColor() {
        Color color = colors[(colorPointer++) % colors.length];
        return color;
    }

    public synchronized void addSprite(Sprite sprite) {
        sprites.add(sprite);
        if (sprite instanceof ThreadSprite) {
            threadColors.put(((ThreadSprite<S>) sprite).getThread(), getNextColor());
        }
    }

    public void addSprite(int position, Sprite sprite) {
        if (position >= 0 && position <= sprites.size()) {
            sprites.add(position, sprite);
        }
        if (sprite instanceof ThreadSprite) {
            threadColors.put(((ThreadSprite<S>) sprite).getThread(), getNextColor());
        }
    }

    public List<Sprite> getAllSprites() {
        return sprites;
    }


    /**
     * sets the supplied sprite to not running, and removes it from this context (allowing sufficient time
     * to animate off the screen)
     *
     * @param threadSprite
     */
    public synchronized void stopThread(ThreadSprite<S> threadSprite) {
        threadSprite.setRunning(false);
        new Thread(() -> {
            try {
                // while the sprite is visible, render it. Otherwise remove it
                // remember, retreating sprites move from right to left
                // todo: the measurement to the right border of the frame
                while (threadSprite.getXPosition() >= 0 && threadSprite.getXPosition() < 600) {
                    Thread.sleep(100);
                }
                sprites.remove(threadSprite);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void startAnimationThread() throws InterruptedException {
        executor.scheduleAtFixedRate(this::advanceSprites, 0, 100, TimeUnit.MILLISECONDS);

        while (true) {
            printAllThreads();
            Thread.sleep(100);
        }
    }

    /**
     * Returns the first running thread, or null if none
     */
    public ThreadSprite<S> getRunningThread() {
        ThreadSprite<S> threadSprite = getThreadOfState(runnable);

        return threadSprite;
    }

    public List<ObjectSprite> getAllObjectSprites() {
        List<ObjectSprite> collect = sprites.stream()
                .filter(sprite -> sprite instanceof FutureSprite)
                .map(sprite -> (ObjectSprite) sprite)
                .collect(Collectors.toList());
        return collect;
    }

    public List<ObjectSprite> getAllWaitingObjectSprites() {
        List<ObjectSprite> collect = sprites.stream()
                .filter(sprite -> sprite instanceof ObjectSprite)
                .map(sprite -> (ObjectSprite) sprite)
                .filter(sprite -> sprite.getState() == waiting)
                .collect(Collectors.toList());
        return collect;
    }

    public List<ThreadSprite> getAllWaitingThreads() {
        List<ThreadSprite> collect = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite) sprite)
                .filter(sprite -> sprite.getState() == waiting)
                .collect(Collectors.toList());
        return collect;
    }

    public ThreadSprite getFirstWaitingThread() {
        ThreadSprite threadSprites = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite) sprite)
                .filter(sprite -> sprite.getState() == waiting)
                .findFirst().orElse(null);
        return threadSprites;
    }

    /**
     * Returns the first object sprite that is in the waiting state, or null
     */
    public ObjectSprite getFirstWaitingObjectSprite() {
        ObjectSprite objectSprite = sprites.stream()
                .filter(sprite -> sprite instanceof ObjectSprite)
                .map(sprite -> (ObjectSprite) sprite)
                .filter(sprite -> sprite.getState() == waiting)
                .findFirst().orElse(null);
        return objectSprite;
    }
    /**
     * Returns the first object sprite that is in the waiting state, or null
     */
    public ObjectSprite getFirstRunningObjectSprite() {
        ObjectSprite objectSprite = sprites.stream()
                .filter(sprite -> sprite instanceof ObjectSprite)
                .map(sprite -> (ObjectSprite) sprite)
                .filter(sprite -> sprite.getState() == runnable)
                .findFirst().orElse(null);
        return objectSprite;
    }

    /**
     * Returns the first thread sprite that is in the waiting state, or null
     */
    public GetterThreadSprite getFirstGetterThreadSprite() {
        GetterThreadSprite getterThreadSprite = sprites.stream()
                .filter(sprite -> sprite instanceof GetterThreadSprite)
                .map(sprite -> (GetterThreadSprite) sprite)
                .filter(sprite -> sprite.getState() == getting)
                .findFirst().orElse(null);
        return getterThreadSprite;
    }

    /**
     * Returns all getther thread sprites
     */
    public List<GetterThreadSprite> getAllGetterThreadSprites() {
        List<GetterThreadSprite> getterThreadSprites = sprites.stream()
                .filter(sprite -> sprite instanceof GetterThreadSprite)
                .map(sprite -> (GetterThreadSprite) sprite)
                .collect(Collectors.toList());
        return getterThreadSprites;
    }

    private List<FutureSprite> getAllFutureSprites() {
        List<FutureSprite> collect = sprites.stream()
                .filter(sprite -> sprite instanceof FutureSprite)
                .map(sprite -> (FutureSprite) sprite)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * If there is exactly one running thread, returns it.
     * Otherwise throws an IllegalArgumentException
     *
     * @return
     */
    public List<ThreadSprite<S>> getRunningThreads() {
        List<ThreadSprite<S>> threads = getThreadsOfState(runnable);
        return threads;
    }

    /**
     * If done is true, returns all FutureRunnableSprites that are done. Else returns all of them that are not done
     * @param done
     * @return
     */
    public List<FutureRunnableSprite> getFutureRunnables(boolean done) {
        List<FutureRunnableSprite> list = sprites.stream()
                .filter(sprite-> sprite instanceof FutureRunnableSprite)
                .map(sprite->(FutureRunnableSprite)sprite)
                .filter(sprite-> sprite.isDone() == done)
                .collect(Collectors.toList());
        return list;
    }

    public List<Sprite> getThreadSpritesWithAction(String action) {
        List<Sprite> list = sprites.stream()
                .filter(sprite-> Objects.equals(sprite.getAction(), action))
                .collect(Collectors.toList());
        return list;
    }

    /**
     * Returns a list of all threads that are not of the specified state
     */
    public List<ThreadSprite<S>> getThreadsNotOfState(ThreadState threadState) {
        List<ThreadSprite<S>> collect = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .filter(sprite -> sprite.getState() != threadState).collect(Collectors.toList());
        return collect;
    }

    public void printAllThreads() {
        sprites.forEach(Logging::log);
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    private List<ThreadSprite<S>> getThreadsOfState(ThreadState threadState) {
        List<ThreadSprite<S>> collect = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .filter(sprite -> sprite.getState() == threadState)
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    private ThreadSprite<S> getThreadOfState(ThreadState threadState) {
        ThreadSprite<S> first = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .filter(sprite -> sprite.getState() == threadState)
                .findFirst().orElse(null);
        return first;
    }

    /**
     * Returns the first waiting thread of specified special id, or null if none
     */
    public ThreadSprite<S> getFirstWaitingThreadOfSpecialId(int specialId) {
        ThreadSprite<S> first = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .filter(sprite -> sprite.getSpecialId() == specialId)
                .filter(sprite -> sprite.getState() == waiting)
                .findFirst().orElse(null);
        return first;
    }

    /**
     * Returns the first running thread of specified special id, or null if none
     */
    public ThreadSprite<S> getFirstRunningThreadOfSpecialId(long specialId) {
        ThreadSprite<S> first = sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .filter(sprite -> sprite.getSpecialId() == specialId)
                .filter(sprite -> sprite.getState() == runnable)
                .findFirst().orElse(null);
        return first;
    }

    /**
     * Advance the position of each sprite, based on its current position and state
     */
    private void advanceSprites() {
        sprites.forEach(Sprite::setNextXPosition);
    }

    /**
     * Add a few pixels to the next y position
     * @param pixels
     */
    public void addYPixels(int pixels) {
        nextYPos += pixels;
    }

    public int getNextYPosition(int height) {
        if (sprites.isEmpty()) {
            nextYPos = initialYPos;
        }
        int nextYPos = this.nextYPos;
        this.nextYPos += height;
        return nextYPos;
    }

    /**
     * Get the next pooled thread position
     */
    public int getNextPooledYPosition() {
        return getNextPooledYPosition(pixelsPerYStep);
    }
    /**
     * Get the next pooled thread position
     * @param height
     * @return
     */
    public int getNextPooledYPosition(int height) {
        if(nextPooledYPos == 0)
        {
            nextPooledYPos = initialPooledYPos;
        }
        int nextPooledYPos = this.nextPooledYPos;
        this.nextPooledYPos += height;
        return nextPooledYPos;
    }

    public int getYPosition() {
        return initialYPos;
    }

    public List<ThreadSprite<S>> getAllThreads() {
        return sprites.stream()
                .filter(sprite -> sprite instanceof ThreadSprite)
                .map(sprite -> (ThreadSprite<S>) sprite)
                .collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(frameWidth, frameHeight);
        // center the frame
        frame.setLocationRelativeTo(null);
        nextYPos = initialYPos;
        fontSize = initialFontSize;
        render();
    }

    public JButton addButton(String text, Runnable runnable) {
        JButton button = new JButton(text);
        button.addActionListener(e -> SwingUtilities.invokeLater(runnable));
        frame.getButtonPanel().add(button);
        frame.getButtonPanel().revalidate();
        frame.revalidate();
        frame.addNotify();
        return button;
    }

    public void setVisible() {
        frame.setVisible(true);
    }

    public Color getColor(ThreadSprite<S> threadSprite) {
        Color color;

        if (isColorByThreadState()) {
            color = getColorByThreadState(threadSprite);
        } else if (isColorByThreadInstance()) {
            color = getColorByInstance(threadSprite);
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
    public Color getColorByThreadState(ThreadSprite<S> threadSprite) {
        Color color;
        Thread.State state = threadSprite.getThreadState();
        if (threadSprite.isRetreating()) {
            return terminatedColor;
        } else if (state == Thread.State.BLOCKED) {
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

    public Color getColorByInstance(ThreadSprite<S> threadSprite) {
        Color color = threadColors.get(threadSprite.getThread());
        if (color == null) {
            color = unknownColor;
        }
        return color;
    }

}

