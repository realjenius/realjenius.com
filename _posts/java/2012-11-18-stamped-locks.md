---
title: 'JEP-155: Concurrency Enhancements for Java 8, Part 2'
summary: 'Another batch of concurrency utilities is in the pipeline for Java as JEP-155. This is a short summary of what is planned.'
category: journal
tags: jeps jep-155 concurrent java
layout: post
published: false
---

## Stamped Lock

Like the atomics, with the new concurrency libraries back in Java 1.5, we got shiny new lock classes that allow for more expressive synchronization APIs, more control, and more granularity. Lock, Condition, ReadWriteLock - all kinds of new opportunities. However, it's not one-size-fits-all here either; there is room for improvement depending on how you plan to use the lock.

Stamped locks are an interesting new construct that are designed to provide a more available form of read-write locking. One of the biggest issues that a typical read write lock has is that a writer can't do its work until all readers finish. While this won't cause writer starvation, it can cause significant contention depending on how many readers are busy, and how efficient they are.

The stamped lock introduces a third lock ownership type with intentionally weak semantics: optimistic read locks. Unlike other locks where the ownership of the lock Optimistic reads work differently than other lock retrievals by passing with a stamp, and failing "post-op" if returning the stamp shows it's stale. In this way it works much like optimistic locking writes with a database, where the fact that it failed isn't known until after the operation completed.

Practically speaking what this means for performance is that an optimistic reader thread is allowed to be run over by a writer thread in the name of prioritization. But that also means that the data-sets that the reader thread may come in contact with must still havesome* protection against multi-threaded access. While they don't need to be atomically consistent for your business rules, they must not have undefined behavior. An un-synchronized `ArrayList`, for example, has the possibility of all kinds of subtle state exceptions (like array bounds issues), and as such is not a good candidate for this sort of class. Let's take a look at sample code using this stamped lock:

{% highlight java %}
class Point {
   private double x, y;
   private final StampedLock sl = new StampedLock();

   void move(double deltaX, double deltaY) { // an exclusively locked method
     long stamp = sl.writeLock();
     try {
       x += deltaX;
       y += deltaY;
     } finally {
       sl.unlockWrite(stamp);
     }
   }

   double distanceFromOriginV1() { // A read-only method
     long stamp;
     if ((stamp = sl.tryOptimisticRead()) != 0L) { // optimistic
       double currentX = x;
       double currentY = y;
       if (sl.validate(stamp))
         return Math.sqrt(currentX currentX + currentY currentY);
     }
     stamp = sl.readLock(); // fall back to read lock
     try {
       double currentX = x;
       double currentY = y;
         return Math.sqrt(currentX currentX + currentY currentY);
     } finally {
       sl.unlockRead(stamp);
     }
   }
}
{% endhighlight %}

*Source: This is a partial segment from the provisional Javadoc for the class located here: [http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/StampedLock.java?view=markup](http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/jsr166e/StampedLock.java?view=markup)*

As you can see, the process for handling lock acquisitions involves retaining a 64-bit stamp value that is returned on the unlock call. Looking at the optimistic read scenario:



## Concurrent Hash Map Enhancements

compute if absent

## Fork Join Enhancements