package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class CyclicBarrierSlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    private CyclicBarrier cyclicBarrier;
    private ThreadSprite firstThread;
    private int count;
    public void run() {
        reset();
        threadContext.addButton("await()", () -> {
            setMessage("");
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("await");
            sprite.attachAndStartRunnable(() -> {
                try {
                    setState(1);
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
                    sprite.setRetreating();
                    setMessage(e.toString());
                }
                finally {
                    threadContext.stopThread(sprite);
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("await(time, TimeUnit)", () -> {
            setMessage("");
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setAction("await");
            sprite.attachAndStartRunnable(() -> {
                setState(2);
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
                    setMessage(e.getMessage());
                    sprite.setRetreating();
                }
                finally {
                    threadContext.stopThread(sprite);
                }
            });
            threadContext.addSprite(sprite);
        });

        threadContext.addButton("barrier.reset()", () ->{
            setMessage("");
            setState(3);
            cyclicBarrier.reset();
            initializeInstanceFields();
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    public void reset() {
        super.reset();
        setState(0);
        initializeInstanceFields();
        threadContext.setSlideLabel("CyclicBarrier");
        setSnippetFile("cyclic-barrier.html");
        setImage("images/cyclicBarrier.jpg");
        setMessage("");
        cyclicBarrier = new CyclicBarrier(4, ()->{
            setState(4);
            setMessage("Barrier Action Hit!!");
        });
    }

    private void initializeInstanceFields() {
        firstThread = null;
        messages.setText("");
        count = 0;
    }
}
