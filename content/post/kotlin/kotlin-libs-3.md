---
title: 'Kotlin Libraries: Concurrency'
date: 2017-08-28
series: kotlin-libraries
tags: ["kotlin","java","kotlin-libraries"]
---

Today's Kotlin library article is about the `kotlin.concurrent` package, and everything that adds to the platform.

Java's concurrency package is already quite sophisticated, and rather than re-invent so many extremely delicate abstractions, the Kotlin authors focused on making the libraries better suited to the language by decorating and shortening various features.

<!--more-->

{{< series >}}

# Thread Local additions

Thread local gets a new "get-or-set" defaulting function. This allows for an arbitrarily complex block of code to be used to default the thread local.

This is complementary to the ``
```java
fun main(args: Array<String>) {
    val tlocal = ThreadLocal<String>()
    println(tlocal.getOrSet(::loadLocal)) // prints "initial-value"
    tlocal.set("updated-value")
    println(tlocal.getOrSet(::loadLocal)) // prints "updated-value"
}

// Could be anything
fun loadLocal() = "initial-value"
```

# Locks

Next on the hit list is lock management. In Java it's common to write this:

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
  threadUnsafe();
} finally {
  lock.unlock();
}
```

This of course opens the opportunity to forget to call unlock or to write this boilerplate pattern in the wrong way. For example, you can write it this way which is not handled well by some counting lock implementations if the lock acquisition fails:

```java
try {
  lock.lock(); // might fail!
  threadUnsafe();
} finally {
  lock.unlock();
}
```

Kotlin adds a `withLock` function for this purpose:

```java
fun main(args: Array<String>) {
    val lock = ReentrantLock()
    lock.withLock {
        threadUnsafe()
    }
}
```

Two immediate benefits:

1. This is added to `Lock` interface, which means all lock implementations can take advantage of it
1. It is an inline function so you don't pay for any additional overhead from the standard Java implementation.

They have done this similarly for `ReentrantReadWriteLock` readers and writers:

```java
  val lock = ReentrantReadWriteLock()

  lock.read {
      readerFunction()
  }

  lock.write {
      writerFunction()
  }
```

Again, these are inlined so you don't actually pay any additional dispatch cost for the cleanliness.

# Quick and Dirty Threading

In Java if you just want to spin off a singular thread to do some work, it probably looks like this:

```java
Thread t = new Thread(() -> doSomeWork(), "worker-thread");
t.start();
```

Kotlin has a convenience function to create a thread that, by default, automactically starts the thread (and has named parameters for a variety of other fields, including the name):

```java
val t = thread(name="worker-thread") = doSomeWork()
// already running here.
```

# Timers

Similarly, they have factory functions for timers, as well. They predominantly exist to cope with the fact `TimerTask` is an abstract class, and as such cannot be traditionally converted into a lambda.

There are a series of global factory functions that auto schedule the timer:

```java
val t1 = timer(name = "timer-1", initialDelay = 0, period = 1000) { println("fixed delay after initial delay") }
val t2 = timer(name = "timer-2", startAt = Date.from(Instant.now()), period = 1000) { println("fixed delay after initial start time") }
val t3 = fixedRateTimer(name = "fixed-rate-1", initialDelay = 0, period = 1000) { println("fixed rate after initial delay") }
val t4 = fixedRateTimer(name ="fixed-rate-2", startAt = Date.from(Instant.now()), period = 1000) { println("fixed rate after initial delay") }
```

Finally, there are also extension methods added to the timer to schedule with lambdas as well:

```java
val t5 = Timer("timer-3")
t5.schedule(delay = 1000) { println("run once after delay") }
val t6 = Timer("timer-4")
t6.schedule(delay = 1000, period = 1000) { println("fixed delay after initial delay") }
val t7 = Timer("timer-5")
t7.schedule(time = Date.from(Instant.now()), period = 1000) { println("fixed delay after initial start time") }
val t8 = Timer("fixed-rate-3")
t8.scheduleAtFixedRate(delay = 0, period = 1000) { println("fixed rate after initial delay") }
val t9 = Timer("fixed-rate-4")
t9.scheduleAtFixedRate(time = Date.from(Instant.now()), period = 1000) { println("fixed rate after initial start time") }
```
