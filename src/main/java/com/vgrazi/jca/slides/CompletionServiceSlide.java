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

    private int getterYPos = 90;
    private Random random = new Random();

    private ExecutorService executor = Executors.newCachedThreadPool();
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private CompletionService<ThreadSprite> completionService;

    @Override
    public void run() {
        reset();
        threadContext.addButton("Add running thread", () -> {
                    // create a new sprite
                    FutureRunnableSprite sprite = (FutureRunnableSprite) applicationContext.getBean("futureRunnableSprite");
                    // give it an action tag to test for
                    sprite.setAction("running");
                    Future[] futureArray = new Future[1];
                    sprite.attachAndStartRunnable(() -> {
                        futureArray[0] = completionService.submit(() -> {
                            sprite.setFuture(futureArray[0]);
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

        threadContext.addButton("complete", () -> {
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

        threadContext.addButton("take().get()", () -> {
            executor.execute(()-> {
                GetterThreadSprite getter = (GetterThreadSprite) applicationContext.getBean("getterSprite");
                getter.setYPosition(getterYPos);
                threadContext.addSprite(getter);
                getterYPos +=30;

                getter.attachAndStartRunnable(() -> {
                    try {
                        Future future = completionService.take();
                        if (future != null) {
                            System.out.println("Called take. Now calling get");
                            FutureRunnableSprite originalSprite = (FutureRunnableSprite) future.get();
                            getter.setYPosition(originalSprite.getYPosition());
                            originalSprite.setDone(true);
                            System.out.println("Completed get" + originalSprite);
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

        threadContext.addButton("reset", this::reset);
    }

    @Override
    public void reset() {
        super.reset();
        threadContext.setSlideLabel("CompletionService");
        completionService = new ExecutorCompletionService<>(pool);
        setSnippetFile("completion-service.html");
    }

}
