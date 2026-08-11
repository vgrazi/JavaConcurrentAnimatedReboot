# Java Concurrent Animated — Presenter Talking Points

Each slide: what it solves, when to reach for it, the trade-off, and a *Tidbit* worth saying out loud.

---

## Titles
- A visual tour of Java concurrency — from intrinsic locks, through the j.u.c toolkit, to virtual threads and structured async.
- Watch three recurring themes on every slide: **coordination**, **contention**, and **cancellation**.
- For each tool ask the same two questions: *what problem does it solve*, and *what does it cost me in return?*
- **Tidbit:** Almost everything after `synchronized` came from one person's work — Doug Lea's JSR‑166, first shipped in Java 5 (2004). The animation is essentially a walk through 20 years of that library.

## Synchronized
- One keyword, two guarantees: **mutual exclusion** *and* **memory visibility** (the happens‑before edge on monitor enter/exit).
- The right default for a small, self‑contained critical section — a shared counter or a tiny cache only one thread should touch at once.
- Trade‑off: the lock scope is coarse and the API is rigid — hold it a moment too long and you serialize work that didn't need serializing.
- **Concrete example:** A singleton `InMemoryRateLimiter` updates `Map<String, Integer>` request counts inside a `synchronized` method so two threads cannot increment the same key at once.
- **Tidbit:** A thread **BLOCKED** waiting to enter a `synchronized` block **cannot be stopped** — `interrupt()` just sets a flag, there's no timeout and no way out until it gets the monitor. That's the exact limitation `ReentrantLock`/`ReadWriteLock` fix with interruptible and timed acquisition. (Note: `Object.wait()` *is* interruptible — it's monitor **entry** that isn't.)
- **Tidbit:** As of **JDK 24 (JEP 491)**, `synchronized` no longer *pins* a virtual thread to its carrier — the decade‑old "don't use synchronized with Loom" advice is now largely obsolete.

## Virtual Threads
- Decouple *task count* from *platform‑thread count*: millions of cheap threads, scheduled onto a small carrier pool.
- Write plain blocking code and still scale — parking/unparking a virtual thread is cheap and frees the carrier for other work.
- Ideal for thread‑per‑request servers, background jobs, and I/O‑heavy fan‑out that used to demand giant pools.
- Trade‑off: they help **throughput, not latency** — a CPU‑bound task is no faster on a virtual thread.
- **Concrete example:** A REST endpoint fans out to `customer`, `orders`, and `recommendations` services by starting three virtual threads per request and blocking on each HTTP call directly.
- **Tidbit:** Finalized in **Java 21 (JEP 444)**. Rule of thumb — **never pool them**; create one per task. And don't cache expensive objects in `ThreadLocal`, since now there are millions of threads.

## ReentrantLock
- Explicit locking with everything `synchronized` lacks: `tryLock()`, timed and **interruptible** acquisition, multiple `Condition`s, and an optional fairness policy.
- Reach for it when a thread must be able to *give up* — e.g. a UI action that fails fast instead of hanging on a busy lock.
- Trade‑off: more power, more rope — forget the `unlock()` in a `finally` and you've got a permanent lock leak.
- **Concrete example:** A desktop "Save" action uses `tryLock(100, MILLISECONDS)` on a document lock; if busy, it shows "Document is busy, try again" instead of freezing the UI.
- **Tidbit:** "Reentrant" means the holding thread can re‑acquire its own lock; each `lock()` needs a matching `unlock()`. It's **unfair by default** on purpose — barging gives much higher throughput than strict FIFO. And with JEP 491 shipped, the old "prefer ReentrantLock over synchronized for virtual threads" reason has mostly gone away.

## Semaphore
- A permit counter: cap *how many* threads run at once without serializing everything down to one.
- Perfect for throttling a shared, finite resource — DB connections, outbound API calls, concurrent uploads.
- Think in **capacity, not ownership** — no thread "holds" a semaphore.
- **Concrete example:** An email sender wraps provider calls with a `Semaphore(20)` so at most 20 sends run concurrently and API throttling limits are not exceeded.
- **Tidbit:** Unlike a lock, a permit can be released by a **different thread than the one that acquired it** — great for handoff patterns, dangerous if you double‑release. A binary semaphore (1 permit) looks like a mutex but has *no* ownership or reentrancy.

## ReadWriteLock
- Split access by intent: **many concurrent readers, or one exclusive writer**.
- Wins big when reads dominate and writes are rare — a config map or lookup table read by everyone, updated by a few.
- Trade‑off: if writes are frequent, the bookkeeping overhead can erase the benefit — plain `synchronized` may win.
- **Concrete example:** A feature-flag cache serves thousands of reads per second under `readLock()`, with a periodic refresh job taking `writeLock()` every 30 seconds.
- **Tidbit:** When a **writer is waiting, new readers are turned away** so the writer can't be starved. This wasn't always true — Java 5 let readers "tag‑team" and starve writers indefinitely; it was changed in **Java 6**. Also: you can **downgrade** write→read by acquiring the read lock before releasing the write lock, but you can **never upgrade** read→write (instant deadlock).

