package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Component
public class CyclicBarrierSlide extends Slide {

    @Autowired
    ApplicationContext applicationContext;

    private CyclicBarrier cyclicBarrier = new CyclicBarrier(4);
    private ThreadSprite firstThread;
    private int count;
    public void run() {
        reset();
        threadContext.addButton("await()", () -> {
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("await");
            sprite.attachAndStartRunnable(() -> {
                try {
                    if (firstThread == null) {
                        firstThread = sprite;
                    }
                    else {
                        sprite.setXPosition(firstThread.getXPosition()-20);
                    }
                    count++;
                    cyclicBarrier.await();
                    count--;
                    if(count == 0) {
                        firstThread = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
            threadContext.addSprite(sprite);
        });
        ;

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    protected void reset() {
        super.reset();
        cyclicBarrier = new CyclicBarrier(4);
    }
}
