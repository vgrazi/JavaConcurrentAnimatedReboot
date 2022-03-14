package com.vgrazi.javaconcurrentanimated.study;

import org.junit.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockStudy{
    @Test
    public void testInterruptOnPlainLock() throws InterruptedException {
        // spin thread1
        // let it grab a lock
        // and sleep
        // spin thread2
        // let it grab the same lock (and wait)
        // interrupt thread2
        // see what it does
        boolean flag = true;
        // create the lock
        ReentrantLock lock = new ReentrantLock();
        Thread thread1 = new Thread(()->
        {
            lock.lock();
            while(flag);
            System.out.println("Thread 1 Exiting");
        });
        Thread thread2 = new Thread(()->
        {
            try {
                lock.lockInterruptibly();
                System.out.println("Thread 2 got lock");
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        Thread.sleep(500);
        thread2.start();
        System.out.println("Interrupting");
        thread2.interrupt();
        System.out.println("Interrupted");

        // see what happens if a non-interruptible thread is interrupted. Test this when it is waiting for the lock, and when it has the lock
    }
    public static void main(String[] args){
        ReentrantLock lock = new ReentrantLock();
        lock.lock();

/// do work
        Condition condition = lock.newCondition();
        try{
            condition.await();
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }

        lock.unlock();

        lock.lock();

/// do work
        condition.signal();
        condition.signalAll();
        lock.unlock();


        try{
            lock.lockInterruptibly();
            // do work
            lock.unlock();
        }catch(InterruptedException e){
            // interrupted
        }finally{
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

        Thread.currentThread().interrupt();

        boolean success = lock.tryLock();
        if(success){
            // do work
            lock.unlock();
        }
    }
}
