package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.StampedLock;

@Component
public class StampedLockSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    @Autowired
    private Stroke dottedStroke;

    private StampedLock stampedLock = new StampedLock();

    private Queue<Long> readStamps = new LinkedList<>();
    private Queue<Long> writeStamps = new LinkedList<>();
    private Queue<Long> optimisticReadStamps = new LinkedList<>();

    public void run() {
        reset();

        threadContext.addButton("lock.readLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            highlightSnippet(1);
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.readLock();
                setMessage("readLock acquired. Stamp=" + stamp);
                sprite.setSpecialId(stamp);
                readStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                if("wrong_stamp".equals(sprite.getHolder())) {
                    // demo what happens if things go wrong
                    try {
                        // the first 7 bits of the stamp are used for tracking permits. The remaining 57 are used for
                        // the stamp. Upshot is, if you just add say 1, it will still be valid.
                        stampedLock.unlockRead(-stamp);
                    } catch (Exception e) {
                        sprite.setRetreating();
                        sprite.setMessage("Validation failed");
                        setMessage("unlocked wrong stamp. Needs=" + stamp + ". " + e);
                        e.printStackTrace();
                    }
                }
                else {
                    stampedLock.unlockRead(stamp);
                    setMessage("unlockedRead. Stamp=" + stamp);
                }
                threadContext.stopThread(sprite);
            }, true);
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("lock.writeLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("writeThreadSprite");
            highlightSnippet(6);
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.writeLock();
                setMessage("writeLock acquired. Stamp=" + stamp);
                sprite.setSpecialId(stamp);
                writeStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                println("unlockWrite(" + stamp);
                stampedLock.unlockWrite(stamp);
                setMessage("unlockedWrite. Stamp=" + stamp);
                threadContext.stopThread(sprite);
            }, true);
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("lock.tryOptimisticRead()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            highlightSnippet(5);
            sprite.setStroke(dottedStroke);
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.tryOptimisticRead();
                setMessage("Optimistic read acquired. Stamp=" + stamp);
                sprite.setSpecialId(stamp);
                optimisticReadStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }

                boolean valid = stampedLock.validate(stamp);
                if(valid){
                    sprite.setMessage("Valid :)");
                    setMessage("validated. Stamp=" + stamp);
                }
                else {
                    sprite.setMessage("Invalid :(");
                    setMessage("validation failed. Stamp=" + stamp);
                    sprite.setRetreating();
                }
                println(valid);
                threadContext.stopThread(sprite);
            }, true);
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("lock.unlockRead(stamp)", () -> {
            Long stamp = readStamps.peek();
            if (stamp != null) {
                highlightSnippet(2);
                ThreadSprite readLockSprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if(readLockSprite != null) {
                    readStamps.poll();
                    readLockSprite.setHolder("done");
                }
                else {
                    println("No running read threads for stamp " +stamp);
                }
            }
        });

        threadContext.addButton("lock.unlockRead(wrong_stamp)", () -> {
            Long stamp = readStamps.peek();
            if (stamp != null) {
                highlightSnippet(2);
                ThreadSprite readLockSprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if(readLockSprite != null) {
                    readStamps.poll();
                    readLockSprite.setHolder("wrong_stamp");
                }
                else {
                    println("No running read threads for stamp " +stamp);
                }
            }
        });

        threadContext.addButton("lock.unlockWrite(stamp)", () -> {
            Long stamp = writeStamps.peek();
            if (stamp != null) {
                highlightSnippet(3);
                ThreadSprite writeLockSprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if(writeLockSprite != null) {
                    writeStamps.poll();
                    writeLockSprite.setHolder("done");
                }
                else {
                    println("No running write threads for stamp " +stamp);
                }
            }
        });
        threadContext.addButton("validate(stamp)", () -> {
            Long stamp = optimisticReadStamps.peek();
            if (stamp != null) {
                highlightSnippet(4);
                ThreadSprite sprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if (sprite != null) {
                    optimisticReadStamps.poll();
                    sprite.setHolder("done");
                }
            }
            else {
                println("No running optimistic threads for stamp " +stamp);
            }
        });
        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    @Override
    public void reset() {
        super.reset();
        threadContext.setSlideLabel("StampedLock");
        setSnippetFile("stamped-lock.html");
        stampedLock = new StampedLock();
        readStamps.clear();
        writeStamps.clear();
        optimisticReadStamps.clear();

    }
}
