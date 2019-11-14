package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class CyclicBarrierSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

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
                    setCssSelected("await");
                    cyclicBarrier.await();
                    count--;
                    if(count == 0) {
                        firstThread = null;
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                    sprite.setRetreating(true);
                    setMessage(e.toString());
                }
                finally {
                    threadContext.stopThread(sprite);
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("await(time, TimeUnit)", () -> {
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
                    setCssSelected("await-timed");
                    cyclicBarrier.await(2, TimeUnit.SECONDS);

                    threadContext.stopThread(sprite);
                    count--;
                    if(count == 0) {
                        firstThread = null;
                    }
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    e.printStackTrace();
                    sprite.setRetreating(true);
                }
                finally {
                    threadContext.stopThread(sprite);
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("barrier.reset()", () ->{
            setCssSelected("reset");

            cyclicBarrier.reset();
            initializeInstanceFields();
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        initializeInstanceFields();
        threadContext.setSlideLabel("CyclicBarrier");
        cyclicBarrier = new CyclicBarrier(4);
        Set styleSelectors = threadContext.setSnippetFile("cyclic-barrier.html");
        setStyleSelectors(styleSelectors);
        resetCss();
    }

    private void initializeInstanceFields() {
        firstThread = null;
        messages.setText("");
        count = 0;
    }
}
