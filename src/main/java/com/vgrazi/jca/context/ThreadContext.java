package com.vgrazi.jca.context;

import com.vgrazi.jca.JCAFrame;
import com.vgrazi.jca.sprites.*;
import com.vgrazi.jca.states.*;
import com.vgrazi.jca.util.Logging;
import com.vgrazi.jca.view.SnippetCanvas;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vgrazi.jca.util.Parsers.parseColor;

/**
 * Maintains the list of ThreadSprites, position of monolith, color schemes,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext<S> implements InitializingBean {
    /**
     * We either color by thread state or thread instance (eg in ForkJoin)
     */
    private ColorationScheme colorScheme = ColorationScheme.byState;

    @Autowired
    private ResourceLoader resourceLoader;

    private String snippet;

    private final Pattern REPLACE_WHITE = Pattern.compile("^(\\s*)(.*)", Pattern.MULTILINE);
    private final Pattern CLASS_LOCATOR = Pattern.compile("class=[\"|'](.+?)[\"|']", Pattern.MULTILINE);
    private String snippetFile;

    public void addStyleRule(String rule) {
        snippetCanvas.addStyleRule(rule);
    }

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
    public PooledThreadSprite<String> getRunningPooledThread() {
        return (PooledThreadSprite) sprites.stream().filter(sprite -> sprite instanceof PooledThreadSprite)
                .filter(sprite -> sprite.isRunning())
                .findFirst()
                .orElse(null);
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
        getAllThreads().forEach(sprite -> sprite.getThread().stop());
        canvas.setSlideLabel("");
        sprites.clear();
        snippetFile = null;
        snippetCanvas.removeContent();
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

    /**
     * Continually repaints the canvas
     */
    private void render() {
        Thread thread = new Thread(() -> {
            while (true) {
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

    public synchronized void addSprite(Sprite sprite) {
        sprites.add(sprite);
        if (sprite instanceof ThreadSprite) {
            threadColors.put(((ThreadSprite<S>) sprite).getThread(), getNextColor());
        }
    }

    public void addSprite(int i, Sprite sprite) {
        if (i >= 0 && i <= sprites.size()) {
            sprites.add(i, sprite);
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
    public ThreadSprite<S> getFirstRunningThreadOfSpecialId(int specialId) {
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

    public int getNextYPosition() {
        return getNextYPosition(pixelsPerYStep);
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
        if (sprites.isEmpty()) {
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

    /**
     * Reads the snippet from the specified filename, and adds a <br> to the end of every line, and replaces leading whitespace with &nbsp;
     * Returns a Set of the CSS class style selectors from the file
     * @param fileName
     * @throws IOException if IO Exception or bad HTML parsing
     */
    public Set<String> setSnippetFile(String fileName) throws IllegalArgumentException {
        try {
            Resource resource = resourceLoader.getResource(fileName);
            byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
            String snippetFileContent = new String(bytes);
            // replace leading white space with &nbsp;
            prepareSnippet(fileName, snippetFileContent);
            Set<String> styles = extractStyleSelectors(snippetFileContent);
            return styles;
        } catch (IOException | BadLocationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Finds all instances of class="xxx" or class='xxx'
     */
    private Set<String> extractStyleSelectors(String snippetFileContent) {
        Set<String> styles = new HashSet<>();
        Matcher matcher = CLASS_LOCATOR.matcher(snippetFileContent);
        while (matcher.find()) {
            styles.add(matcher.group(1));
        }
        return styles;
    }

    /**
     * loads the snippet file, replaces spaces with &nbsp; and ends all lines with <br/>\n
     */
    private void prepareSnippet(String fileName, String snippetFileContent) throws IOException, BadLocationException {
        Matcher matcher = REPLACE_WHITE.matcher(snippetFileContent);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String blanks = matcher.group(1);
            String spaces = blanks.replaceAll(" ", "&nbsp;");
            builder.append(spaces).append(matcher.group(2)).append("<br/>\n");
            // would prefer to use <pre>$1</pre> but apparently this is not supported :(
//                snippetFileContent = matcher.replaceAll(spaces + "$1");
        }
        String snippet = builder.toString();
        this.snippet = snippet;
        System.out.println(snippet);
        snippetCanvas.setSnippet(snippet);
        this.snippetFile = fileName;
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

