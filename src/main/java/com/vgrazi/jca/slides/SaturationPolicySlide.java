package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.PooledThreadSprite;
import com.vgrazi.jca.sprites.RunnableSprite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SaturationPolicySlide extends Slide {

    @Value("${monolith-right-border}")
    private int rightBorder;

    @Value("${monolith-left-border}")
    private int leftBorder;

    private ApplicationContext applicationContext;

    private ThreadPoolExecutor executor;
    private SynchronousQueue<Runnable> workQueue;
    private final AtomicBoolean callerRunsActive = new AtomicBoolean(false);
    private volatile RunnableSprite callerRunsRunnableSprite;

    public SaturationPolicySlide(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void run() {
        reset();

        threadContext.addButton("execute", this::executeAction);

        threadContext.addButton("(done)", () -> {
            PooledThreadSprite<String> sprite = threadContext.getRunningPooledThread();
            if (sprite != null) {
                sprite.setRunning(false);
                sprite.setPooled(true);
            } else if (callerRunsActive.get()) {
                callerRunsActive.set(false);
            }
        });

        threadContext.addButton("AbortPolicy", () -> {
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
            highlightSnippet(2);
            setMessage("Policy: AbortPolicy  –  fill pool then execute", Color.red);
        });
        threadContext.addButton("CallerRunsPolicy", () -> {
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            highlightSnippet(3);
            setMessage("Policy: CallerRunsPolicy  –  fill pool then execute", Color.yellow);
        });
        threadContext.addButton("DiscardPolicy", () -> {
            reset();
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
            highlightSnippet(4);
            setMessage("Policy: DiscardPolicy  –  fill pool then execute", Color.orange);
        });
        threadContext.addButton("DiscardOldestPolicy", () -> {
            reset();
            // Safe wrapper: discards the task without re-submitting to avoid
            // infinite recursion that DiscardOldestPolicy causes with SynchronousQueue.
            executor.setRejectedExecutionHandler((r, e) -> e.getQueue().poll());
            highlightSnippet(5);
            setMessage("Policy: DiscardOldestPolicy  –  fill pool then execute", Color.orange);
        });

        threadContext.addButton("reset", this::reset);
        threadContext.setVisible();
    }

    /**
     * Submits a task to the executor from a background thread so that:
     * <ul>
     *   <li>CallerRunsPolicy runs on the submitter thread, not the EDT.</li>
     *   <li>We can detect a silent Discard after execute() returns.</li>
     *   <li>AbortPolicy exceptions are caught and reported cleanly.</li>
     * </ul>
     *
     * taskState values:
     *   0 = not yet started (detect Discard here)
     *   1 = running on a pool thread (normal execution)
     *   2 = running on the caller/submitter thread (CallerRunsPolicy)
     */
    private void executeAction() {
        RunnableSprite runnableSprite = (RunnableSprite) applicationContext.getBean("runnableSprite");
        threadContext.addSprite(runnableSprite);
        highlightSnippet(1);

        // 0=not started, 1=pool thread, 2=caller thread (CallerRunsPolicy)
        AtomicInteger taskState = new AtomicInteger(0);

        Runnable task = () -> {
            PooledThreadSprite<String> sprite =
                    (PooledThreadSprite) threadContext.getThreadSprite(Thread.currentThread());

            if (sprite != null) {
                // ── Normal pool-thread execution ──────────────────────────────────
                taskState.set(1);
                sprite.setPooled(false);
                sprite.setRunning(true);
                sprite.setYPosition(runnableSprite.getYPosition());
                runnableSprite.setThread(Thread.currentThread());
                while (sprite.isRunning()) {
                    Thread.yield();
                }
                sprite.setPooled(true);
                runnableSprite.setDone();
                threadContext.stopThread(runnableSprite);
                highlightSnippet(0);

            } else {
                // ── CallerRunsPolicy: task runs on the background submitter thread ─
                taskState.set(2);
                callerRunsRunnableSprite = runnableSprite;
                callerRunsActive.set(true);
                runnableSprite.setThread(Thread.currentThread());
                SwingUtilities.invokeLater(() ->
                        setMessage("CallerRunsPolicy: caller thread is running this task (click done)", Color.yellow));
                while (callerRunsActive.get()) {
                    Thread.onSpinWait();
                }
                SwingUtilities.invokeLater(() -> {
                    setMessage("Calling thread finished the task", Color.green);
                    runnableSprite.setDone();
                    threadContext.stopThread(runnableSprite);
                });
                callerRunsRunnableSprite = null;
            }
        };

        // Submit from a daemon background thread so the EDT stays responsive.
        Thread submitter = new Thread(() -> {
            try {
                executor.execute(task);
            } catch (RejectedExecutionException e) {
                // ── AbortPolicy ─────────────────────────────────────────────────────
                SwingUtilities.invokeLater(() -> {
                    setMessage("AbortPolicy: RejectedExecutionException thrown!", Color.red);
                    runnableSprite.setRetreating();
                    threadContext.stopThread(runnableSprite);
                });
                return;
            }

            // execute() returned without exception.
            if (taskState.get() == 2) {
                // CallerRunsPolicy already handled everything inside the task.
                return;
            }

            // Give a pool thread a moment to pick up the task.
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            if (taskState.get() == 0) {
                // ── DiscardPolicy / DiscardOldestPolicy: task silently dropped ────
                SwingUtilities.invokeLater(() -> {
                    setMessage("Task was silently discarded!", Color.orange);
                    runnableSprite.setRetreating();
                    threadContext.stopThread(runnableSprite);
                });
            }
            // taskState == 1: pool thread is handling it – nothing more to do here.
        }, "saturation-submitter");
        submitter.setDaemon(true);
        submitter.start();
    }

    public void reset() {
        callerRunsActive.set(false);
        callerRunsRunnableSprite = null;
        if (executor != null) {
            executor.shutdownNow();
        }
        super.reset();
        setSnippetFile("saturation-policy.html");
        threadContext.setSlideLabel("Saturation Policy");
        workQueue = new SynchronousQueue<>();
        executor = new ThreadPoolExecutor(0, 4, 2, TimeUnit.SECONDS,
                workQueue,
                r -> {
                    PooledThreadSprite<String> sprite =
                            (PooledThreadSprite) applicationContext.getBean("pooledThreadSprite");
                    Thread thread = new Thread(r);
                    sprite.setThread(thread);
                    sprite.setPooled(true);
                    sprite.setRunning(false);
                    threadContext.addSprite(sprite);
                    return thread;
                }
        );
    }
}
