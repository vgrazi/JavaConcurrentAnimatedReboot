package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.PooledThreadSprite;
import com.vgrazi.jca.sprites.RunnableSprite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private BlockingQueue<Runnable> workQueue;
    private final AtomicBoolean callerRunsActive = new AtomicBoolean(false);
    private volatile RunnableSprite callerRunsRunnableSprite;
    private final ConcurrentLinkedDeque<TaskSubmission> queuedSubmissions = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<Runnable, TaskSubmission> queuedByTask = new ConcurrentHashMap<>();
    private volatile PolicyMode policyMode = PolicyMode.ABORT;

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
            policyMode = PolicyMode.ABORT;
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
            highlightSnippet(2);
            setMessage("Policy: AbortPolicy  –  fill pool then execute", Color.red);
        });
        threadContext.addButton("CallerRunsPolicy", () -> {
            reset();
            policyMode = PolicyMode.CALLER_RUNS;
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            highlightSnippet(3);
            setMessage("Policy: CallerRunsPolicy  –  fill pool then execute", Color.yellow);
        });
        threadContext.addButton("DiscardPolicy", () -> {
            reset();
            policyMode = PolicyMode.DISCARD;
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
            highlightSnippet(4);
            setMessage("Policy: DiscardPolicy  –  fill pool then execute", Color.orange);
        });
        threadContext.addButton("DiscardOldestPolicy", () -> {
            reset();
            policyMode = PolicyMode.DISCARD_OLDEST;
            switchToDiscardOldestExecutor();
            executor.setRejectedExecutionHandler((r, e) -> {
                Runnable dropped = e.getQueue().poll();
                TaskSubmission oldestQueued = dropped == null ? null : queuedByTask.remove(dropped);
                if (oldestQueued != null) {
                    queuedSubmissions.remove(oldestQueued);
                    oldestQueued.discarded.set(true);
                    SwingUtilities.invokeLater(() -> {
                        setMessage("DiscardOldestPolicy: oldest queued task discarded", Color.orange);
                        oldestQueued.sprite.setRetreating();
                        threadContext.stopThread(oldestQueued.sprite);
                    });
                }
                try {
                    e.execute(r);
                } catch (RejectedExecutionException ignored) {
                    // Rare race (shutdown/re-saturation): drop the incoming task.
                }
            });
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
        TaskSubmission submission = new TaskSubmission(runnableSprite);
        final Runnable[] taskRef = new Runnable[1];

        Runnable task = () -> {
            queuedSubmissions.remove(submission);
            queuedByTask.remove(taskRef[0]);
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
                if (policyMode == PolicyMode.DISCARD_OLDEST && executor.getQueue().contains(task)) {
                    // Track only tasks that are actually enqueued.
                    submission.task = task;
                    queuedByTask.put(task, submission);
                    queuedSubmissions.offerLast(submission);
                }
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

            if (policyMode == PolicyMode.DISCARD && taskState.get() == 0) {
                // ── DiscardPolicy / DiscardOldestPolicy: task silently dropped ────
                SwingUtilities.invokeLater(() -> {
                    setMessage("Task was silently discarded!", Color.orange);
                    runnableSprite.setRetreating();
                    threadContext.stopThread(runnableSprite);
                });
            }
            // taskState == 1: pool thread is handling it – nothing more to do here.
        }, "saturation-submitter");
        taskRef[0] = task;
        submitter.setDaemon(true);
        submitter.start();
    }

    public void reset() {
        cleanup();
        super.reset();
        setSnippetFile("saturation-policy.html");
        threadContext.setSlideLabel("Saturation Policy");
        policyMode = PolicyMode.ABORT;
        createExecutor(false);
    }

    @Override
    public void cleanup() {
        callerRunsActive.set(false);
        callerRunsRunnableSprite = null;
        queuedSubmissions.clear();
        queuedByTask.clear();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        workQueue = null;
        super.cleanup();
    }

    private void switchToDiscardOldestExecutor() {
        if (executor != null) {
            executor.shutdownNow();
        }
        queuedSubmissions.clear();
        queuedByTask.clear();
        createExecutor(true);
    }

    private void createExecutor(boolean boundedQueue) {
        workQueue = boundedQueue ? new ArrayBlockingQueue<>(1) : new SynchronousQueue<>();
        int corePoolSize = boundedQueue ? 4 : 0;
        executor = new ThreadPoolExecutor(corePoolSize, 4, 2, TimeUnit.SECONDS,
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

    private enum PolicyMode {
        ABORT,
        CALLER_RUNS,
        DISCARD,
        DISCARD_OLDEST
    }

    private static class TaskSubmission {
        final RunnableSprite sprite;
        final AtomicBoolean discarded = new AtomicBoolean(false);
        volatile Runnable task;

        private TaskSubmission(RunnableSprite sprite) {
            this.sprite = sprite;
        }
    }
}
