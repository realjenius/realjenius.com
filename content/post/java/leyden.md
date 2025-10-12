---
title: "Project Leyden: A Cornucopia of Optimizations"
draft: true
---


# Understanding Startup Performance

To understand the various aspects of Java performance that are in the scope for Project Leyden, it's important to understand all the steps involved in getting a Java application running, and up to maximum performance. So let's go through those steps:

1. **Class Reading and Parsing** - The first thing that has to happen for a Java app to run is to do a bunch of file I/O to load up all the classfiles from disk that contain the binarily encoded bytecodes, and then parse them into an in-memory representation the JVM can use
2. **Class Loading** - Once classfiles have been read and parsed, they have to actually be loaded - this transforms them from their raw binary format into something the runtime can understand, including verifying the bytecode and making sure the data represents valid classes
3. **Class Linking** - Related to loading is the process of linking. Most applications have more than one class, and those classes refer to each other. To make a system run they must be linked. In Java, this is always dynamic - ClassA references ClassB, and if ClassB has never been encountered, Java will attempt to load and link to it
4. **Initial Bytecode Execution** - Once all classes are loaded and linked for the current phase of execution, the interpretation of bytecode begins. This is the first, and slowest, phase of actual program execution for Java. Prior to this, classes are simply being loaded off disk, but now we're actually running code
5. **Code Compilation Phases** - Once bytecode starts running, the JVM can transition to detecting "hotspots" (hence the name of the OpenJDK runtime), and can start to compile the Java bytecode into native machine code that is swapped in-place, ensuring as-fast-as-native execution for the hot paths

It's important to note that in most runtimes all of these steps are interleaved, and run "just in time". The JVM discovers classes as they are discovered by the running program. At any point your application can encounter a new class, which can trigger reading, parsing, loading, linking, bytecode, and compilation -- all over again.

Of course, in most cases all of this is far more predictable, usually happens quite early in the running program, and as such, there is an opportunity for major optimization - hence project Leyden.

# Class-Data Sharing: A First Iteration

For a long time now, Java has had a feature called ["class-data sharing"](https://docs.oracle.com/en/java/javase/17/vm/class-data-sharing.html). CDS has been evolving over the past several years, and as of JDK 12, has included a default archive to help with loading. CDS also supports per-application CDS archives as well, which allows you to pre-package your application for CDS along with the core libraries.

CDS, unsurprisingly, is about improving startup performance. CDS is all about the first step: reading and parsing the classfiles and turning them into a format that is ready to be loaded and linked.

Historically the name "sharing" came from creating a reusable memory-mappable footprint of the read and parsed classes so that the JVM could skip the first step altogether. Of course, with the way most applications are deployed these days, the "sharing" is less useful given that a JVM is usually not shared by multiple applications anymore, but rather packaged in a container (*cough* [Docker?](https://docker.com) *cough*) or otherwise deployed in an isolated way, where memory-mapped data is not useful (nor secure even if it was).

Libraries and frameworks that load a ton of classes ([I'm looking at you Spring](https://docs.spring.io/spring-framework/reference/integration/cds.html)) have even done the work to adopt CDS and make it easy to include into your deployment footprint to further help startup performance.

# The Next Step: AOT Class-Loading and Linking

[JEP-483](https://openjdk.org/jeps/483) describes the process of making classes faster to load into the JVM after their contents are known:

> Improve startup time by making the classes of an application instantly available, in a loaded and linked state, when the HotSpot Java Virtual Machine starts. Achieve this by monitoring the application during one run and storing the loaded and linked forms of all classes in a cache for use in subsequent runs. Lay a foundation for future improvements to both startup and warmup time.

To put this another way, JEP-483 is evolving CDS to the next level, by enabling the JVM to support not only reading and parsing classfiles into a cache, but also pre-loading and pre-linking classfiles (#2 above).

Most applications today (increasingly so) are consistent run to run - they do the same things every time they start up, and largely care about the same things once they are running. This is true most of the time until they are 100% different because they were re-built from zero. As a result, if you can pre-seed the application with a cache that cover reading, parsing, loading, *and* linking, then we are reducing a ton of unnecessary repeated overhead.

As illustrated in the contents of [JEP-483](https://openjdk.org/jeps/483), CDS (which is already in all OpenJDK builds) does make a big practical difference. This was shown by demonstrating [PetClinic](https://github.com/spring-projects/spring-petclinic) performance: CDS covers a big piece of the cost of a "big server-like" application:

> When we use this option, we can see that most of the improvement to the startup time of the HelloStream program is due to ahead-of-time loading and linking, while most of the improvement to the startup time of the PetClinic application is due to the ahead-of-time reading and parsing already done by CDS today (all times are in seconds, and percentages are cumulative):
> ```
>                                      HelloStream      PetClinic
> JDK 23 	                                0.031          4.486
> AOT cache, no loading or linking        0.027 (+13%)   3.008 (+33%)
> AOT cache, with loading and linking     0.018 (+42%)   2.604 (+42%)
> ```

JEP-483 has nothing to do with that first 33% of improvement; that is all CDS. However, if we squint at these numbers we can see that even with that big fancy Spring app we get another 9% of speedup with this JEP. Further, for the smaller application (which is when you will really actually care about Java startup performance), we see an additional 29% of an improvement based on the total duration being much lower.

That's noticeable real-time impact that will make a practical difference to Java in  

Here is an important quote that describes this initiative:

--- start
The JVM does a lot of work during the startup of a typical server application, interleaving several kinds of activities:

    It scans hundreds of JAR files on disk and reads and parses thousands of class files into memory;

    It loads the parsed class data into class objects and links them together so that classes can use each others’ APIs, which involves verifying bytecodes and resolving symbolic references, which in turn may involve instantiating lambda objects; and

    It executes the static initializers of classes — their static field initializers and static { ... } blocks — which can create many objects and even perform I/O operations such as opening log files.

If, additionally, the application uses a framework, e.g., the Spring Framework, then the framework’s startup-time discovery of @Bean, @Configuration, and related annotations will trigger yet more work.

All this work is done on demand, lazily, just in time. It is heavily optimized, however, so many Java programs start up in milliseconds. Even so, a large server application which uses a web application framework plus libraries for XML processing, database persistence, etc., may require seconds or even minutes to start up.
--- end


# Make Archiving Easier

[AOT GC Agnostic Archiving](https://openjdk.org/jeps/8326035)
* Created: 2024-02
* Updated: 2024-12

# Give Developers Tools to Define Final Types

[Stable Values (Preview)](https://openjdk.org/jeps/8312611)
* Introduce an API for stable values, which are objects that represent immutable data. Stable values are treated as constants by the JVM, enabling the same performance optimizations that are possible by marking a field final. Yet, stable values offer greater flexibility as to the timing of initialization compared to final fields. 


# Drafts
[AOT Code Compilation Cache](https://openjdk.org/jeps/8335368)
* Created: 2023-07
* Updated: 2024-08
* Enhance the AOT cache to store compiled code from training runs, reducing delays to application startup and warmup.

[AOT Compilation Support in JVM](https://openjdk.org/jeps/8313278)
* Created: 2024-06
* Updated: 2024-08
* Enhance the Java Virtual Machine with the ability to load Java applications and libraries compiled to native code for faster startup and baseline execution
  
[AOT Method Profiling](https://openjdk.org/jeps/8325147)
* Created: 2024-02
* Updated: 2024-08
* Enhance the [AOT cache](https://openjdk.org/jeps/483) to store method profiles from training runs, reducing profiling delays during application warmup