## StampedLock
- Adds an **optimistic read**: grab a stamp, read the fields, then *validate* the stamp — usually with no locking at all.
- Best where reads are very frequent and short and writes are rare — a hot telemetry or coordinate object.
- Trade‑off: a low‑level, easy‑to‑misuse API; you must re‑read into locals and fall back to a real read lock if validation fails.
- **Concrete example:** A moving-vehicle tracker computes nearest vehicle distance using optimistic reads of `x/y` coordinates and falls back to a read lock only when validation fails.
- **Tidbit:** Introduced in **Java 8**, and it is famously **not reentrant** and has **no `Condition`s** — re‑lock the same `StampedLock` on one thread and you deadlock yourself. It also doesn't implement the `Lock` interface at all.

## Executors
- Separate *task submission* from *thread management* — you hand over `Runnable`/`Callable`, the pool decides how to run it.
- Pick the pool from the workload, not from habit: isolate CPU‑heavy report generation from latency‑sensitive request handling.
- Trade‑off: sensible‑looking defaults can silently hide queue growth or starvation.
- **Concrete example:** A billing service runs PDF invoice generation on a dedicated fixed pool (`newFixedThreadPool(8)`) so API request threads stay responsive.
- **Tidbit:** `Executors.newFixedThreadPool()` uses an **unbounded** queue — a traffic spike grows it until you hit `OutOfMemoryError`, not backpressure. And `newCachedThreadPool()` uses a `SynchronousQueue` (zero capacity), so it will spawn threads without limit. In Java 21+, `newVirtualThreadPerTaskExecutor()` sidesteps pool sizing entirely.

## Saturation Policy
- When a bounded pool is full, the **rejection policy is your backpressure contract** — it decides what "we're overloaded" means.
- The four built‑ins: `AbortPolicy` (default, throws), `CallerRunsPolicy`, `DiscardPolicy`, `DiscardOldestPolicy`.
- `AbortPolicy`: fail fast with `RejectedExecutionException` — loud and explicit.
- `CallerRunsPolicy`: the submitting thread runs the task — natural throttling via producer slowdown.
  `CallerRunsPolicy` is the quiet hero: it makes a bursty producer run the task on its own thread, so it *slows itself down* instead of drowning the pool.
- `DiscardPolicy`: silently drop the new task — only safe for best-effort work.
- `DiscardOldestPolicy`: evict the oldest queued task, retry the new one — favors fresh work over stale.
- **Concrete example:** A telemetry pipeline uses a bounded queue + `CallerRunsPolicy` so when workers are saturated, the producer thread is forced to process events and naturally slows ingestion.
- **Tidbit:** This is where reliability under load is usually won or lost — and you can plug in your own `RejectedExecutionHandler` (e.g. shed load to a fallback, or log‑and‑drop with a metric).

## CyclicBarrier
- A rendezvous: N parties each call `await()`, all block, and all proceed **together** once the last one arrives.
- Built for phased parallel algorithms — every worker must finish phase *k* before anyone starts phase *k+1*.
- Trade‑off: fragile by design — if one party times out or is interrupted, everyone gets a `BrokenBarrierException`.
- **Concrete example:** In an image-processing pipeline, 8 workers process tile phase A, meet at a barrier, then all start phase B edge blending together.
- **Tidbit:** It's "cyclic" because it **re‑arms automatically** after each release (that's the difference from a latch). Bonus feature: an optional **barrier action** runs once, on the last‑arriving thread, before the others are freed — handy for merging each phase's results.

## CountDownLatch
- One‑way gate: threads `await()` until the count is counted down to zero, then all pass.
- Ideal for start‑up coordination or "wait for N independent things to finish" fan‑in.
- Trade‑off: **single‑use** — once it hits zero it stays open forever; there's no reset.
- **Concrete example:** Service startup waits on a `CountDownLatch(3)` until `cacheWarmup`, `schemaValidation`, and `keyLoad` each finish before opening the HTTP port.
- **Tidbit:** Two classic shapes from one primitive — a **start signal** (latch of 1, everyone waits on it) and a **done signal** (latch of N, one waiter). Need a reusable version? That's exactly when you switch to `CyclicBarrier` or `Phaser`.

## Phaser

- `Phaser` generalizes barrier/latch with dynamic registration.
- Good for variable participant counts and multi-phase workflows.
- For a crawl or batch pipeline, it lets tasks join late and leave after a phase completes.
- Flexible but more complex - phase lifecycle discipline matters.
- **Concrete example:** A web crawler registers a new party for each discovered domain worker, advances phase after each crawl depth, and deregisters workers as domains complete.

