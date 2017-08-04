---
title: 'JEP-155: New Concurrency Utils Part 1 - Atomically Delicious'
summary: 'Another batch of concurrency utilities is in the pipeline for Java as JEP-155. This is part one of a short summary of what is planned.'
tags: ["jeps","jep-155","concurrent","java"]
date: 2012-11-18
---

Concurrency geeks rejoice! Doug Lea and the JSR166 exper group are a perpetual fountain of classes, constructs, and frameworks for multi-threading, and they are at it once again; working to funnel more amazing roflscale concurrency primitives and not-so-primitives into Java 8 - this time in the form of [JEP 155: Concurrency Updates](http://openjdk.java.net/jeps/155).

There are a number of enhancements being proposed in this JEP, so I'll probably tackle it in a few blog posts. For today, we'll talk numbers!

## Atomic-ish Numbers

It's very very common to use atomic values in metrics gathering capacities in Java application. Applications everywhere (if they know what is good for them) are doing things like tracking total execution time, # of executions, and max execution time for as many data points as they can stomach.

Compared to the "olden days" of Java, atomics (particularly `java.util.concurrent.atomic.AtomicLong`) are a huge boon for this as they get rid of the overhead caused by contention when you're dealing with synchronization. A particular code-flow will be much more concurrent when updating an atomic value than trying to fence through a synchronization boundary in heavy traffic.

However, all is not well in atomic land. While they are much better than everything that has come before them, they can  still be quite disruptive. Updating an atomic value is, by definition, still fully shared between threads. While it's a narrow operation, the guarantee is that after the operation is done, all CPU active on the system will see the same value in that memory location. To do this, the CPU core doing the work has to ensure that the value of that atomic is pushed back out to main memory; not just retained in the CPU cache. And in so doing, it has to keep trying to update the core memory until the incremental is what is expected (via a CAS - [compare and swap](http://en.wikipedia.org/wiki/Compare-and-swap) operation). This CAS and memory fence ensures that staleness does not happen due to the CPU keeping data in local cache only, and not dumping out to slow system memory constantly.

However, this inherently means that, for this value, we *are* writing out to slow system memory constantly. So it's not free. Additionally, what often can happen is that the atomic changing invalidates a line of cache shared by multiple values. This is fairly common because the contiguous cache blocks the CPU is designed to invalidate are generally large enough to hold multiple numeric values. This means that the CPU is forced to (unintentionally) re-fence multiple atomics; often atomics that are allocated together for the same data structure.

This lack of cache coherence and affinity can be a very real, and very hidden cost with all fence operations. Martin Thompson and Michael Barker (the wizards behind the much discussed [LMAX Architecture](http://martinfowler.com/articles/lmax.html) and the [disruptor stream processing framework](http://lmax-exchange.github.com/disruptor/)) have a talk that covers this problem (and many others) in detail titled [Lock-Free Algorithms](http://www.infoq.com/presentations/Lock-free-Algorithms#HN2).

Thompson and Barker show exactly how much this cache issue can impact performance, and how with a little adjusting, the atomic classes can be designed with what they call "mechanical sympathy" in mind; in other words, built to avoid sharing cache regions with other atomic values. So instead of this:

```
cache line 1 . . . . <atomic int val A> . . . . . <atomic int val B> . . .
cache line 2 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
```

You ideally would get this:

```
cache line 1 . . . . <atomic int val A> . . . . . . . . . . . . . . . . .
cache line 2 . . <atomic int val B> . . . . . . . . . . . . . . . . . . .
```

Now the two values are isolated from each other, reducing cache damaging significantly.

While I'm listing interesting related reads, another article just popped up in the past couple days that shows some very real metrics for the problems you can hit with Atomics in terms of program throughput. Marc Brooker [just posted how atomics and volatiles in Java work on x86](http://brooker.co.za/blog/2012/11/13/increment.html), and it shows exactly what a lack of mechanical sympathy can mean to your cache hit performance. The `perf stat` output he shows with the significant increase of `LLC-load-misses` is exactly what this padded cache is meant to mitigate as much as possible - of course, it can only do so much; the entire idea of an atomic implies there is some shared memory involvement, which is simply going to be slower. The goal is to narrow that slowness to specific values, as much as possible.

So mechanical sympathy changes help, but only get you so far. The work in JEP-155 doesn't stop by exploring this idea. There is another issue with atomics as it pertains to being used for metrics-like monitoring in large scale Java systems. Their atomic requirement may actually be too stringent. Often times when you're looking at something like an "average" execution time or "max" execution time, it's acceptable to say the value can be eventually consistent across multiple updates. If there is a little margin for error (like you might miss one recent update on a read), it's not going to impact your analysis in any real way.

Knowing this, the JSR166 folks have taken one of the ideas that are common in highly concurrent programming, and applied them to these types: striping, or segmenting.

If you've ever looked into the internals of the `ConcurrentHashMap` class, you may be familiar with the constructs it uses to minimize contention and avoid locking. Internally, the CHM uses a series of segments which represent sub-sections of the map, and can be worked with independently. By using multiple segments, the data structure is able to immediately divide the potential contention, at the expense of a little more memory used.

*(Incidentally, the fastest JDBC connection pool I've ever benchmarked at scale uses this same mechanic to avoid pool contention - see [BoneCP](https://github.com/wwadge/bonecp).)*

This same idea is in place for a number of new atomic classes. There is an abstract `Striped64` super-class that holds a collection of `Cell` objects, which are really just Atomics with special padding to help with mechanical sympathy. The cells are created lazily as CAS updates fail due to contention. What this means is that a single instance of this may have several internal atomics representing the value in the aggregate. Since this class doesn't just sit on the CAS wall until it updates successfully, it doesn't have the same repeated crawling down into shared memory from the CPU cache. Instead it will retry a set number of times, and if it can't get the first cell to update, it creates another one. The next one is a fully separate atomic in memory, and is bound by its own memory fencing and contention.

What's interesting about this is since the atomics are intentionally written to avoid cache collisions, each one can be independently represented on a CPU, and since the class will try to balance the cells to individual threads, it will avoid causing the painful contentious updates altogether, making the most valuable use of the atomics. Each thread can say "If I created a new cell because a previous cell was contentious, I'll keep using that new cell" - this means that CAS collisions can theoretically be eliminated over time, with the expense of potentially creating a larger memory footprint for a single number.

Since the cells are all independently update-able and create-able, and based on multiple iterative passes, the process of calculating a value over them involves iterating each cell and getting a snapshot value from them independently. Because of that, it's possible that during the iteration, some of the cells may be slightly out of date or non existent on my local thread.

Additionally, since this is all based on combining the values out of multiple cells together, it immediately implies that there are only certain accumulative operations where this segmentation can be used.

There are several new classes that extend `Striped64` to provide relaxed-consistency classes for cases where you want to accumulate values in this way:
* `LongAdder`; `DoubleAdder` - Classes that allow threads to accumulate values into a long or double in a thread-safe, and extremely low-contention path. **Example Usage:** Accumulating total execution time for a particular call so you can calculate average execution time.
* `LongMaxUpdater`; `DoubleMaxUpdater` - Classes that allow threads to calculate the "max" value seen up to a point, again in a very low-contention implementation. **Example Usage:** Tracking the max execution time for a particular method call.
* `LongAdderTable` - A handy extrapolation of long adder into a hash table form. This is an optimized map of LongAdders (although it doesn't implement the Map interface for a number of reasons) that is specifically designed to be as efficient as possible in the use of long adder values per key, and allows for decrementing, incrementing, and adding by key. It also will create the adders for you automatically simply as an operation of you incrementing. **Example Usage:** Tracking several call execution times for an application all in one data structure.

Let's look at how these classes work internally to bring home the idea of cells for contention management. If we look at the `LongAdder` class as an example - let's say we have a simple counter value we're tracking for some method call, and it looks like this at one point:

```
LongAdder
|
*-- Cell[0] = 51352
```

Our thread comes in to increment the first cell and we keep getting a CAS collision. So, based on the collision heuristics, we decide to create a new cell and increment there:

```
LongAdder
|
*-- Cell[0] = 51494
|
*-- Cell[1] = 1
```

Note that Cell\[0\] has incremented several times outside of our control (other threads), and we also created Cell\[1\] to hold our new value.

To calculate the sum total for this long adder, we simply sum all of the cells together, returning `51495`.

*The internal documentation on the [Striped64](http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/Striped64.java?view=markup) draft class has phenomenal notes on the algorithm)*.

While discussing atomic numbers, it should also be noted that there are implementations of `AtomicDouble` and `AtomicDoubleArray` laid out in the extras package of this draft work as well. These would ideally bring true atomic floating point to Java for the first time, but as indicated on the draft group site, the extras folder is probably not going to make it into Java 8 directly. They do show how these can be implemented, however, which is a feat in and of itself.

## What's Next?

There's a good bit more in the JEP-155 bundle, including stamped locks and some nifty fork join improvements. Those are for another time, however.
