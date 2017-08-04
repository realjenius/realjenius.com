---
title: 'JEP-171: Fence Intrisics Keep Memory In Line'
summary: 'Doug Lea has proposed to promote memory fencing mechanics into implementation APIs in Java. I cover what this means.'
tags: ["java","jeps","jep-171","concurrent"]
date: 2012-12-03
---

Doug Lea just posted a new Java enhancement proposal with [JEP-171 - Fence Intrinsics](http://openjdk.java.net/jeps/171). This enhancement is all about exposing memory fence controls into Java code so that the `java.util.concurrent` APIs can more accurately and efficiently control memory ordering and bounding.

This is an interesting JEP as it proposes no Java consumer-oriented APIs; the only changes would be on the `sun.misc.Unsafe` class - which is already a fascinating pivot point for many of the advanced concurrency features of Java. Here is a Stack Overflow article that recaps many of them better than I could: [StackOverflow.com: "Interesting Uses of sun.misc.Unsafe"](http://stackoverflow.com/questions/5574241/interesting-uses-of-sun-misc-unsafe). This class (as you can guess from the package) is *not* intended for regular Java devs to access; it's an implementation-specific class, and is really only meant for internal use by class library devs on the JDK. *(That said, many folks have taken this class by the horns to wrangle the most out of the JVM anyway.)*

What is proposed instead is to make ordering fences with memory a first-class citizen in the implementation layer of the JDK so that the various core APIs for concurrency can leverage fencing without having to resort on side-effects of other lower-level intrinsics (something that is done regularly today).

You may be asking why memory fencing is important. Modern CPUs can easily re-order memory accessing and storing when it can see via the upcoming instruction set that re-ordering will not impact the overall outcome of the program as considered by a single thread in the CPU. When you start throwing multiple threads or CPUs at a problem, out-of-order operations on memory that would otherwise go un-noticed could instead cause all kinds of data corruption and confusion. That's why effectively all of the core synchronization and atomic operations in Java today implicitly carry a memory fence along with them; it's part of the larger equation of protecting memory access.

This JEP includes three operations in the proposal:
* `Unsafe.loadFence()` - Prevent reordering of load operations before this call with loads and stores after this call.
* `Unsafe.storeFence()` - Prevent reordering of store operations before this call with loads and stores after this call.
* `Unsafe.fullFence()` - Prevent reordering of **all** memory operations before this call with loads and stores after this call.

You can read more about memory fences in the Wikipedia [Memory Barrier](http://en.wikipedia.org/wiki/Memory_barrier) article.

It's worth noting that the JEP does consider potentially surfacing memory fence operations to full devs at some point in the future given that `sun.misc.Unsafe` is already platform specific (making it risky for external libs to access), and may become *impossible* to access given the efforts of Jigsaw:

> Adding these methods at the VM level permits use by JDK libraries in support of JDK 8 features, while also opening up the possibility of later exporting the base functionality via new java.util.concurrent APIs. This may become essential to allow people developing non-JDK low-level libraries if upcoming modularity support makes these methods impossible for others to access.
