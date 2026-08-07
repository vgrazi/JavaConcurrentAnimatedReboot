# JavaConcurrentAnimatedReboot — Structured Concurrency Animation

## Problem Statement
Add an animation for Java Structured Concurrency to the existing
JavaConcurrentAnimatedReboot project (Swing/Spring Boot desktop app that animates
java.util.concurrent components), matching the existing slide style.

## User Choices
- Clone & modify the actual GitHub repo.
- Cover BOTH variants (allSuccessfulOrThrow + anySuccessfulResultOrThrow) with buttons
  for forking subtasks, calling join, and observing success/failure of the next step.
- Target Java 25 (StructuredTaskScope preview / JEP 505).
- Follow existing repo visual conventions. Craft HTML snippet too.

## Architecture
- Desktop Swing app on Spring Boot. Each concept = a `Slide` (com.vgrazi.jca.slides).
- `ThreadContext` manages sprites; the monolith = the shared resource/scope; a background
  thread carries sprites through thread states which drive rendering.
- Snippets are HTML files under src/main/resources/snippets with `<index style>` tokens
  that `Slide.highlightSnippet(i)` selectively colors.

## Implemented (2026-06)
- `StructuredConcurrencySlide.java` — runs the REAL Java 25 `StructuredTaskScope` on a
  dedicated scope-owner thread. Buttons: open(allSuccessfulOrThrow),
  open(anySuccessfulResultOrThrow), fork(subtask), Succeed a subtask, Fail a subtask,
  join(), Reset. Forked subtasks are virtual-thread sprites parked inside the monolith
  (the scope); on success they exit right, on failure/cancellation they retreat.
- `structured-concurrency.html` snippet (both variants + next-step + FailedException catch).
- Wired into menu (`JCAFrame`) and startup switch (`Main`, arg `structured-concurrency`).
- Build updated for Java 25 preview: `pom.xml` compiler release=25 + `--enable-preview`,
  spring-boot-maven-plugin jvmArguments=--enable-preview, Spring Boot parent bumped
  3.2.5 -> 3.5.6 (required so repackage/ASM can read Java 25 bytecode).

## Verification
- `mvn clean package` succeeds under Temurin JDK 25 (fat jar produced).
- Ran the GUI headless (Xvfb) and drove buttons via xdotool. Confirmed:
  - ALL: fork 3 -> fail one -> siblings cancelled -> join() throws FailedException, next step skipped.
  - ANY: fork 3 -> succeed one -> winner returned, losers cancelled, next step runs.
- Standalone API check confirmed join() shapes (Stream<Subtask> vs value) and FailedException.

## Backlog / Next
- P2: Add a per-subtask latency simulation so subtasks can auto-complete without manual resolve.
- P2: Add a static SpritesList image entry / README mention for the new slide.
- P2: Optionally show carrier/virtual-thread coloring (as the Virtual Threads slide does).
