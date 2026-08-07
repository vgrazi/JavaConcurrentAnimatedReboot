package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.RunnerThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.Stream;

/**
 * Animates Java 25 (preview, JEP 505) Structured Concurrency.
 *
 * The monolith represents the {@link StructuredTaskScope}. Each forked subtask is a
 * virtual-thread sprite that parks inside the scope until the user resolves it.
 * <ul>
 *   <li>open(allSuccessfulOrThrow) - the scope waits for EVERY child; one failure fails fast
 *       and cancels the remaining siblings.</li>
 *   <li>open(anySuccessfulResultOrThrow) - the first successful child wins and the losing
 *       siblings are cancelled.</li>
 * </ul>
 * The whole flow drives the REAL {@code StructuredTaskScope} API on a dedicated scope-owner
 * thread, so fork/join and sibling cancellation are genuine, not faked.
 */
@Component
public class StructuredConcurrencySlide extends Slide {

    private static final int PENDING = 0;
    private static final int SUCCEED = 1;
    private static final int FAIL = 2;

    /** Sentinel placed on the command queue to tell the owner thread to call join() */
    private static final Object JOIN = new Object();

    private static final Color GREEN = new Color(0, 190, 0);
    private static final Color RED = new Color(255, 90, 90);

    private enum JoinerType {ALL, ANY}

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${monolith-left-border}")
    private int leftBorder;
    @Value("${arrow-length}")
    private int arrowLength;

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Object> commandQueue = new LinkedBlockingQueue<>();

    private volatile Thread ownerThread;
    private volatile boolean scopeOpen;
    private volatile boolean joining;
    private volatile JoinerType joinerType = JoinerType.ALL;
    private int counter;

    /** Holds the per-subtask sprite and the user-controlled outcome */
    private static final class Task {
        final String name;
        final Object lock = new Object();
        volatile int outcome = PENDING;
        RunnerThreadSprite sprite;

        Task(String name) {
            this.name = name;
        }
    }

    public void run() {
        reset();
        threadContext.addButton("open(allSuccessfulOrThrow)", () -> openScope(JoinerType.ALL));
        threadContext.addButton("open(anySuccessfulResultOrThrow)", () -> openScope(JoinerType.ANY));
        threadContext.addButton("fork(subtask)", this::forkSubtask);
        threadContext.addButton("Succeed a subtask", () -> resolve(SUCCEED));
        threadContext.addButton("Fail a subtask", () -> resolve(FAIL));
        threadContext.addButton("join()", this::joinScope);
        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();
    }

    private void openScope(JoinerType type) {
        if (scopeOpen) {
            setMessage("Scope already open - call join() or Reset first", RED);
            return;
        }
        tasks.clear();
        commandQueue.clear();
        counter = 0;
        joinerType = type;
        joining = false;
        scopeOpen = true;
        highlightSnippet(type == JoinerType.ALL ? 1 : 2);
        setMessage(type == JoinerType.ALL
                ? "Opened scope with Joiner.allSuccessfulOrThrow()"
                : "Opened scope with Joiner.anySuccessfulResultOrThrow()");
        startOwnerThread();
    }

    private void forkSubtask() {
        if (!scopeOpen) {
            setMessage("Open a scope first", RED);
            return;
        }
        if (joining) {
            setMessage("join() already called - cannot fork after join()", RED);
            return;
        }
        highlightSnippet(3);
        Task task = new Task("task-" + (++counter));
        RunnerThreadSprite sprite = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setMessage(task.name);
        task.sprite = sprite;
        tasks.add(task);
        commandQueue.add(task);
        setMessage("scope.fork(" + task.name + ")");
    }

    private void resolve(int outcome) {
        if (!scopeOpen) {
            setMessage("Open a scope first", RED);
            return;
        }
        Task task = tasks.stream().filter(t -> t.outcome == PENDING).findFirst().orElse(null);
        if (task == null) {
            setMessage("No pending subtasks to resolve", RED);
            return;
        }
        task.outcome = outcome;
        synchronized (task.lock) {
            task.lock.notifyAll();
        }
        if (outcome == FAIL) {
            highlightSnippet(6);
            setMessage(task.name + " throws - the scope will cancel its siblings", RED);
        } else {
            setMessage(task.name + " completed successfully", GREEN);
        }
    }

    private void joinScope() {
        if (!scopeOpen) {
            setMessage("Open a scope first", RED);
            return;
        }
        if (joining) {
            setMessage("join() already called", RED);
            return;
        }
        joining = true;
        highlightSnippet(joinerType == JoinerType.ALL ? 4 : 5);
        setMessage("scope.join() - blocking the owner until the joiner is satisfied");
        commandQueue.add(JOIN);
    }

