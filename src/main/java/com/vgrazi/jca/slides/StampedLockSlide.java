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

        threadContext.addButton("readLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.readLock();
                sprite.setSpecialId(stamp);
                readStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                stampedLock.unlockRead(stamp);
                threadContext.stopThread(sprite);
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("tryOptimisticRead()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            sprite.setStroke(dottedStroke);
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.tryOptimisticRead();
                sprite.setSpecialId(stamp);
                optimisticReadStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }

                boolean valid = stampedLock.validate(stamp);
                if(valid){
                    sprite.setMessage("Valid :)");
                }
                else {
                    sprite.setMessage("Invalid :(");
                    sprite.setRetreating();
                }
                System.out.println(valid);
                threadContext.stopThread(sprite);
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("writeLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("writeThreadSprite");
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                long stamp = stampedLock.writeLock();
                sprite.setSpecialId(stamp);
                writeStamps.offer(stamp);
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                System.out.println("unlockWrite(" + stamp);
                stampedLock.unlockWrite(stamp);
                threadContext.stopThread(sprite);
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("unlockRead(stamp)", () -> {
            Long stamp = readStamps.peek();
            if (stamp != null) {
                ThreadSprite readLockSprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if(readLockSprite != null) {
                    readStamps.poll();
                    readLockSprite.setHolder("done");
                }
                else {
                    System.out.println("No running read threads for stamp " +stamp);
                }
            }
        });

        threadContext.addButton("unlockWrite(stamp)", () -> {
            Long stamp = writeStamps.peek();
            if (stamp != null) {
                ThreadSprite writeLockSprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if(writeLockSprite != null) {
                    writeStamps.poll();
                    writeLockSprite.setHolder("done");
                }
                else {
                    System.out.println("No running write threads for stamp " +stamp);
                }
            }
        });
        threadContext.addButton("validate(stamp)", () -> {
            Long stamp = optimisticReadStamps.peek();
            if (stamp != null) {
                ThreadSprite sprite = threadContext.getFirstRunningThreadOfSpecialId(stamp);
                if (sprite != null) {
                    optimisticReadStamps.poll();
                    sprite.setHolder("done");
                }
            }
            else {
                System.out.println("No running optimistic threads for stamp " +stamp);
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
