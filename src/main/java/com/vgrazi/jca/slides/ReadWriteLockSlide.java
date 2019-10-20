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
        threadContext.addButton("readLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("runnerThreadSprite");
            sprite.setHolder("running");
            sprite.attachAndStartRunnable(()->{
                readWriteLock.readLock().lock();
                while("running".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                readWriteLock.readLock().unlock();
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("writeLock", ()->{
            ThreadSprite<String> sprite = (WriteThreadSprite<String>) applicationContext.getBean("writeThreadSprite");
            sprite.setHolder("write-lock");
            sprite.setSpecialId(1);
            sprite.attachAndStartRunnable(()->{
                readWriteLock.writeLock().lock();
                while("write-lock".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                if("downgrade".equals(sprite.getHolder())){
                    readWriteLock.readLock().lock();
                    readWriteLock.writeLock().unlock();
                    sprite.setStroke(basicStroke);
                    sprite.setHolder("running");
                    while("running".equals(sprite.getHolder())) {
                        Thread.yield();
                    }
                    readWriteLock.readLock().unlock();
                }
                else {
                    readWriteLock.writeLock().unlock();
                }
                threadContext.stopThread(sprite);

            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("(downgrade to read)", ()->{
            ThreadSprite<String> runningWriteThread = (ThreadSprite<String>) threadContext.getFirstRunningThreadOfSpecialId(1);
            if (runningWriteThread != null) {
                runningWriteThread.setHolder("downgrade");
            }
        });

        threadContext.addButton("release", ()->{
            ThreadSprite<String> runningThread = (ThreadSprite<String>) threadContext.getRunningThread();
            if (runningThread != null) {
                runningThread.setHolder("release");
                threadContext.stopThread(runningThread);
            }
        });

        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();
    }

    @Override
    protected void reset() {
        super.reset();
        threadContext.setSlideLabel("ReadWriteLock");
        readWriteLock = new ReentrantReadWriteLock();
    }
}