    private void startOwnerThread() {
        ownerThread = new Thread(this::runScope, "SC-scope-owner");
        ownerThread.start();
    }

    /**
     * Runs the real StructuredTaskScope. Forks are pulled off the command queue (so the owner
     * thread is the one forking, as the API requires) until the JOIN sentinel arrives.
     */
    @SuppressWarnings("unchecked")
    private void runScope() {
        Joiner<String, ?> joiner = joinerType == JoinerType.ALL
                ? Joiner.<String>allSuccessfulOrThrow()
                : Joiner.<String>anySuccessfulResultOrThrow();
        try (StructuredTaskScope<String, ?> scope = StructuredTaskScope.open(joiner)) {
            while (true) {
                Object command = commandQueue.take();
                if (command == JOIN) {
                    break;
                }
                Task task = (Task) command;
                CountDownLatch started = new CountDownLatch(1);
                scope.fork(makeSubtask(task, started));
                started.await();
                threadContext.addSprite(task.sprite);
            }
            Object result = scope.join();
            if (joinerType == JoinerType.ALL) {
                List<String> values = ((Stream<Subtask<String>>) result).map(Subtask::get).toList();
                highlightSnippet(4);
                setMessage("join() returned " + values + "  ->  next step runs", GREEN);
            } else {
                highlightSnippet(5);
                setMessage("join() returned \"" + result + "\"  ->  next step runs (losers cancelled)", GREEN);
            }
            runNextStep(true);
        } catch (StructuredTaskScope.FailedException e) {
            highlightSnippet(6);
            setMessage("FailedException: " + rootMessage(e) + "  ->  siblings cancelled, next step skipped", RED);
            runNextStep(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            joining = false;
            scopeOpen = false;
            ownerThread = null;
        }
    }

    /**
     * The subtask body running on a scope-managed virtual thread. It binds its sprite to the
     * live virtual thread, then parks until the user succeeds/fails it, or until the scope
     * cancels it (interrupt) because a sibling already decided the outcome.
     */
    private Callable<String> makeSubtask(Task task, CountDownLatch started) {
        return () -> {
            task.sprite.setThread(Thread.currentThread());
            started.countDown();
            try {
                synchronized (task.lock) {
                    while (task.outcome == PENDING) {
                        task.lock.wait();
                    }
                }
                if (task.outcome == FAIL) {
                    task.sprite.setMessage(task.name + " threw");
                    task.sprite.setRetreating();
                    threadContext.stopThread(task.sprite);
                    throw new RuntimeException(task.name + " failed");
                }
                task.sprite.setMessage(task.name + " -> value");
                threadContext.stopThread(task.sprite);
                return task.name;
            } catch (InterruptedException e) {
                task.sprite.setMessage(task.name + " cancelled");
                task.sprite.setRetreating();
                threadContext.stopThread(task.sprite);
                throw e;
            }
        };
    }

    /**
     * Visualizes the code that runs after join(): on success a downstream thread runs through
     * the scope; on failure nothing runs (the message already explains it was skipped).
     */
    private void runNextStep(boolean success) {
        if (!success) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            RunnerThreadSprite next = (RunnerThreadSprite) applicationContext.getBean("runnerThreadSprite");
            next.setXPosition(leftBorder + arrowLength);
            next.setMessage("next step");
            next.attachAndStartRunnable(() -> {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 1500) {
                    Thread.yield();
                }
                threadContext.stopThread(next);
            }, true);
            threadContext.addSprite(next);
        });
    }

    private String rootMessage(Throwable e) {
        Throwable cause = e.getCause();
        return cause != null ? cause.getMessage() : e.getMessage();
    }

    public void reset() {
        Thread owner = ownerThread;
        if (owner != null) {
            owner.interrupt();
        }
        // wake any parked subtasks so their virtual threads can unwind
        tasks.forEach(t -> {
            synchronized (t.lock) {
                t.lock.notifyAll();
            }
        });
        super.reset();
        scopeOpen = false;
        joining = false;
        ownerThread = null;
        counter = 0;
        tasks.clear();
        commandQueue.clear();
        threadCanvas.hideMonolith(false);
        threadContext.setSlideLabel("Structured");
        threadContext.setSlideLabel("Concurrency", 1);
        setSnippetFile("structured-concurrency.html");
        highlightSnippet(0);
    }
}
