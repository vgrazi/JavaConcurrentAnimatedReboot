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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Maintains the list of ThreadSprites, position of monolith,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
public class ThreadContext implements InitializingBean {
    @Autowired
    Blocked blocked;
    @Autowired
    Running runnable;
    @Autowired
    Waiting waiting;
    @Autowired
    Terminated terminated;

    @Value("${initial-y-position}")
    private int initialYPos;

    @Value("${initial-bottom-y-position}")
    private int initialBottomYPos;

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    private List<ThreadSprite> threads = new CopyOnWriteArrayList<>();
    @Autowired
    ApplicationContext context;

    @Autowired ThreadCanvas canvas;

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

    private void render() {
        Thread thread = new Thread(() -> {
            while (true) {
                canvas.repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }


    public synchronized void addThread(ThreadSprite thread) {
        threads.add(thread);
    }

    public synchronized void stopThread(ThreadSprite threadSprite) {
        threadSprite.setRunning(false);
        new Thread(()->{
            try {
                Thread.sleep(5000);
                threads.remove(threadSprite);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void run() throws InterruptedException {
        executor.scheduleAtFixedRate(this::advanceSprites, 0, 100, TimeUnit.MILLISECONDS);

        while(true) {
            printAllThreads();
            Thread.sleep(100);
        }
    }

    /**
     * If there is exactly one running thread, returns it.
     * Otherwise throws an IllegalArgumentException
     */
    public ThreadSprite getRunningThread() {
        List<ThreadSprite> threads = getThreadsOfState(runnable);
        if(threads.size() != 1) {
            throw new IllegalArgumentException("Expected one running thread but found " + threads.size());
        }

        return threads.get(0);
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
    public List<ThreadSprite> getThreadsNotOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() != state).collect(Collectors.toList());
        return collect;
    }

    public void printAllThreads() {
        threads.forEach(Logging::log);
    }

    /**
     * Returns a list of all threads in the supplied state
     */
    private List<ThreadSprite> getThreadsOfState(State state) {
        List<ThreadSprite> collect = threads.stream().filter(sprite -> sprite.getState() == state).collect(Collectors.toList());
        return collect;
    }


    /**
     * Advance the position of each sprite, based on its current position and state
     */
    private void advanceSprites() {
        threads.forEach(ThreadSprite::setNextPosition);
    }

    public int getNextYPosition() {
        int initialYPos = this.initialYPos;
        this.initialYPos += pixelsPerYStep;
        return initialYPos;
    }

    public int getNextBottomYPosition() {
        int initialBottomYPos = this.initialBottomYPos;
        this.initialBottomYPos += pixelsPerYStep;
        return initialBottomYPos;
    }

    public List<ThreadSprite> getAllThreads() {
        return threads;
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
}

