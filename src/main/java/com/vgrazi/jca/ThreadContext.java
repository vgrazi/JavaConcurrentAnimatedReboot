package com.vgrazi.jca;

import com.vgrazi.jca.states.*;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.vgrazi.jca.util.Logging.log;

/**
 * Maintains the list of ThreadSprites, position of monolith,
 * responsible for creating new threadSprites, and provides accessors
 * for all of the threads of a specific state (for example, getRunningThreads)
 */
@Component
//@Scope("prototype")
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

    @Autowired
    JFrame frame;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Value("${monolith-left-border}")
    public int monolithLeftBorder;
    @Value("${monolith-right-border}")
    private int monolithRightBorder;
    @Value("${pixels-per-step}")
    public int pixelsPerStep;

    @Value("${arrow-length}")
    private int arrowLength;

    public ThreadContext() {
    }

    private void render() {
        Thread thread = new Thread(() -> {
            while (true) {
                if (frame!=null) {
                    frame.revalidate();
                    frame.repaint();
                    frame.addNotify();
                }
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

    void run() throws InterruptedException {
        executor.scheduleAtFixedRate(this::advanceSprites, 0, 100, TimeUnit.MILLISECONDS);

        while(true) {
            printAllThreads();
//            threads.forEach(thread -> Logging.logAndSleep(0,"", thread));
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

    @Override
    public void afterPropertiesSet() {

        JPanel panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                setOpaque(true);
                Graphics2D graphics = (Graphics2D) g;
                super.paintComponent(graphics);
                graphics.setColor(Color.black);
                graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                graphics.setColor(Color.white);
                threads.forEach(sprite -> render(sprite, graphics));
                graphics.dispose();
            }

            private void render(ThreadSprite sprite, Graphics graphics) {
                graphics.drawLine(sprite.getXPosition(), sprite.getYPosition(),
                        sprite.getXPosition() + arrowLength, sprite.getYPosition());

            }
        };
        frame.getContentPane().add(panel);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);
        render();
    }
}

