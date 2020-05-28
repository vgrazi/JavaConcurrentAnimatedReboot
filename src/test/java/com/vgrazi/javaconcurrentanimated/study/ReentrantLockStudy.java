package com.vgrazi.javaconcurrentanimated.study;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockStudy {
    public static void main(String[] args) {
ReentrantLock lock = new ReentrantLock();
lock.lock();

/// do work
Condition condition = lock.newCondition();
try {
    condition.await();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

lock.unlock();

lock.lock();

/// do work
condition.signal();
condition.signalAll();
lock.unlock();


try {
    lock.lockInterruptibly();
    // do work
    lock.unlock();
} catch (InterruptedException e) {
    // interrupted
} finally {
    if(lock.isHeldByCurrentThread()){
        lock.unlock();
    }
}

Thread.currentThread().interrupt();

boolean success = lock.tryLock();
if(success) {
    // do work
    lock.unlock();
}
    }
}
