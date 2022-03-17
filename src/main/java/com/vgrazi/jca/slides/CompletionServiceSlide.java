package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.FutureRunnableSprite;
import com.vgrazi.jca.sprites.GetterThreadSprite;
import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Component
public class CompletionServiceSlide extends Slide {

    private Random random = new Random();

    private ExecutorService executor = Executors.newCachedThreadPool();
    private CompletionService<ThreadSprite> completionService;
    private ExecutorService pool;

    @Override
    public void run() {
        reset();
        threadContext.addButton("submit()", () -> {
                    highlightSnippet(1);
                    // create a new sprite
                    FutureRunnableSprite sprite = (FutureRunnableSprite) applicationContext.getBean("futureRunnableSprite");
                    // set to starting, so that it won't come up when we get all the runners when the complete button is pressed
                    sprite.setAction("starting");
                    Future[] futureArray = new Future[1];
                    sprite.attachAndStartRunnable(() -> {
                        futureArray[0] = completionService.submit(() -> {
                            sprite.setFuture(futureArray[0]);
                            sprite.setThread(Thread.currentThread());
                            sprite.setAction("running");

                            // attach a runnable and start the thread
                            while (sprite.getAction().equals("running")) {
                                // Even tho it is consuming CPU, we need to leave it running so as not to change the thread state
                                // in any case, let it yield to running threads
                                Thread.yield();
                            }

                            return sprite;
                        });
                    });
                    // Always add the sprite to the thread context.
                    threadContext.addSprite(sprite);
                }
        );

        threadContext.addButton("take().get()", () -> {
            executor.execute(() -> {
                highlightSnippet(2);
                GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
                threadContext.setGetterNextYPos(getter);
                threadContext.addSprite(getter);

                getter.attachAndStartRunnable(() -> {
                    try {
                        Future future = completionService.take();
                        if (future != null) {
                            println("Called take. Now calling get");
                            FutureRunnableSprite originalSprite = (FutureRunnableSprite) future.get();
                            getter.setYPosition(originalSprite.getYPosition());
                            originalSprite.setDone(true);
                            println("Completed get" + originalSprite);
                            threadContext.stopThread(originalSprite);
                            threadContext.stopThread(getter);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        });

        threadContext.addButton("(complete)", () -> {
            // We need to find a not-done sprite, and mark it as done.
            List<FutureRunnableSprite> notCompleteSprites = threadContext.getThreadSpritesWithAction("running");
            if (notCompleteSprites.size() > 0) {
                // pick a random one and complete it
                int index = random.nextInt(notCompleteSprites.size());
                FutureRunnableSprite sprite = notCompleteSprites.get(index);
                sprite.setAction("complete");
                // setting the sprite to "complete" will end the thread loop, thereby causing the future to be complete.
                // the rendering algorithm will leave that kissing the right side of the monolith, until it is marked as done
            }
        });

        threadContext.addButton("reset", this::reset);
    }

    @Override
    public void reset() {
        super.reset();
        threadContext.setSlideLabel("CompletionService");
        pool = Executors.newFixedThreadPool(4);
        completionService = new ExecutorCompletionService<>(pool);
        setSnippetFile("completion-service.html");
    }

}
