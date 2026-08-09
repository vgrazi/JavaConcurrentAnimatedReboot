package com.vgrazi.jca.slides;


import com.vgrazi.jca.sprites.ThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("preview")
@Component
public class StructuredConcurrencySlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${arrow-length}")
    private int arrowLength;

    private StructuredTaskScope<String, Void> structuredTaskScope;
    private final List<ThreadSprite<Boolean>> scopeThreads = new CopyOnWriteArrayList<>();
    private final Set<ThreadSprite<Boolean>> completed = ConcurrentHashMap.newKeySet();
    private final Set<ThreadSprite<Boolean>> failed = ConcurrentHashMap.newKeySet();
    private final Set<ThreadSprite<Boolean>> cancelled = ConcurrentHashMap.newKeySet();
    private final AtomicInteger taskCounter = new AtomicInteger();
    private JButton joinButton;
    private volatile boolean joinWaitingForCompletion;

    @Override
    public void run() {
        reset();

        threadContext.addButton("scope.fork(subtask)", this::addForkAction);
        joinButton = threadContext.addButton("scope.join()", () -> joinScopeAction(4));
        joinButton.setEnabled(true);
        threadContext.addButton("subtask completes", () -> completeTaskAction(2));
        threadContext.addButton("subtask fails", () -> failTaskAction(3));
        threadContext.addButton("scope.shutdown()", () -> shutdownScopeAction(5));
        threadContext.addButton("Reset", this::reset);

        threadContext.setVisible();
    }

    private void addForkAction() {
        final int taskId = taskCounter.incrementAndGet();
        if (taskId == 1) {
            highlightSnippet(11);
        } else if (taskId == 2) {
            highlightSnippet(12);
        }

        // Create new scope if needed
        if (structuredTaskScope == null || scopeThreads.isEmpty()) {
            structuredTaskScope = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow());
        }

        final ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("structuredConcurrencyRunnerThreadSprite");
        threadSprite.setXPosition(leftBorder + arrowLength);
        threadSprite.setLabel("task-" + taskId);

        // Store the task ID in the sprite for use in the subtask
        final String taskLabel = "task-" + taskId;
        
        // Fork the subtask using the real scope
        structuredTaskScope.fork(() -> {
            // Simulate work with sleep
            try {
                Thread.sleep(5000); // Long sleep to allow user interaction
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedException(taskLabel + " cancelled");
            }
            return taskLabel + "-result";
        });

        // Start a separate thread to visualize the subtask execution
        threadSprite.attachAndStartRunnable(() -> {
            // In Java 25, subtask.get() can only be called after scope.join()
            // The sprite will be marked as completed when the scope is joined
            // Just keep the sprite running until explicitly stopped
            while (threadSprite.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, false);

        scopeThreads.add(threadSprite);
        threadContext.addSprite(threadSprite);
    }



    private void completeTaskAction(int state) {
        boolean transitionedToPostJoinSnippet = false;
        // Mark exactly one active sprite as completed
        ThreadSprite<Boolean> firstActive = getFirstActiveTask();
        if (firstActive != null) {
            firstActive.setMessage("ok");
            completed.add(firstActive);
            scopeThreads.remove(firstActive);
            threadContext.stopThread(firstActive);
            if (isScopeFinished() && joinButton != null) {
                joinButton.setEnabled(true);
            }
            if (joinWaitingForCompletion && isScopeFinished()) {
                // Once the last visible task completes while join() is waiting, fade out the join highlight.
                highlightSnippet(7);
                threadCanvas.fadeHighlightBox();
                joinWaitingForCompletion = false;
                transitionedToPostJoinSnippet = true;
            } else if (joinWaitingForCompletion) {
                // Keep the join line highlighted until the final task completes.
                highlightSnippet(4);
            }
        }
        if (!joinWaitingForCompletion && !transitionedToPostJoinSnippet) {
            highlightSnippet(state);
        }
        setMessage("Subtasks complete when their work finishes naturally", Color.white);
    }

    private void failTaskAction(int state) {
        highlightSnippet(state);
        // Show one failed subtask, then cancel and remove the rest.
        ThreadSprite<Boolean> failedTask = getFirstActiveTask();
        if (failedTask != null) {
            failedTask.setMessage("failed");
            failed.add(failedTask);
            scopeThreads.remove(failedTask);
            threadContext.stopThread(failedTask);
        }
        scopeThreads.stream().filter(this::isActive).forEach(sprite -> {
            sprite.setMessage("cancelled");
            cancelled.add(sprite);
            scopeThreads.remove(sprite);
            threadContext.stopThread(sprite);
        });
        joinWaitingForCompletion = false;
        threadCanvas.fadeHighlightBox();
        if (joinButton != null) {
            joinButton.setEnabled(true);
        }
        setMessage("Scope shutdown - cancelling all subtasks", Color.pink);
    }





    private void joinScopeAction(int state) {
        if (joinButton != null) {
            joinButton.setEnabled(false);
        }
        highlightSnippet(state);
        highlightAllSubtasks();
        joinWaitingForCompletion = true;

        if (structuredTaskScope == null) {
            joinWaitingForCompletion = false;
            if (joinButton != null) {
                joinButton.setEnabled(true);
            }
            setMessage("No scope to join", Color.white);
            return;
        }

        // Call join on the real scope in a separate thread to avoid blocking UI
        Thread joinThread = new Thread(() -> {
            try {
                structuredTaskScope.join();
                SwingUtilities.invokeLater(() -> {
                    // Mark all active sprites as completed after successful join
                    scopeThreads.stream().filter(this::isActive).forEach(sprite -> {
                        sprite.setMessage("ok");
                        completed.add(sprite);
                        scopeThreads.remove(sprite);
                        threadContext.stopThread(sprite);
                    });
                    highlightSnippet(7);
                    setMessage("join() returned successfully", Color.green);
                    threadCanvas.fadeHighlightBox();
                    joinWaitingForCompletion = false;
                    if (joinButton != null) {
                        joinButton.setEnabled(true);
                    }
                });
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> {
                    setMessage("join() interrupted", Color.pink);
                    joinWaitingForCompletion = false;
                    if (joinButton != null) {
                        joinButton.setEnabled(true);
                    }
                });
            }
        });
        joinThread.start();
        setMessage("join() waiting for subtasks...", Color.white);
    }

    private void shutdownScopeAction(int state) {
        highlightSnippet(state);
        joinWaitingForCompletion = false;
        if (structuredTaskScope != null) {
            // In Java 25, close() cancels the scope and waits for threads to finish
            // Run in background thread to avoid blocking UI
            Thread closeThread = new Thread(() -> {
                try {
                    structuredTaskScope.close();
                } catch (Exception e) {
                    // Ignore exceptions during close
                }
            });
            closeThread.start();
        }
        scopeThreads.stream().filter(this::isActive).forEach(cancelled::add);
        threadCanvas.fadeHighlightBox();
        setMessage("Scope cancelled", Color.white);
    }

    private ThreadSprite<Boolean> getFirstActiveTask() {
        return scopeThreads.stream().filter(this::isActive).findFirst().orElse(null);
    }

    private boolean isScopeFinished() { return scopeThreads.stream().noneMatch(this::isActive); }

    private boolean isActive(ThreadSprite<Boolean> threadSprite) {
        return !completed.contains(threadSprite)
                && !failed.contains(threadSprite)
                && !cancelled.contains(threadSprite);
    }

    @Override
    public void reset() {
        super.reset();
        threadCanvas.clearHighlightBox();
        threadCanvas.hideMonolith(true);
        threadContext.setSlideLabel("Structured Concurrency");
        taskCounter.set(0);
        scopeThreads.clear();
        completed.clear();
        failed.clear();
        cancelled.clear();
        joinWaitingForCompletion = false;
        structuredTaskScope = null;
        if (joinButton != null) {
            joinButton.setEnabled(true);
        }
        setSnippetFile("structured-concurrency.html");
        highlightSnippet(0);
    }

    private void highlightAllSubtasks() {
        if (scopeThreads.isEmpty()) {
            return;
        }
        final int virtualRenderXOffset = -100;
        final int verticalPadding = 10;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (ThreadSprite<Boolean> threadSprite : scopeThreads) {
            int renderedX = virtualRenderXOffset + threadSprite.getXPosition();
            minX = Math.min(minX, renderedX - arrowLength - 16);
            maxX = Math.max(maxX, renderedX + 24);
            minY = Math.min(minY, threadSprite.getYPosition() - 14 - verticalPadding);
            maxY = Math.max(maxY, threadSprite.getYPosition() + 14 + verticalPadding);
        }

        int width = Math.max(40, maxX - minX);
        int height = Math.max(24, maxY - minY);
        threadCanvas.showHighlightBox(minX, minY, width, height, Color.green, true);
    }
}