package com.vgrazi.jca.slides;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Phaser;

@Component
public class PhaserSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public void run() throws InterruptedException {
        Phaser phaser = new Phaser(4);
        Logging.sleepAndLog(0, "Creating Phaser");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite1.setTargetState(ThreadSprite.TargetState.awaitAdvance);
        Logging.sleepAndLog("Adding first await ", sprite1);
        addRunnable(phaser, sprite1);

        ThreadSprite sprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite2.setTargetState(ThreadSprite.TargetState.awaitAdvance);
        Logging.sleepAndLog("Adding second await ",sprite2);
        addRunnable(phaser, sprite2);

        ThreadSprite arriveSprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite1.setTargetState(ThreadSprite.TargetState.arrive);
        Logging.sleepAndLog("Arriving ", arriveSprite1);
        addRunnable(phaser, arriveSprite1);

        ThreadSprite arriveSprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite2.setTargetState(ThreadSprite.TargetState.arrive);
        Logging.sleepAndLog("Arriving ", arriveSprite2);
        addRunnable(phaser, arriveSprite2);

        ThreadSprite arriveSprite3 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite3.setTargetState(ThreadSprite.TargetState.arrive);
        Logging.sleepAndLog("Arriving ", arriveSprite3);
        addRunnable(phaser, arriveSprite3);

        ThreadSprite arriveSprite4 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite4.setTargetState(ThreadSprite.TargetState.arrive);
        Logging.sleepAndLog("Arriving ", arriveSprite4);
        addRunnable(phaser, arriveSprite4);


    }

    private void addRunnable(Phaser phaser, ThreadSprite sprite) {
        sprite.setRunnable(() -> {
            int phase =0;
            while (sprite.isRunning()) {
                if (sprite.getTargetState() == ThreadSprite.TargetState.release) {
                    threadContext.stopThread(sprite);
                    break;
                }
                switch (sprite.getTargetState()) {
                    case awaitAdvance:
                        phaser.awaitAdvance(phase);
                        sprite.setTargetState(ThreadSprite.TargetState.release);
                        break;
                    case arrive:
                        phase = phaser.arrive();
                        System.out.println("Phase:" + phase);
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
