package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.context.ThreadSprite;
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

    public void run() {
        Phaser phaser = new Phaser(4);
        threadContext.addButton("await()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setTargetState(ThreadSprite.TargetState.awaitAdvance);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("arrive()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setTargetState(ThreadSprite.TargetState.arrive);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("arriveAndAwaitAdvance()", ()->{
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setTargetState(ThreadSprite.TargetState.arriveAndAwaitAdvance);
            addRunnable(phaser, sprite);
        });

        threadContext.addButton("register()", ()->{
            // todo: register should set a message on the UI message area, indicating the number
            //  of permits. No need to create a thread
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setTargetState(ThreadSprite.TargetState.register);
            addRunnable(phaser, sprite);
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
                    case arriveAndAwaitAdvance:
                        phase = phaser.arriveAndAwaitAdvance();
                        sprite.setTargetState(ThreadSprite.TargetState.release);
                        break;
                    case register:
                        phase = phaser.register();
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
