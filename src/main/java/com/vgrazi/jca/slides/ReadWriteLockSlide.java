package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
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

    public void run() {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Logging.log("Created RW Lock");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite1.setAction("readLock");
        addRunnable(readWriteLock, sprite1);
        Logging.log("Added first read lock " + sprite1);

        ThreadSprite sprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite2.setAction("readLock");
        addRunnable(readWriteLock, sprite2);
        Logging.log("Added second read lock " + sprite2);

        ThreadSprite spriteWriteLock = (ThreadSprite) applicationContext.getBean("threadSprite");
        spriteWriteLock.setAction("writeLock");
        addRunnable(readWriteLock, spriteWriteLock);
        Logging.log("Added write lock runnable " + spriteWriteLock);

        sprite1.setAction("releaseReadLock");
        Logging.log("Released readlock 1");

        sprite2.setAction("releaseReadLock");
        Logging.log("Released readlock 2");

//        ThreadSprite sprite4 = (ThreadSprite) applicationContext.getBean("threadSprite");
//        sprite4.setAction(ThreadSprite.TargetState.readLock);
//        addRunnable(readWriteLock, sprite4);
//        logAndSleep("Added third read lock " + sprite4);
//
//        spriteWriteLock.setAction(ThreadSprite.TargetState.releaseWriteLock);



    }

    private void addRunnable(ReadWriteLock rwLock, ThreadSprite sprite) {
        sprite.attachAndStartRunnable(() -> {
            while (sprite.isRunning()) {
                if ("release".equals(sprite.getAction())) {
                    threadContext.stopThread(sprite);
                    break;
                }
                switch (sprite.getAction()) {
                    case "readLock":
                        rwLock.readLock().lock();
                        sprite.setAction("default");

                        break;
                    case "writeLock":
                        rwLock.writeLock().lock();
                        sprite.setAction("default");
                        break;
                    case "releaseReadLock":
                        rwLock.readLock().unlock();
                        sprite.setAction("release");
                        break;
                    case "releaseWriteLock":
                        rwLock.writeLock().unlock();
                        sprite.setAction("release");
                        break;
                    case "default":
                        Thread.yield();
                        break;
                }
            }
            System.out.println(sprite + " exiting");
        });
        threadContext.addSprite(sprite);
    }
}
