package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.vgrazi.jca.util.Logging.log;

/**
 * add button - new condition - applies to running thread if any, and only if no condition already assigned
 * add button - await condition - Prompt for which condition. If no condition already set, message and ignore
 * add button - signal condition - Prompt for which condition. If no condition already set, message and ignore
 * add button - signalAll condition - Prompt for which condition. If no condition already set, message and ignore
 */
@Component
public class ReentrantLockSlide extends Slide {

    private final ApplicationContext applicationContext;

    public ReentrantLockSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private ReentrantLock lock = new ReentrantLock();

    private int conditionId;
    @Autowired
    @Qualifier("dottedStroke")
    private Stroke dottedStroke;

    @Autowired
    private ThreadCanvas threadCanvas;

    @Value("${monolith-left-border}")
    private int monolithLeftBorder;

    public void run() {
        reset();

        threadContext.addButton("lock()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.attachAndStartRunnable(() -> {
                lock.lock();
                try {
                    whileLock(sprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                lock.unlock();
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });
        threadContext.addButton("lockInterrubtibly()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            sprite.setSpecialId(1);
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.setStroke(dottedStroke);
            sprite.attachAndStartRunnable(() -> {
                try {
                    lock.lockInterruptibly();
                    whileLock(sprite);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    sprite.setMessage(e.toString());
                }
                lock.unlock();
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });

        threadContext.addButton("tryLock()", () -> {
            ThreadSprite<Boolean> sprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            // set the holder to true for running
            sprite.setHolder(true);
            sprite.attachAndStartRunnable(() -> {
                boolean acquired = lock.tryLock();
                if (acquired) {
                    try {
                        whileLock(sprite);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    lock.unlock();
                } else {
                    sprite.setRetreating(true);
                }
            });
            threadContext.addSprite(sprite);

//            setCssSelected("synchronized");
        });

        threadContext.addButton("interrupt()", () -> {
//            ThreadSprite sprite = threadContext.getFirstWaitingInterruptibleThread();
            ThreadSprite sprite = threadContext.getFirstWaitingThreadOfSpecialId(1);
            sprite.setRetreating(true);
            sprite.getThread().interrupt();
//            setCssSelected("synchronized");
        });


//        // one of the threads (call it thread1, probably same as sprite1) is now runnable and the other (thread2) is blocked
//
        threadContext.addButton("newCondition()", () -> {
            ThreadSprite runningSprite = threadContext.getRunningThread();
            if (!runningSprite.hasCondition()) {
                runningSprite.setAction("newCondition");
            }
        });

        addAwaitSignalButton("await()", "await");

        addAwaitSignalButton("signal()", "signal");

        addAwaitSignalButton("signalAll()", "signalAll");

        threadContext.addButton("unlock()", () -> {
            ThreadSprite<Boolean> runningSprite = threadContext.getRunningThread();
            runningSprite.setHolder(false);
            // The new running thread should call notify
            runningSprite.setAction("release");
            log("Set release on ", runningSprite);
            threadContext.stopThread(runningSprite);
//                setCssSelected("release");
        });

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();

    }

    /**
     * Creates the while loop called by all of the locking methods - lock, tryLock, lockInterruptibly
     */
    private void whileLock(ThreadSprite<Boolean> sprite) throws InterruptedException {
        while (sprite.getHolder()) {
            switch (sprite.getAction()) {
                case "newCondition": {
                    // newCondition was requested. Create and continue
                    Condition condition = lock.newCondition();
                    sprite.setCondition(condition, ++conditionId);
                    sprite.setAction("running");
                }
                break;
                case "await": {
                    Condition condition = sprite.getCondition();
                    condition.await();
                    sprite.setAction("running");
                }
                break;
                case "signal": {
                    Condition condition = sprite.getCondition();
                    condition.signal();
                    sprite.setAction("running");
                }
                break;
                case "signalAll": {
                    Condition condition = sprite.getCondition();
                    condition.signalAll();
                    sprite.setAction("running");
                }
                break;
            }
            Thread.yield();
        }
    }

    /**
     * Adds a button for signal, signalAll, or await
     *
     * @param label  the button label
     * @param action the action to set on the sprite if the menu item is selected
     */
    private void addAwaitSignalButton(String label, String action) {
        threadContext.addButton(label, () -> {
                    ThreadSprite runningSprite = threadContext.getRunnableThread();
                    if (runningSprite != null) {
                        if (!runningSprite.hasCondition()) {
                            // the user much choose which condition to wait on
                            List<ThreadSprite> conditionThreads = threadContext.getAllConditionSprites();
                            if (!conditionThreads.isEmpty()) {
                                JPopupMenu menu = new JPopupMenu();
                                JMenuItem labelItem = new JMenuItem("Select condition:");
                                labelItem.setEnabled(false);
                                menu.add(labelItem);
                                menu.add(new JSeparator());
                                Set<Integer> conditionIds = new HashSet<>();

                                for (ThreadSprite sprite : conditionThreads) {
                                    JMenuItem conditionItem = new JMenuItem(String.format("Condition C%d", sprite.getConditionId()));
                                    if (!conditionIds.contains(sprite.getConditionId())) {
                                        conditionIds.add(sprite.getConditionId());
                                        menu.add(conditionItem);
                                        conditionItem.addActionListener(event -> {
                                            Condition condition = sprite.getCondition();
                                            runningSprite.setCondition(condition, sprite.getConditionId());
                                            runningSprite.setAction(action);
                                        });
                                    }
                                }
                                if (conditionIds.size() > 1) {
                                    menu.show(threadCanvas, monolithLeftBorder - menu.getPreferredSize().width - 10, runningSprite.getYPosition());
                                } else {
                                    ThreadSprite sprite = conditionThreads.get(0);
                                    Condition condition = sprite.getCondition();
                                    runningSprite.setCondition(condition, sprite.getConditionId());
                                    runningSprite.setAction(action);
                                }
//            setCssSelected("wait");
                            }
                        } else {
                            runningSprite.setAction(action);
                        }
                    }
                }
        );
    }

    public void reset() {
        super.reset();
        conditionId = 0;
        threadContext.setSlideLabel("ReentrantLock");
//        Set styleSelectors = threadContext.setSnippetFile("synchronized.html");
//        setStyleSelectors(styleSelectors);
        lock = new ReentrantLock();
    }
}
