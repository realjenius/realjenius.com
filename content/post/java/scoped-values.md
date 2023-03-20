---
title: "Test"
draft: true
date: 2023-03-20
---

Partially motivated by [Virtual Threads](), a new lightweight thread-local alternative has been introduced with the incubation [JEP-429 called ScopedValues](https://openjdk.org/jeps/429). `ScopedValue` is purpose-built to provide a lighter-weight alternative to `ThreadLocal` that works well with virtual threads and also solves multiple API shortcomings. This article will dive in to how `ScopedValue` is different, and how it is made faster under the covers.

<!--more-->

## Scoped Value Re-Cap

Overall, the JEP does an excellent job of explaining the motivational benefits of `ScopedValue` over `ThreadLocal`. It can briefly be re-summarized with a few key bullet points:

* Enforce immutability and make the object lifecycle explicit in the API design. This serves to simplify API, reduce risk of errors, and also vastly expands performance optimizations available in the implementation.
* Move to a light-weight, processor-friendly implementation that does not pay the same cost of thread locals, and maximizes performance for the normal/expected use-cases, with an eye toward "virtual Thread" scenarios with many-thousands of live threads as a real possibility
* Enable inexpensive/free inheritance of values from parent threads to child threads via the use of a lightweight data sharing model, again considering the virtual thread extended use-cases

Because of the different API approach, it is explicitly stated that `ScopedValue` is not meant to replace *all* cases for which a `ThreadLocal` might be used; just those common scenarios where thread locals are used for capturing per-thread context:

> There are a few scenarios that favor thread-local variables. An example is caching objects that are expensive to create and use, such as instances of java.text.DateFormat. Notoriously, a DateFormat object is mutable, so it cannot be shared between threads without synchronization. Giving each thread its own DateFormat object, via a thread-local variable that persists for the lifetime of the thread, is often a practical approach.

## API Usage Examples

TODO thread local and scoped value

## Implementation Details

### ThreadLocal

The JEP discusses briefly the nature of the thread local implementation, but let's take a deeper dive into the internals.

Thread locals are stored directly on each thread as a `ThreadLocalMap`, which is an array-backed map. The map is very basic and meant to be specifically tuned for the use-cases with thread locals:

{{< mermaid >}}
graph LR
A(Thread) -- threadLocals --> B(ThreadLocalMap) 
B -- Contains --> C(Array of Entry)
B -- Grows --> C
C --key --> D(key: ThreadLocal<*>)
C -- value --> E(value: *)
subgraph Internals
B
C
D
E
end
{{< /mermaid >}}

The `ThreadLocal` object itself acts as the logical key in the map. The hashcode of the key is used to find a position in the entry array, and collisions are handled by a fairly basic "try next index" methodology to look for an open spot.

Additionally, the thread local map regularly attempts to `expunge` stale entries; i.e.: those that are no longer associated with any valid value. This process is triggered in various flows, such as potential resize scenarios as well as when hash collisions occur.

One of the most notable scenarios where `ThreadLocalMap` is what happens with child threads. There is a special type of thread local called `InheritableThreadLocal` which attempts to carry thread local state from parent threads to child threads. 


TODO

### ScopedValue

TODO

## Summary

TODO