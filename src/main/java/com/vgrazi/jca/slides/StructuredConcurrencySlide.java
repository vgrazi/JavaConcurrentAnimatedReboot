package com.vgrazi.jca.slides;


import com.vgrazi.jca.slides.Slide;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StructuredConcurrencySlide extends Slide {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${monolith-left-border}")
    private int leftBorder;

    @Value("${arrow-length}")
    private int arrowLength;

    private final List<ThreadSprite<Boolean>> scopeThreads = new CopyOnWriteArrayList<>();
    private final Set<ThreadSprite<Boolean>> completed = ConcurrentHashMap.newKeySet();
    private final Set<ThreadSprite<Boolean>> failed = ConcurrentHashMap.newKeySet();
    private final Set<ThreadSprite<Boolean>> cancelled = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean scopeCancelled = new AtomicBoolean(false);
    private final AtomicInteger taskCounter = new AtomicInteger();
    private JButton joinButton;

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

        // Reset cancellation state if scope is empty to allow new subtasks after failure
        if (scopeThreads.isEmpty()) {
            scopeCancelled.set(false);
        }

        final ThreadSprite<Boolean> threadSprite = (ThreadSprite<Boolean>) applicationContext.getBean("virtualRunnerThreadSprite");
        threadSprite.setXPosition(leftBorder + arrowLength);
        threadSprite.setLabel("task-" + taskId);

        threadSprite.attachAndStartRunnable(() -> {
            while (threadSprite.isRunning()) {
                if (completed.contains(threadSprite) || failed.contains(threadSprite)) {
                    break;
                }
                if (scopeCancelled.get()) {
                    cancelled.add(threadSprite);
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cancelled.add(threadSprite);
                    break;
                }
            }

            if (completed.contains(threadSprite)) {
                threadSprite.setMessage("ok");
            } else if (failed.contains(threadSprite)) {
                threadSprite.setMessage("failed");
            } else if (cancelled.contains(threadSprite)) {
                threadSprite.setMessage("cancelled");
            }

            scopeThreads.remove(threadSprite);
            threadContext.stopThread(threadSprite);
        }, false);

        scopeThreads.add(threadSprite);
        threadContext.addSprite(threadSprite);
    }



    private void completeTaskAction(int state) {
        highlightSnippet(state);
        ThreadSprite<Boolean> threadSprite = getFirstActiveTask();
        if (threadSprite == null) {
            setMessage("No active subtask to complete", Color.white);
            return;
        }

        completed.add(threadSprite);
        if (isScopeFinished()) {
            threadCanvas.fadeHighlightBox();
            if (joinButton != null) {
                joinButton.setEnabled(true);
            }
            highlightSnippet(7);
            setMessage("All subtasks completed; parent thread can run next step", Color.green);
        }
    }

    private void failTaskAction(int state) {
        highlightSnippet(state);
        ThreadSprite<Boolean> threadSprite = getFirstActiveTask();
        if (threadSprite == null) {
            setMessage("No active subtask to fail", Color.white);
            return;
        }

        failed.add(threadSprite);
        scopeCancelled.set(true);
        scopeThreads.stream()
                .filter(task -> task != threadSprite)
                .filter(this::isActive)
                .forEach(cancelled::add);
        threadCanvas.fadeHighlightBox();
        if (joinButton != null) {
            joinButton.setEnabled(true);
        }
        setMessage("Failure cancels sibling subtasks", Color.pink);
    }





    private void joinScopeAction(int state) {
        if (joinButton != null) {
            joinButton.setEnabled(false);
        }
        highlightSnippet(state);
        highlightAllSubtasks();
        if (!failed.isEmpty()) {
            setMessage("join() sees failure; scope throws", Color.pink);
            return;
        }
        if (isScopeFinished()) {
            highlightSnippet(7);
            setMessage("join() returns successfully; parent thread resumes next step", Color.green);
        } else {
            setMessage("join() waiting for subtasks", Color.white);
        }
    }

    private void shutdownScopeAction(int state) {
        highlightSnippet(state);
        scopeCancelled.set(true);
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
        threadContext.setSlideLabel("            Scope", 1);
        taskCounter.set(0);
        scopeThreads.clear();
        completed.clear();
        failed.clear();
        cancelled.clear();
        scopeCancelled.set(false);
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