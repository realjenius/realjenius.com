---
title: "Helpful Kotlin APIs for Java Developers"
date: 2023-03-18
tags: ["kotlin","java","jvm"]
---

One of the major benefits of Kotlin as a JVM language is that you can use any and all Java libraries natively. However, sometimes, Kotlin has better (or at least more idiomatic) alternatives to the native Java libraries. Over the years, I've coached a lot of Java developers at various experience levels on how to effectively use Kotlin, and it's a very common pattern for them to not know the standard Kotlin library and choose the "Java way" instead of the "Kotlin way". This article will cover some of the more common cases where I see that happen.

<!--more-->

I should note that in this article I don't plan to go over the collections API, I/O APIs, or the Time APIs in Kotlin, as each may require a more in depth overview, and also I am fully aware this is also not an exhaustive list; the standard Kotlin library is wide and deep, and has a variety of great APIs. Additionally, as evidenced by my recent blog post on the benefits of the Kotlin 1.8 [Recursive Copy/Delete File APIs]({{< relref "./recursive-copy-delete.md" >}}), the Kotlin-first APIs available to achieve tasks is an ever-growing list. 

However, if you are a well-experienced Java developer with less experience with Kotlin, this list may help you with some very common places Java developers can use Kotlin APIs that may be a little cleaner, a little more efficient, or just fit better with the rest of the Kotlin application code in a given codebase.

## Concurrency

A very common place where I see this pattern is in concurrency. There is often a handful of fundamental pieces in JVM applications (especially backend apps) that involve some form of concurrency. While [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) can certainly help build concurrent code, there are often cases where traditional Java concurrency primitives are simply required or bridge a necessary gap. Kotlin has a variety of helpers in this space that are worth knowing.

### Threads

Creating threads is a common task in the Java world, and is a bit verbose. While often teams will use something like an `ExecutorService` as a pool of threads, sometimes a single thread is the most appropriate tool.

Kotlin has a helper function that simplifies the entire process. Here are some various Java thread creation patterns in Kotlin code using the Java Thread class:

```kotlin
// 1. Basic Java version
Thread({ someAsyncWork() }).start()

// 2. Cleaner: Using Function References
Thread(::someAsyncWork).start()

// 3. Configuring name and daemon flag
Thread(::someAsyncWork, "some-async-thread").apply { 
  isDaemon = true
  start()
}
```

It's notable that for all of these, `start()` must explicitly be called. Initially, whether something can be configured via the constructor or via setters (i.e. `daemon`) is somewhat arbitrary.

Kotlin supports all of this through a single function with parameters that all have default values which can be overridden:

```kotlin
// 1. Basic Kotlin version
thread { someAsyncWork() }

// 2. Using Function References
thread(::someAsyncWork)

// 3. Configuring name and daemon flag
thread(name = "some-async-thread", isDaemon = true) { someAsyncWork() }

// 4. Create without starting
val thread = thread(start = false) { someAsyncWork() }
// ...then later:
thread.start()
```

Note that in the Kotlin case (unlike the Java case), the thread starts by default, and callers have to ask for the thread to not begin running immediately (since that is the less common scenario). There are other optional parameters as well, such as the context class loader and stack size management, if those are important for your use-case.

### Locks

In addition to threads, programs often need access to locks to ensure that critical sections are protected from concurrent execution. Typically, Java applications that work with locks are littered with try/finally patterns like so:

```kotlin
val lock = ReentrantLock()

lock.lock()
try { 
  someSynchronousWork()
} finally {
  lock.unlock()
}
```

Kotlin has added the `withLock` extension to enable doing synchronous work against the lock and ensuring it closes correctly. Further, the `withLock` function can return a value as well, making it very easy to capture a value from within the critical section: 

```kotlin
lock.withLock { 
  someSynchronousWork()
}

val someResult = lock.withLock { 
  someFunctionWithReturn()
}
```

`ReentrantReadWriteLock` has similar function extensions for read and write respectively:

```kotlin
val lock = ReentrantReadWriteLock()

val someResult = lock.read {
  someFunctionWithReturn()
}
lock.write { 
  someOtherSynchronousWork()
}
```

