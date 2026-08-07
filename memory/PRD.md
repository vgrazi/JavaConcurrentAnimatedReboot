# JavaConcurrentAnimatedReboot — Structured Concurrency Animation

## Problem Statement
Add an animation for Java Structured Concurrency to the existing JavaConcurrentAnimatedReboot
project (Swing/Spring Boot desktop app animating java.util.concurrent), in the existing style.
Then: create a PPT of per-button talking points + SC-vs-CompletableFuture comparison.
Follow-up: keep the original project unchanged; only add the slide (canvas/sprite tweaks OK).

## User Choices
- Clone & modify the actual GitHub repo (lives at /app/JavaConcurrentAnimatedReboot).
- Cover BOTH variants (allSuccessfulOrThrow + anySuccessfulResultOrThrow), with buttons to
  fork subtasks, call join, and see the next step on success/failure.
- Target **Java 25** structured concurrency (JEP 505) — confirmed again when choosing option 2.
- Follow existing repo visual conventions.

## Architecture
- Desktop Swing app on Spring Boot. Each concept = a `Slide` (com.vgrazi.jca.slides).
- `ThreadContext` manages sprites; the monolith = the shared resource / scope; a background
  thread carries sprites through thread states that drive rendering.
- Snippets: HTML under resources/snippets with `<index style>` tokens for highlightSnippet(i).

## Implemented
- `slides/StructuredConcurrencySlide.java` — drives the REAL Java 25 `StructuredTaskScope` on a
  dedicated scope-owner thread. Buttons: open(allSuccessfulOrThrow),
  open(anySuccessfulResultOrThrow), fork(subtask), Succeed a subtask, Fail a subtask, join(),
  Reset. Forked subtasks are virtual-thread sprites parked inside the monolith (scope); success
  exits right, failure/cancellation retreats left.
- `resources/snippets/structured-concurrency.html` — both variants + next-step + FailedException.
- `JCAFrame.java` — one menu button ("Structured Concurrency") + one @Autowired field.
- `docs/StructuredConcurrency-TalkingPoints.pptx` — 12-slide deck (per-button talking points,
  benefits over CompletableFuture, when CompletableFuture is preferred, rule of thumb).

## Build (minimal footprint — final)
Only 2 original files changed from upstream, plus new files:
- pom.xml: compiler release 25 + `--enable-preview` (compiler + spring-boot run jvmArguments);
  Spring Boot parent 3.2.5 -> 3.5.6. The parent bump is MANDATORY: with Java 25 bytecode,
  Spring's component-scanning ASM in 3.2.5 throws "Unsupported class file major version 69" at
  startup. 3.5.5+ is the first Java-25-ready line. This is the only dependency change.
- JCAFrame.java: menu registration (+4 lines).
- Main.java: reverted to original (startup still opens the intro slide).
Requires JDK 25 to build and run. `mvn clean package` produces the runnable fat jar.

## Verification (desktop GUI — testing_agent N/A for Swing/Maven, no HTTP surface)
- `mvn clean package` succeeds on Temurin JDK 25; fat jar produced; app starts clean (intro).
- Ran the jar headless (Xvfb) and drove real Swing buttons via xdotool. Confirmed:
  - ALL: fork -> fail one -> siblings cancelled (fail-fast) -> join() throws FailedException, next step skipped.
  - ANY: fork -> succeed one -> winner returned, losers cancelled, next step runs.
  - Slide reached via the menu button after the Main.java revert.
- Standalone check confirmed join() shapes (Stream<Subtask> vs value) and FailedException.

## Notes / Backlog
- Not pushed to GitHub — user should use the "Save to Github" button.
- P2: per-subtask auto-latency so children finish without manual resolve.
- P2: timeout joiner button; carrier/virtual-thread colouring like the Virtual Threads slide.
