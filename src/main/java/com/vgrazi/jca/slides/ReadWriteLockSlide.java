package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.sprites.ThreadSprite;
import com.vgrazi.jca.sprites.WriteThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ReadWriteLockSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void run() {
        reset();
        threadContext.setSlideLabel("ReadWriteLock");
        threadContext.addButton("readLock()", () -> {
            ThreadSprite<String> sprite = (ThreadSprite<String>) applicationContext.getBean("threadSprite");
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
            sprite.attachAndStartRunnable(()->{
                readWriteLock.writeLock().lock();
                while("write-lock".equals(sprite.getHolder())) {
                    Thread.yield();
                }
                readWriteLock.writeLock().unlock();
                threadContext.stopThread(sprite);

            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("release", ()->{
            ThreadSprite<String> runningThread = (ThreadSprite<String>) threadContext.getRunningThread();
            if (runningThread != null) {
                runningThread.setHolder("release");
            }
            threadContext.stopThread(runningThread);

        });

        threadContext.addButton("reset", this::reset);

        threadContext.setVisible();
    }

    @Override
    protected void reset() {
        super.reset();
        readWriteLock = new ReentrantReadWriteLock();
    }
}
