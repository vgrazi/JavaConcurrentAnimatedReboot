package com.vgrazi.jca.slides;

import com.vgrazi.jca.ThreadContext;
import com.vgrazi.jca.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.Phaser;

@Component
public class PhaserSlide implements Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;
    private long stepDelay = 2000;

    public void run() throws InterruptedException {
        Phaser phaser = new Phaser(4);
        sleep("Created Phaser");
        ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite1.setTargetState(ThreadSprite.TargetState.awaitAdvance);
        addRunnable(phaser, sprite1);
        sleep("Added first await " + sprite1);

        ThreadSprite sprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        sprite2.setTargetState(ThreadSprite.TargetState.awaitAdvance);
        addRunnable(phaser, sprite2);
        sleep("Added second await " + sprite2);

        ThreadSprite arriveSprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite1.setTargetState(ThreadSprite.TargetState.arrive);
        addRunnable(phaser, arriveSprite1);
        sleep("Arrive " + arriveSprite1);

        ThreadSprite arriveSprite2 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite2.setTargetState(ThreadSprite.TargetState.arrive);
        addRunnable(phaser, arriveSprite2);
        sleep("Arrive " + arriveSprite2);

        ThreadSprite arriveSprite3 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite3.setTargetState(ThreadSprite.TargetState.arrive);
        addRunnable(phaser, arriveSprite3);
        sleep("Arrive " + arriveSprite3);

        ThreadSprite arriveSprite4 = (ThreadSprite) applicationContext.getBean("threadSprite");
        arriveSprite4.setTargetState(ThreadSprite.TargetState.arrive);
        addRunnable(phaser, arriveSprite4);
        sleep("Arrive " + arriveSprite4);

        System.exit(0);

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

    private void sleep(String message) throws InterruptedException {
        sleep(message, stepDelay);
    }

    private void sleep(String message, long delay) throws InterruptedException {
        System.out.println(LocalTime.now() + " " + message);
        Thread.sleep(delay);
    }
}
