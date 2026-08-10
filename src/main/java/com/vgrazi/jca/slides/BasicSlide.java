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

    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    private ApplicationContext applicationContext;

    public BasicSlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        reset();
        threadContext.addButton("Take a bow", () -> {
            reset();
            int y = threadContext.getYPosition();
            final int verticalGap = pixelsPerYStep + 10;
            // create a new sprite
            ObjectSprite objectSprite = (ObjectSprite) applicationContext.getBean("objectSprite");
            objectSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(objectSprite);
            objectSprite.setMessage("ObjectSprite");
            threadContext.addSprite(objectSprite);
            ThreadSprite sprite = (ThreadSprite) applicationContext.getBean("threadSprite");
            sprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(sprite);
            sprite.setMessage("ThreadSprite");
            threadContext.addSprite(sprite);
            RunnerThreadSprite runnerThreadSprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
            runnerThreadSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(runnerThreadSprite);
            runnerThreadSprite.setMessage("RunnerThreadSprite");
            threadContext.addSprite(runnerThreadSprite);
            RunnableSprite runnableSprite  = (RunnableSprite) applicationContext.getBean("runnableSprite");
            runnableSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(runnableSprite);
            runnableSprite.setMessage("RunnableSprite");
            threadContext.addSprite(runnableSprite);

//            FutureSprite futureSprite = (FutureSprite) applicationContext.getBean("completableFutureSprite");
//            attachAndStartRunnable(futureSprite);
//            futureSprite.setMessage("FutureSprite");
//            threadContext.addSprite(futureSprite);
//            threadContext.addYPixels(10);
            FutureRunnableSprite futureRunnableSprite = (FutureRunnableSprite) applicationContext.getBean("futureRunnableSprite");
            futureRunnableSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(futureRunnableSprite);
            futureRunnableSprite.setMessage("FutureRunnableSprite");
            threadContext.addSprite(futureRunnableSprite);

            PooledThreadSprite pooledThreadSprite  = (PooledThreadSprite) applicationContext.getBean("pooledThreadSprite");
            // Pooled sprites keep separate active/pooled lanes; render this one on the pooled lane.
            pooledThreadSprite.setPooled(true);
            attachAndStartRunnable(pooledThreadSprite);
            pooledThreadSprite.setMessage("PooledThreadSprite");
            threadContext.addSprite(pooledThreadSprite);
            int pooledLaneY = pooledThreadSprite.getYPosition();

            ThreadSprite interruptedSprite = (ThreadSprite) applicationContext.getBean("runnerThreadSprite");
            int interruptedY = pooledLaneY + verticalGap + 15;
            interruptedSprite.setYPosition(interruptedY);
            interruptedSprite.attachAndStartRunnable(()->{
                while(true){Thread.yield();}
            }, true);
            interruptedSprite.setMessage("InterruptedSprite");
            interruptedSprite.getThread().interrupt();
            threadContext.addSprite(interruptedSprite);
            y = Math.max(y, interruptedY + verticalGap);

            GetterThreadSprite getterThreadSprite  = (GetterThreadSprite) applicationContext.getBean("getterSprite");
            getterThreadSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(getterThreadSprite);
            getterThreadSprite.setMessage("GetterThreadSprite");
            threadContext.addSprite(getterThreadSprite);
            WriteThreadSprite writeThreadSprite = (WriteThreadSprite) applicationContext.getBean("writeThreadSprite");
            writeThreadSprite.setYPosition(y);
            y += verticalGap;
            attachAndStartRunnable(writeThreadSprite);
            writeThreadSprite.setMessage("WriteThreadSprite");
            threadContext.addSprite(writeThreadSprite);

            RunnerThreadSprite taggedRunnerThreadSprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
            taggedRunnerThreadSprite.setYPosition(y);
            // give it an action tag to test for
            taggedRunnerThreadSprite.setAction("someRunningTag");
            // always attach a runnable, and then start the thread
            taggedRunnerThreadSprite.attachAndStartRunnable(()->{
                while(taggedRunnerThreadSprite.getAction().equals("someRunningTag")) {
                    // Even tho it is consuming CPU, we need to leave it running so as not to change the thread state
                    // in any case, let it yield to running threads
                    Thread.yield();
                }
                threadContext.stopThread(taggedRunnerThreadSprite);
            }, true);
            // Always add the sprite to the thread context.
            taggedRunnerThreadSprite.setMessage("TaggedRunnerThreadSprite");
            threadContext.addSprite(taggedRunnerThreadSprite);
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
        }, true);
    }

    public void reset() {
        super.reset();
        threadContext.setSlideLabel("Credits");
        setSnippetFile("some.html");
    }
}