{{< alert "circle-info" >}}
While I plan to discuss this in a separate article at some point, those who are looking for Atomic variable support might want to consider [AtomicFu](https://github.com/Kotlin/kotlinx-atomicfu). Not only are these more flexible and more Kotlin-idiomatic than using the Java APIs directly, they also have multiplatform support, meaning they can be used in Native and JS environments.
{{< /alert >}}

### Thread Locals

Java developers working with thread locals often have to handle the `initialValue` edge-case. Prior to Java 8, `ThreadLocal` code was almost always littered with null-check-set patterns like this:

```kotlin
val threadLocal = ThreadLocal<SomeResource>()

// ...

val current = threadLocal.get()
if (current == null) { 
  threadLocal.set(SomeResource())
}
```

With Java 8, the `withInitial` helper was introduced which can often resolve this, by initializing the value via a supplier callback:

```kotlin
val threadLocal = ThreadLocal.withInitial {
  SomeResource()
}
```

However, this has a few limitations:

1. The `withInitial` factory function may be "far away" from where your application code can construct the value, especially if the value comes from database connection logic or similar
2. This value does not guarantee at compile-time that the result of `get()` will be null-free, which can break the null-type-safety benefits of the Kotlin compiler
3. The value can still in fact be null if the developer assertively assigns it to null after initialization

This latter point can be demonstrated with this code:

```kotlin
val threadLocal = ThreadLocal.withInitial {
  SomeResource()
}
val result = threadLocal.get()
println("Initial value: $result")
threadLocal.set(null)
println("Post-null-set value: ${threadLocal.get()}")
```

When run, this will print this to the console, showing that null is still possible:

```bash
Initial value: SomeResource@5caf905d
Post-null-set value: null
```

(Note: this does *not* happen if developers use `ThreadLocal::remove` to remove the value, which actually resets the thread local state)

Kotlin provides an alternative via the `getOrSet` function. This function allows for the default value to be computed at the time of calling against the thread local. Additionally, by being correctly type-parameterized, this has the added benefit that (unless your lambda returns null sometimes), the compiler can ensure this function will never return a null value:

```kotlin
fun main() {
  val threadLocal = ThreadLocal<SomeResource>()

  val result = threadLocal.getOrSet { SomeResource() }
  println("Initial value: $result")
  threadLocal.set(null)

  val result2 = threadLocal.getOrSet { SomeResource() }
  println("Post-null-set value: $result2")
}
```

Logs for this run now show that two separate instances of `SomeResource` were created:

```bash
Initial value: SomeResource@2ff4acd0
Post-null-set value: SomeResource@54bedef2
```

### Timers

One of the most common reasons for asynchronous code in applications is to schedule some work to be done later, periodically, or both. Prior to `ScheduledExecutorService`, the most common way to do this in Java was with a `Timer` and a `TimerTask`. While Kotlin has a variety of APIs here that can be used to replace the Java `Timer` counterparts, I rarely point them out or suggest them to developers. While I'll mention them here, I also plan to explain why I generally don't suggest using them.

Kotlin can enable very simple creation of scheduled/delayed tasks as well as repeating tasks using timers if desired. For example:

```kotlin
Timer().schedule(5000) { 
  after5Seconds()
}
fixedRateTimer(period = 5000) { 
  every5Seconds()
}
```

There are a variety of variants that take thread names, daemon status, support initial delays, and support fixed-delay vs fixed-rate scheduling.

#### Why No Timers

Partially due to the age of the library, the `Timer` APIs, even in Java, feel a bit strange idiomatically. The `TimerTask` class must be extended by a concrete child type which, at a minimum, implements the abstract `run` function. Additionally, the task has access on itself to both `cancel()` itself, as well as the `scheduledExecutionTime()` API to determine the most recent (present or past) execution time. Similarly, `Timer` has a variety of parameters to enable configuring the nature of the underlying thread (name, daemon behavior), as well as a variety of methods for scheduling various tasks on the timer.

Admittedly, the relation between all these types is a bit unusual:
* `TimerTask` can be reused across `Timer` instances, but that often isn't useful or the right thing to do.
* Similarly, a single `Timer` can schedule multiple `TimerTask` instances on different schedules, but that also often isn't useful or the right thing to do.
* However, because each `Timer` is an abstraction around a single `Thread`, that means that the number of timers you create is directly correlated to the number of scheduling threads you have available.
* If the `TimerTask` throws an unhandled exception, the `Timer` object becomes effectively dead and will not restart.
* Timer tasks cannot be cleanly observed or connected to `Future` and `CompletableFuture` APIs.

For all of these reasons, it's generally preferable for developers to use the much newer `ScheduledExecutorService` API. while Kotlin doesn't have specific library abstractions for executors, they have all the benefits that timers don't:

1. They take standard Java function types like `Runnable`, `Callable`, etc.
2. Integration with APIs that use `Future` is made possible
3. There is a clean abstraction between the tasks to be run and the threads being used
4. Threads in the executor do not die when the task fails (though the task will de-schedule itself from subsequent execution)

```kotlin
val executor = Executors.newSingleThreadScheduledExecutor()
// ...

// Once in 5 seconds
executor.schedule({ someTask() }, 5, TimeUnit.SECONDS)
// Every 5 seconds on the clock after 1 second delay
executor.scheduleAtFixedRate(
    { someTask() },
    1,
    5,
    TimeUnit.SECONDS
)

// A 1 second delay followed by a 5 second delay between 
// each execution
executor.scheduleWithFixedDelay(
    { someTask() },
    1,
    5,
    TimeUnit.SECONDS
)
```

Teams that want to clean this up *without* standard library support can take inspiration from this Kotlin extension function which uses the `kotlin.time` libraries for creating durations, and moves the task to run to the end of the function chain to allow for cleaner integration with the lambda syntax:

```kotlin
fun ScheduledExecutorService.schedule(
  duration: Duration,
  action: () -> Unit
) {
  this.schedule(
      action,
      duration.inWholeMilliseconds,
      TimeUnit.MILLISECONDS
  )
}

// Usage:
executor.schedule(5.seconds) { someTask() }
```

## Randoms

Within the Java world, there are a variety of ways to handle random value generation. Given that there is a tradeoff between fidelity of randomness and cost of randomness, it makes sense to have a variety of knobs and dials. There may be reasons that your random generation must be cryptographically secure. Similarly, there may be a reason for your randomness to be pre-seeded to a consistent value.

Here is a sample of the "raw Java" variants of Randoms that exist:

```kotlin
// A very basic random value generator, doubles only
val someDouble = Math.random()

// A not-thread-safe random object with a variety of APIs
val random = java.util.Random()
val someInt = random.nextInt()
val inRange = random.nextInt(0, 100)

// A seeded random
val randomSeeded = java.util.Random(123456)
val seededInt = randomSeeded.nextInt()

// Backed by cryptographically secure bit generation
val secure = java.security.SecureRandom()
val secureRandomInt = secure.nextInt()

// Since Java 7
val threadLocalInt = ThreadLocalRandom.current().nextInt()

// Since Java 8, for parallel computations and stream-like processing
val splittable = SplittableRandom()
val splitInt = splittable.nextInt()

// Since Java 17
// as well as `.of(...)` for pluggable random algorithms
val generator = RandomGenerator.getDefault()
val generatorInt = generator.nextInt()
```

Here we see a few of the various patterns of use built over time, including the most recent `RandomGenerator` abstraction, which came from [JEP 356: Enhanced Pseudo-Random Number Generators](https://openjdk.org/jeps/356), and exists to provide more pluggable random algorithms, improve integration with stream-based programming patterns, and introduce a newer more robust set of LXM algorithms. 

Within Kotlin there is a multiplatform `kotlin.random.Random` API which can be used instead, and is the only random type that the Kotlin libraries refer to directly. While this API isn't as feature-rich and scenario-complex as Java's various APIs, it exists as a common case, and can be used in a variety of contexts. Additionally, this API can translate to and from the Java counterpart:

```kotlin
val threadSafeInt = Random.nextInt()
val bounded = Random.nextInt(0 until 100)
val unsigned: UInt = Random.nextUInt()

val toJava = Random.asJavaRandom()

val fromJava = SecureRandom().asKotlinRandom()
fromJava.nextInt(100 until 200)
```

Unlike the Java variant, the Kotlin `Random.Default` type is, by default, thread-safe (in fact on the JVM it is backed by `ThreadLocalRandom` when available). Randoms can be passed in various places in Kotlin code, such as an optional parameter to shuffle an array or list, as well as to pick an item from an array or list at random:

```kotlin

val fromJava = SecureRandom().asKotlinRandom()
val collection = // ...
// Randomly sorted securely
collection.shuffled(fromJava)
// Pick an item at random
val anItem = collection.random(fromJava)
```

## System Operations

Finally, I think it's worth talking about system utilities. Some helpers that I think are often overlooked is in the `kotlin.system` package. Notably, Kotlin has lightweight helpers for timing blocks of code:

```kotlin
// Java approach - milliseconds
val start = System.currentTimeMillis()
doSomething()
val end = System.currentTimeMillis()
val duration = end - start

// Java approach - nanoseconds
val start = System.nanoTime()
doSomething()
val end = System.nanoTime()
val durationNanos = end - start

// Kotlin helper: kotlin.system.measureTimeMillis
val duration: Long = measureTimeMillis { doSomething() }
// Kotlin helper: kotlin.system.measureTimeNanos
val durationNanos: Long = measureNanoTime { doSomething() }
```

{{< alert "circle-info" >}}
I should note that, unfortunately, these callbacks do not return any value other than the timing. As a result, if you need to time functions that return values, you will need to use the Java APIs, or more likely use something like OTEL or Micrometer metrics for capturing (but not returning) timed values for aggregation.
{{< /alert >}}

When writing standalone applications it can be handy to assertively end the process. Kotlin has a function to make this easy as well:

```kotlin
// Java
System.exit(123)

// Kotlin
exitProcess(123)
```

## Summary

As mentioned, this is just a starting list of APIs and abstractions that Kotlin offers. I highly recommend Java developers spend some time [with the Kotlin Standard Library API Reference](https://kotlinlang.org/api/latest/jvm/stdlib/), simply browsing for APIs for various tasks, as there is often a better Kotlin replacement in cases where Java has a verbose or boilerplate-heavy approach.