## BlockingQueue
- The producer–consumer backbone: `put()` blocks when full, `take()` blocks when empty — backpressure for free, no manual `wait/notify`.
- Drop it between pipeline stages — collector→parser, reader→image‑processor — to decouple their speeds.
- Trade‑off: the *choice* of queue is the design — bounded vs unbounded, array vs linked — and it dictates latency and memory.
- **Concrete example:** Log collectors put raw lines into a bounded `ArrayBlockingQueue`; parser workers `take()` and enrich them before writing to Elasticsearch.
- **Tidbit:** `SynchronousQueue` has **zero capacity** — every `put()` waits for a matching `take()` (it's the engine inside `newCachedThreadPool`). And `LinkedBlockingQueue`'s default capacity is `Integer.MAX_VALUE`, i.e. effectively unbounded — the same footgun as the fixed thread pool.

## TransferQueue
- Lets a producer **hand a task directly to a waiting consumer** and, with `transfer()`, block until it's actually received.
- Reach for it in low‑latency dispatch: a scheduler wants confirmation that a worker *took* the job, not just that it was queued.
- Trade‑off: richer semantics than a plain queue — use it only when that handoff guarantee is something you actually need.
- **Concrete example:** A market-data dispatcher uses `transfer()` so the feed thread only advances after a consumer thread has actually accepted each quote update.
- **Tidbit:** `LinkedTransferQueue` (**Java 7**) is a superset of `SynchronousQueue` — `transfer()` is a synchronous handoff, while `put()` behaves like an ordinary unbounded queue, so you can mix both styles on one queue.

## CompletableFuture
- A composable async graph — `thenApply`/`thenCompose`/`thenCombine` to transform and join, `anyOf`/`allOf` to race or gather, `exceptionally`/`handle` to recover.
- Express *dependencies declaratively* — fetch profile, permissions, and recommendations in parallel, then combine — instead of choreographing threads.
- Trade‑off: watch **which executor** runs your callbacks and how exceptions flow.
- **Concrete example:** Product page rendering runs `priceFuture`, `inventoryFuture`, and `reviewsFuture` concurrently, then `thenCombine` merges results into one response DTO.
- **Tidbit:** `*Async` methods with no executor run on `ForkJoinPool.commonPool()` — **but** if the machine reports only one CPU, the common pool has parallelism 0 and each task quietly gets its **own new thread**. Also, exceptions are wrapped in `CompletionException`, so `join()` and `get()` report failures differently.

## Structured Concurrency
- Fork subtasks inside a `StructuredTaskScope`; the scope *owns* them and can't be closed until they all finish — concurrency that mirrors your code's block structure.
- `allSuccessfulOrThrow` waits for every child and fails fast; `anySuccessfulResultOrThrow` takes the first success and cancels the losers.
- Trade‑off: still a **preview** API (JEP 505, Java 25) and deliberately scoped — it's an orchestration tool, not a replacement for a long‑lived `CompletableFuture`.
- **Concrete example:** `checkout()` forks `reserveInventory()`, `authorizePayment()`, and `quoteShipping()` inside one scope and returns only when all succeed; if payment fails, the other two are cancelled automatically.
- **Tidbit:** It turns a leaked‑thread problem into a compile‑and‑runtime guarantee: when the `try`‑block exits, there are provably **no children still running** — no orphans, no swallowed exceptions. (See the dedicated deck for the button‑by‑button walkthrough.)
- `scope.close()` cancels running forked subtasks (they are interrupted/cancelled), and `close()` waits for them to finish.
- If the owner thread forked tasks but did not call `join()` first, `close()` throws `java.lang.IllegalStateException: Owner did not join after forking`.
- If `close()` is called from a different thread than the owner, it throws `java.lang.WrongThreadException: Current thread not owner`.
- For running tasks specifically: blocking tasks (`sleep`, waits, interruptible blocking I/O) typically exit via `InterruptedException`.
- Non-blocking spin loops only stop if they explicitly check interruption/cancellation conditions.

## CompletionService

- Submit many tasks, consume results as they complete.
- Ideal when completion order matters more than submission order.
- It works well when querying several external endpoints and processing each response as soon as it arrives.
- Simple pattern for fan-out/fan-in without managing per-future polling.
- **Concrete example:** A search endpoint submits queries to 10 shards and streams top matches back to the client in completion order rather than shard order.

## AtomicInteger

- Lock-free atomic updates for shared counters/flags.
- Use CAS-based primitives for simple shared-state hotspots.
- A request counter, retry counter, or live gauge updated by many threads is a natural fit.
- Atomicity is per variable - multi-field invariants still need stronger coordination.
- **Concrete example:** A retry loop increments an `AtomicInteger` attempt counter from multiple worker threads and trips a circuit breaker once attempts exceed a threshold.

## Credits

- Concurrency is about controlled coordination, not just speed.
- Prefer simplest correct primitive first; optimize once contention is measured.
- Design for cancellation, timeouts, and overload from day one.
