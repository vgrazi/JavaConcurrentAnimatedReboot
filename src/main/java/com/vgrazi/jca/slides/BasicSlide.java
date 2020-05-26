package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Use this as a starting point for constructing new slides. To use, uncomment the addButton for basicSlide in JCAFrame
 */
@Component
public class BasicSlide extends Slide {

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${monolith-left-border}")
    private int leftBorder;

    private ApplicationContext applicationContext;

    public BasicSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        reset();
        threadContext.addButton("Add running thread", () -> {
            // create a new sprite
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            attachAndStartRunnable(objectSprite);
            objectSprite.setMessage("ObjectSprite");
            threadContext.addSprite(objectSprite);
            threadContext.addYPixels(10);
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            attachAndStartRunnable(sprite);
            sprite.setMessage("ThreadSprite");
            threadContext.addSprite(sprite);
            threadContext.addYPixels(10);
            RunnerThreadSprite runnerThreadSprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
            attachAndStartRunnable(runnerThreadSprite);
            runnerThreadSprite.setMessage("RunnerThreadSprite");
            threadContext.addSprite(runnerThreadSprite);
            threadContext.addYPixels(10);
            RunnableSprite runnableSprite  = (RunnableSprite) applicationContext.getBean("runnableSprite");
            attachAndStartRunnable(runnableSprite);
            runnableSprite.setMessage("RunnableSprite");
            threadContext.addSprite(runnableSprite);
            threadContext.addYPixels(10);
            FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("futureSprite");
//            attachAndStartRunnable(futureSprite);
            futureSprite.setMessage("FutureSprite");
            threadContext.addSprite(futureSprite);
            threadContext.addYPixels(10);
            FutureRunnableSprite futureRunnableSprite = (FutureRunnableSprite) applicationContext.getBean("futureRunnableSprite");
            attachAndStartRunnable(futureRunnableSprite);
            futureRunnableSprite.setMessage("FutureRunnableSprite");
            threadContext.addSprite(futureRunnableSprite);
            threadContext.addYPixels(10);
            PooledThreadSprite pooledThreadSprite  = (PooledThreadSprite) applicationContext.getBean("pooledThreadSprite");
            attachAndStartRunnable(pooledThreadSprite);
            pooledThreadSprite.setMessage("PooledThreadSprite");
            threadContext.addSprite(pooledThreadSprite);
            threadContext.addYPixels(10);
            GetterThreadSprite getterThreadSprite  = (GetterThreadSprite) applicationContext.getBean("getterSprite");
            attachAndStartRunnable(getterThreadSprite);
            getterThreadSprite.setMessage("GetterThreadSprite");
            threadContext.addSprite(getterThreadSprite);
            threadContext.addYPixels(10);
            WriteThreadSprite writeThreadSprite = (WriteThreadSprite) applicationContext.getBean("writeThreadSprite");
            attachAndStartRunnable(writeThreadSprite);
            writeThreadSprite.setMessage("WriteThreadSprite");
            threadContext.addSprite(writeThreadSprite);

            // give it an action tag to test for
            runnerThreadSprite.setAction("someRunningTag");
            // always attach a runnable, and then start the thread
            runnerThreadSprite.attachAndStartRunnable(()->{
                while(runnerThreadSprite.getAction().equals("someRunningTag")) {
                    // Even tho it is consuming CPU, we need to leave it running so as not to change the thread state
                    // in any case, let it yield to running threads
                    Thread.yield();
                }
                threadContext.stopThread(runnerThreadSprite);
            });
            // Always add the sprite to the thread context.
            threadContext.addSprite(runnerThreadSprite);
        });
        threadContext.addButton("Stop thread", () -> {
            // get a running thread, if any
            ThreadSprite sprite = threadContext.getRunningThread();
            // stop it
            if(sprite != null) {
                // set it to anything except "someRunningTag" so it will exit the loop above
                sprite.setAction("done");
            }
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    private void attachAndStartRunnable(ThreadSprite sprite) {
        sprite.attachAndStartRunnable(()->{
            Object mutex = new Object();
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void reset() {
        super.reset();
        threadContext.setSlideLabel("Basic Slide");
        setSnippetFile("some.html");
    }
}
