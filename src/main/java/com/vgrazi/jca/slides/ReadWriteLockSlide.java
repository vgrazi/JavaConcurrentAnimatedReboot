package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.ThreadSprite;
import com.vgrazi.jca.sprites.WriteThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ReadWriteLockSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Autowired
    private Stroke basicStroke;

    public void run() {
        reset();
        threadContext.addButton("readWriteLock.readLock().lock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            setState(1);
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(() -> {
                readWriteLock.readLock().lock();
                while ("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                readWriteLock.readLock().unlock();
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("readWriteLock.writeLock().lock()", () -> {
            ThreadSprite<String> sprite = (WriteThreadSprite<String>) applicationContext.getBean("writeThreadSprite");
            sprite.setHolder("write-lock");
            sprite.setSpecialId(1);
            sprite.attachAndStartRunnable(() -> {
                setState(2);
                readWriteLock.writeLock().lock();
                while ("write-lock".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                if ("downgrade".equals(sprite.getHolder())) {
                    readWriteLock.readLock().lock();
                    readWriteLock.writeLock().unlock();
                    sprite.setStroke(basicStroke);
                    sprite.setHolder("running");
                    while ("running".equals(sprite.getHolder())) {
                        Thread.yield();
                    }
                    readWriteLock.readLock().unlock();
                } else {
                    readWriteLock.writeLock().unlock();
                }
                threadContext.stopThread(sprite);

            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("lock.unlock()", () -> {
            ThreadSprite<String> runningThread = (ThreadSprite<String>) threadContext.getRunningThread();
            if (runningThread != null) {
                setState(3);
                runningThread.setHolder("release");
                threadContext.stopThread(runningThread);
            }
        });

        threadContext.addButton("(downgrade to read)", () -> {
            ThreadSprite<String> runningWriteThread = (ThreadSprite<String>) threadContext.getFirstRunningThreadOfSpecialId(1);
            if (runningWriteThread != null) {
                setState(4);
                runningWriteThread.setHolder("downgrade");
            }
        });

        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();
    }

    @Override
    public void reset() {
        super.reset();
        threadContext.setSlideLabel("ReadWriteLock");
        setSnippetFile("read-write-lock.html");
        setImage("images/reentrantRWLock.jpg");
        readWriteLock = new ReentrantReadWriteLock();
    }
}
