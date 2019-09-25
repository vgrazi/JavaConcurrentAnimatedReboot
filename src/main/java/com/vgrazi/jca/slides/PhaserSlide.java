package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
import com.vgrazi.jca.util.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Phaser;

import static com.vgrazi.jca.util.Logging.log;

@Component
public class PhaserSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ThreadContext threadContext;

    public void run() throws InterruptedException {
        Phaser phaser = new Phaser(4);
        threadContext.addButton("await()", ()->{
            ThreadSprite sprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite1.setTargetState(ThreadSprite.TargetState.awaitAdvance);
            log("Adding first await ", sprite1);
            addRunnable(phaser, sprite1);
        });

        threadContext.addButton("arrive()", ()->{
            ThreadSprite arriveSprite1 = (ThreadSprite) applicationContext.getBean("threadSprite");
            arriveSprite1.setTargetState(ThreadSprite.TargetState.arrive);
            log("Arriving ", arriveSprite1);
            addRunnable(phaser, arriveSprite1);
        });
        threadContext.setVisible();


    }

    private void addRunnable(Phaser phaser, ThreadSprite sprite) {
        sprite.attachAndStartRunnable(() -> {
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
