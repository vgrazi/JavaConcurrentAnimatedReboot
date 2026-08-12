package com.vgrazi.jca.slides;

import com.vgrazi.jca.sprites.RunnableSprite;
import com.vgrazi.jca.sprites.RunnerThreadSprite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReactorSlide extends Slide {
    @Autowired
    private ApplicationContext applicationContext;

    @Value("${monolith-left-border}")
    private int leftBorder;
    @Value("${monolith-right-border}")
    private int rightBorder;
    @Value("${arrow-length}")
    private int arrowLength;
    @Value("${pixels-per-y-step}")
    private int pixelsPerYStep;

    private final List<RunnerThreadSprite<Boolean>> activeSprites = new ArrayList<>();
    private final AtomicInteger valueIdGenerator = new AtomicInteger(0);
    private volatile boolean running = true;

    public void run() {
        reset();

        threadContext.addButton("Flux.just()", () -> {
            highlightSnippet(1);
            createFluxJust();
        });

        threadContext.addButton("Mono.just()", () -> {
            highlightSnippet(2);
            createMonoJust();
        });

        threadContext.addButton("subscribe()", () -> {
            highlightSnippet(3);
            subscribeToFlux();
        });

        threadContext.addButton("Flux.interval()", () -> {
            highlightSnippet(4);
            createFluxInterval();
        });

        threadContext.addButton("publishOn()", () -> {
            highlightSnippet(5);
            demonstratePublishOn();
        });

        threadContext.addButton("Flux.merge()", () -> {
            highlightSnippet(6);
            demonstrateMerge();
        });

        threadContext.addButton("Flux.zip()", () -> {
            highlightSnippet(7);
            demonstrateZip();
        });

        threadContext.addButton("Done", () -> {
            running = false;
            activeSprites.forEach(sprite -> {
                sprite.setHolder(false);
                threadContext.stopThread(sprite);
            });
            activeSprites.clear();
            running = true;
        });

        threadContext.addButton("Reset", this::reset);
        threadContext.setVisible();
    }

    private void createFluxJust() {
        RunnerThreadSprite<Boolean> sprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setHolder(true);
        activeSprites.add(sprite);

        Flux.just("one", "two", "three")
            .map(String::toUpperCase)
            .doOnNext(value -> {
                sprite.setMessage(value);
                println("Emitting: " + value);
            })
            .subscribe();

        sprite.attachAndStartRunnable(() -> {
            while (sprite.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite);
            activeSprites.remove(sprite);
        }, true);
        threadContext.addSprite(sprite);
    }

    private void createMonoJust() {
        RunnerThreadSprite<Boolean> sprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setHolder(true);
        activeSprites.add(sprite);

        Mono.just("value")
            .map(String::toUpperCase)
            .doOnNext(value -> {
                sprite.setMessage(value);
                println("Mono emitting: " + value);
            })
            .subscribe();

        sprite.attachAndStartRunnable(() -> {
            while (sprite.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite);
            activeSprites.remove(sprite);
        }, true);
        threadContext.addSprite(sprite);
    }

    private void subscribeToFlux() {
        RunnerThreadSprite<Boolean> sprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setHolder(true);
        activeSprites.add(sprite);

        Flux.just("data1", "data2", "data3")
            .subscribe(
                value -> {
                    sprite.setMessage(value);
                    println("Received: " + value);
                },
                error -> {
                    sprite.setMessage("Error: " + error.getMessage());
                    println("Error: " + error.getMessage());
                },
                () -> {
                    sprite.setMessage("Complete");
                    println("Stream completed");
                    sprite.setHolder(false);
                }
            );

        sprite.attachAndStartRunnable(() -> {
            while (sprite.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite);
            activeSprites.remove(sprite);
        }, true);
        threadContext.addSprite(sprite);
    }

    private void createFluxInterval() {
        RunnerThreadSprite<Boolean> sprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setHolder(true);
        activeSprites.add(sprite);

        Flux.interval(Duration.ofMillis(100))
            .subscribeOn(Schedulers.parallel())
            .take(5)
            .doOnNext(value -> {
                sprite.setMessage("Tick: " + value);
                println("Interval tick: " + value);
            })
            .doOnComplete(() -> {
                sprite.setMessage("Complete");
                sprite.setHolder(false);
            })
            .subscribe();

        sprite.attachAndStartRunnable(() -> {
            while (sprite.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite);
            activeSprites.remove(sprite);
        }, true);
        threadContext.addSprite(sprite);
    }

    private void demonstratePublishOn() {
        RunnerThreadSprite<Boolean> sprite = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite.setXPosition(leftBorder + arrowLength);
        sprite.setHolder(true);
        activeSprites.add(sprite);

        Flux.just("process1", "process2", "process3")
            .publishOn(Schedulers.boundedElastic())
            .map(value -> {
                sprite.setMessage("Processing: " + value);
                println("Processing on elastic: " + value);
                return value.toUpperCase();
            })
            .doOnComplete(() -> {
                sprite.setMessage("Complete");
                sprite.setHolder(false);
            })
            .subscribe();

        sprite.attachAndStartRunnable(() -> {
            while (sprite.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite);
            activeSprites.remove(sprite);
        }, true);
        threadContext.addSprite(sprite);
    }

    private void demonstrateMerge() {
        RunnerThreadSprite<Boolean> sprite1 = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        RunnerThreadSprite<Boolean> sprite2 = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite1.setXPosition(leftBorder + arrowLength);
        sprite1.setHolder(true);
        sprite2.setXPosition(leftBorder + arrowLength);
        sprite2.setHolder(true);
        activeSprites.add(sprite1);
        activeSprites.add(sprite2);

        Flux<String> flux1 = Flux.just("A1", "A2", "A3")
            .doOnNext(value -> {
                sprite1.setMessage("Flux1: " + value);
                println("Flux1: " + value);
            });

        Flux<String> flux2 = Flux.just("B1", "B2", "B3")
            .doOnNext(value -> {
                sprite2.setMessage("Flux2: " + value);
                println("Flux2: " + value);
            });

        Flux.merge(flux1, flux2)
            .doOnComplete(() -> {
                sprite1.setMessage("Complete");
                sprite2.setMessage("Complete");
                sprite1.setHolder(false);
                sprite2.setHolder(false);
            })
            .subscribe();

        sprite1.attachAndStartRunnable(() -> {
            while (sprite1.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite1);
            activeSprites.remove(sprite1);
        }, true);
        threadContext.addSprite(sprite1);

        sprite2.attachAndStartRunnable(() -> {
            while (sprite2.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite2);
            activeSprites.remove(sprite2);
        }, true);
        threadContext.addSprite(sprite2);
    }

    private void demonstrateZip() {
        RunnerThreadSprite<Boolean> sprite1 = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        RunnerThreadSprite<Boolean> sprite2 = (RunnerThreadSprite<Boolean>) applicationContext.getBean("runnerThreadSprite");
        threadContext.reclaimYPosition();
        sprite1.setXPosition(leftBorder + arrowLength);
        sprite1.setHolder(true);
        sprite2.setXPosition(leftBorder + arrowLength);
        sprite2.setHolder(true);
        activeSprites.add(sprite1);
        activeSprites.add(sprite2);

        Flux<String> flux1 = Flux.just("one", "two", "three")
            .doOnNext(value -> {
                sprite1.setMessage("Flux1: " + value);
                println("Flux1: " + value);
            });

        Flux<String> flux2 = Flux.just("1", "2", "3")
            .doOnNext(value -> {
                sprite2.setMessage("Flux2: " + value);
                println("Flux2: " + value);
            });

        Flux.zip(flux1, flux2)
            .map(tuple -> tuple.getT1() + "-" + tuple.getT2())
            .doOnNext(value -> {
                sprite1.setMessage("Zipped: " + value);
                println("Zipped: " + value);
            })
            .doOnComplete(() -> {
                sprite1.setMessage("Complete");
                sprite2.setMessage("Complete");
                sprite1.setHolder(false);
                sprite2.setHolder(false);
            })
            .subscribe();

        sprite1.attachAndStartRunnable(() -> {
            while (sprite1.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite1);
            activeSprites.remove(sprite1);
        }, true);
        threadContext.addSprite(sprite1);

        sprite2.attachAndStartRunnable(() -> {
            while (sprite2.getHolder() && running) {
                Thread.yield();
            }
            threadContext.stopThread(sprite2);
            activeSprites.remove(sprite2);
        }, true);
        threadContext.addSprite(sprite2);
    }

    public void reset() {
        cleanup();
        super.reset();
        threadContext.setSlideLabel("Project Reactor");
        threadContext.setBottomLabel("Reactive Streams");
        setSnippetFile("reactor.html");
        threadCanvas.hideMonolith(false);
        activeSprites.clear();
        valueIdGenerator.set(0);
        running = true;
    }

    @Override
    public void cleanup() {
        running = false;
        activeSprites.forEach(sprite -> {
            sprite.setHolder(false);
            threadContext.stopThread(sprite);
        });
        activeSprites.clear();
        super.cleanup();
    }
}
