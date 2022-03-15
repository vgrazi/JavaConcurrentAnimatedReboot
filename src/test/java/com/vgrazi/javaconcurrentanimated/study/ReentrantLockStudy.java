package com.vgrazi.javaconcurrentanimated.study;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

public class ReentrantLockStudy{
    @Test
    public void testAwaitAfterInterrupted() throws InterruptedException{

        // spin thread1
        // thread1 grab lock
        // call thread1.interrupt
        // thread1 call await
        // expect an InterruptedException
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] expected = {true};
        boolean[] flag = new boolean[]{true};
        // create the lock
        ReentrantLock lock = new ReentrantLock();
        Thread thread1 = new Thread(()->
        {
            lock.lock();
            // loop until flag becomes false, after interrupt is called
            while(flag[0]);
            // interrupt was called. Now await and prove the interrupt falls thru
            Condition condition = lock.newCondition();
            try{
                Thread.currentThread().interrupt();
                condition.await();
                System.out.println("Thread 1 Exiting");
                // Interrupt never happeed. test failed
                expected[0] = false;
            }catch(InterruptedException e){
                e.printStackTrace();
                // test passed
            }
            latch.countDown();
        });
        System.out.println("Starting thread1");
        thread1.start();
        System.out.println("Interrupting thread1");
        thread1.interrupt();
        // set flag to false to force the loop to exit
        System.out.println("Setting flag=false to signal thread1 to await()");
        flag[0] = false;
        latch.await();
        if(!expected[0]){
            fail();
        }
    }

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
