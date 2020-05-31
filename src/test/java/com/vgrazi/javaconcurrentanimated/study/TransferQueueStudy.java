package com.vgrazi.javaconcurrentanimated.study;

import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

public class TransferQueueStudy {
    Logger logger = Logger.getLogger("TransferQueueStudy");
//    @BeforeClass
//    public void setLogger() {
//        System.setProperty("java.util.logging.SimpleFormatter.format","[%1$tF %1$tT] [%4$-7s] %5$s");
//    }
    @Test
    public void doesTransferQueueTransferWaitForTaker() throws InterruptedException {
        TransferQueue transferQueue = new LinkedTransferQueue();
        new Thread(()->{
            try {
                Thread.sleep(1000);
                logger.info(Thread.currentThread() + " Taking");
                Object take = transferQueue.take();
                logger.info(Thread.currentThread() + " Took " + take);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        logger.info(Thread.currentThread() + " Transferring");
        transferQueue.transfer("xxxx");
        logger.info(Thread.currentThread() + " Transfer complete");
    }

    @Test
    public void doesTransferQueuePutWaitForTaker() throws InterruptedException {
        TransferQueue transferQueue = new LinkedTransferQueue();
        new Thread(()->{
            try {
                Thread.sleep(1000);
                logger.info(Thread.currentThread() + " Taking");
                Object take = transferQueue.take();
                logger.info(Thread.currentThread() + " Took " + take);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        logger.info(Thread.currentThread() + " Transferring");
        transferQueue.put("xxxx");
        logger.info(Thread.currentThread() + " Transfer complete");
    }

    @AfterEach
    public void keepAlive() throws InterruptedException {
        Thread.sleep(5_000);
    }
}
