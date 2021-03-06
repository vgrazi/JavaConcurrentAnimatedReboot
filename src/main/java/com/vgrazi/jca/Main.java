package com.vgrazi.jca;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.slides.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Runs the Slide selected from the menu
 */
@Component
public class Main implements CommandLineRunner {
    @Autowired
    private ThreadContext threadContext;

    @Autowired()
    private SynchronizedSlide synchronizedSlide;

    @Autowired()
    private ReentrantLockSlide reentrantLockSlide;

    @Autowired
    private ReadWriteLockSlide readWriteLockSlide;

    @Autowired
    private BlockingQueueSlide blockingQueueSlide;

    @Autowired
    private TransferQueueSlide transferQueueSlide;

    @Autowired
    private IntroSlide introSlide;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PhaserSlide phaserSlide;

    @Autowired
    private CyclicBarrierSlide cyclicBarrierSlide;

    @Autowired
    private CompletableFutureSlide completableFutureSlide;

    @Autowired
    private ExecutorsSlide executorsSlide;

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                threadContext.startAnimationThread();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        String param;
        if(args.length == 0) {
            param = "intro";
        }
        else {
            param = args[0];
        }
        switch (param) {
            case "synchronized":
                threadContext.registerSlide(synchronizedSlide);
                break;
            case "reentrant-lock":
                threadContext.registerSlide(reentrantLockSlide);
                break;
            case "read-write-lock":
                threadContext.registerSlide(readWriteLockSlide);
                break;
            case "blocking-queue":
                threadContext.registerSlide(blockingQueueSlide);
                break;
            case "transfer-queue":
                threadContext.registerSlide(transferQueueSlide);
                break;
            case "phaser":
                threadContext.registerSlide(phaserSlide);
                break;
            case "completable-future":
                threadContext.registerSlide(completableFutureSlide);
                break;
            case "cyclic-barrier":
                threadContext.registerSlide(cyclicBarrierSlide);
                break;
            case "executors":
                threadContext.registerSlide(executorsSlide);
                break;
            case "intro":
                threadContext.registerSlide(introSlide);
                break;
        }
    }
}
