package com.vgrazi.jca.slides;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ReadWriteLockSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public void run() throws InterruptedException {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Logging.sleepAndLog("Created RW Lock");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite1.setTargetState(ThreadSprite.TargetState.readLock);
        addRunnable(readWriteLock, sprite1);
        Logging.sleepAndLog("Added first read lock " + sprite1);

        ThreadSprite sprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite2.setTargetState(ThreadSprite.TargetState.readLock);
        addRunnable(readWriteLock, sprite2);
        Logging.sleepAndLog("Added second read lock " + sprite2);

        ThreadSprite spriteWriteLock = (ThreadSprite) applicationContext.getBean("threadSprite");
        spriteWriteLock.setTargetState(ThreadSprite.TargetState.writeLock);
        addRunnable(readWriteLock, spriteWriteLock);
        Logging.sleepAndLog("Added write lock runnable " + spriteWriteLock);

        sprite1.setTargetState(ThreadSprite.TargetState.releaseReadLock);
        Logging.sleepAndLog("Released readlock 1");

        sprite2.setTargetState(ThreadSprite.TargetState.releaseReadLock);
        Logging.sleepAndLog("Released readlock 2");

//        ThreadSprite sprite4 = (ThreadSprite) applicationContext.getBean("threadSprite");
//        sprite4.setTargetState(ThreadSprite.TargetState.readLock);
//        addRunnable(readWriteLock, sprite4);
//        sleepAndLog("Added third read lock " + sprite4);
//
//        spriteWriteLock.setTargetState(ThreadSprite.TargetState.releaseWriteLock);



    }

    private void addRunnable(ReadWriteLock rwLock, ThreadSprite sprite) {
        sprite.setRunnable(() -> {
            while (sprite.isRunning()) {
                if (sprite.getTargetState() == ThreadSprite.TargetState.release) {
                    threadContext.stopThread(sprite);
                    break;
                }
                switch (sprite.getTargetState()) {
                    case readLock:
                        rwLock.readLock().lock();
                        sprite.setTargetState(ThreadSprite.TargetState.default_state);

                        break;
                    case writeLock:
                        rwLock.writeLock().lock();
                        sprite.setTargetState(ThreadSprite.TargetState.default_state);
                        break;
                    case releaseReadLock:
                        rwLock.readLock().unlock();
                        sprite.setTargetState(ThreadSprite.TargetState.release);
                        break;
                    case releaseWriteLock:
                        rwLock.writeLock().unlock();
                        sprite.setTargetState(ThreadSprite.TargetState.release);
                        break;
                    case default_state:
                        Thread.yield();
                        break;
                }
            }
            System.out.println(sprite + " exiting");
        });
        threadContext.addThread(sprite);
    }
}
