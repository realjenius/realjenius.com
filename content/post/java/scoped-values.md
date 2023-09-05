---
title: "Test"
draft: false
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
D -- value --> E(value: *)
subgraph Internals
B
C
D
E
end
{{< /mermaid >}}

The `ThreadLocal` object itself acts as the logical key in the map. The hashcode of the key is used to find a position in the entry array, and collisions are handled by a fairly basic "try next index" methodology to look for an open spot.

Additionally, the thread local map regularly attempts to `expunge` stale entries; i.e.: those that are no longer associated with any valid value. This process is triggered in various flows, such as potential resize scenarios as well as when hash collisions occur.

One of the most notable scenarios with `ThreadLocalMap` is what happens with child threads. There is a special type of thread local called `InheritableThreadLocal` which is designed carry thread local state from parent threads to child threads. However, per the specification, once carried into a child thread, changes to the parent thread local value do *not* propagate to the child thread, and vice-versa.

This behavior, combined with the "always mutable" nature of thread locals, results in the relatively inefficient implementation of thread locals, as the child thread has no choice than to eagerly copy over the full thread local state at construction time. Consider this expected behavior:

1. Inheritable thread local in Thread `Parent` has value `"test"`
2. `Child` thread created/forked from `Parent`
3. `Child` thread has value `"test"`
4. `Parent` has thread local value re-set to `"test2"`
5. `Child` still has value `"test"`

To support this, the implementation internally looks like this:

{{< mermaid >}}
graph TB
    A(Parent) -- inheritableThreadLocals --> C(ThreadLocalMap)
    A -- creates --> B(Child)
    B -- inheritableThreadLocals --> D(ThreadLocalMap)
    C -- copied to --> D
    C -- Contains --> E(Array of Entry)
    D -- Contains --> F(Array of Entry)
    C -- Grows --> E
    D -- Grows --> F
    E -- key --> G(key: ThreadLocal<*>)
    F -- key --> H(key: ThreadLocal<*>)
    G -- value --> I(value: *)
    H -- value --> J(value: *)
    subgraph Internals
        C
        E
        G
        I
    end
    subgraph Child Internals
        D
        F
        H
        J
    end
{{< /mermaid >}}

In other words, there are two full independent copies of the values stored in two independent heap arrays in the system. Even if the values are treated fully immutably.

When thinking about virtual threads and having ten-thousand, or even one-hundred-thousand virtual threads in memory, this inheritable thread local map copying can create significant memory pressure.

### ScopedValue

In comparison to thread locals, scoped values are designed with an optimized internal model in mind. The implementation details (at least, as of Java 20) are definitely worth exploring.

With scoped values, the primary mechanisms for holding bound values are the `Carrier` and the `Snapshot`:

* The `ScopedValue<T>` object itself behaves like a map key; a unique pointer to the value as set in other references. This is also where the user-facing API typically resides, much like the `ThreadLocal` object
* `Carrier` objects are a binding of a value to a scoped value at a point in time. Carriers are modeled as a linked list (or chain) of bindings, so that when a caller says something like `.where(scope1, "xyz").where(scope2, "abc").run { ... }`, both scope1 and scope2 are in the search path for that specific set of carrier bindings. Carriers are not a global binding of values for all scoped values, however, as we'll see shortly
* `Snapshot` objects are where the carrier objects are saved. Each snapshot really represents a "tier of scoping" in the processing. Like carriers, snapshots are modeled as a chain, so as you nest scoping, snapshots will extend from each other.

All of these descriptions may be confusing, so we can try a diagram combined with code to make it a little easier to understand. Revisiting previous examples, consider this scoped value logic:

```java
// Binding 1
ScopedValue.where(A, "a1").where(B, "b2")
  .run(() -> {
    // Binding 2
    ScopedValue.where(C, "c3", () -> {
      // Binding 3
      ScopedValue.where(A, "a4").where(D, "d5")
        .run(() -> doSomething(A.get(), B.get(), C.get(), D.get()));
    });
  });
```

As a reminder, at the point of `doSomething()` being invoked, the scopes values would have these values:

```
A=a4
B=b2
C=c3
D=d5
```

Here is what this would look like in the modeled object hierarchy that is retained behind the scenes:

{{< mermaid >}}
flowchart TB
s3>Snapshot 3]
s2>Snapshot 2]
s1>Snapshot 1]
s3c1([carrier: D=d5])
s3c2([carrier: A=a4])
s2c1([carrier: C=c3])
s1c1([carrier: B=b2])
s1c2([carrier: A=a1])
s0>Empty]
s3 -- prev --> s2
s3 -- bindings --> s3c1
s3c1 -- prev --> s3c2
s2 -- prev --> s1
s2 -- bindings --> s2c1
s1 -- prev --> s0
s1 -- bindings --> s1c1
s1c1 -- prev --> s1c2
{{< /mermaid >}}

When an execution boundary completes, the snapshot (and all carriers) are "popped", simply by the thread moving back to the `prev` snapshot.

From an implementation perspective, because a `Snapshot` and the associated `Carrier` objects is an immutable data structure, a snapshot can be freely shared across thread boundaries without any risk of corruption nor any need to copy or otherwise secure values for multithreaded reasons.

With traditional thread locals, every time a new child thread is created, the inheritable values are copied to the new thread, but with scoped values it is just a pointer to an immutable snapshot. In fact, the only time in which new objects are created in this model is when scoped values are modified: changing a scoped value or adding additional scoped values using `where` results in new carriers and a new snapshot for the duration of that code block executing.

### Bonus ScopedValue Speedups

While this immutable hierarchy is a big benefit for sharing scoped binding across threads, further performance benefits can be (and are) realized. Notably: traversing the snapshot hierarchy to find values is, relatively, slow (as compared to a simple hash-table lookup).

Using the example above we can revisit how slow it could be, by following a naive traversal for the value for `B` (the first bound value), while within `Snapshot3` (the inner-most binding):

1. Check snapshot 3 carrier 1 for B - **no**
2. Check snapshot 3 carrier 2 for B - **no**
3. Check snapshot 2 carrier 1 for B - **no**
4. Check snapshot 2 carrier 2 for B - **no**
5. Check snapshot 1 carrier 1 for B - **no**
6. Finally: check snapshot 1 carrier 2 for B - **yes!**

There are two components in place to optimize this slow traversal. The first is a bitmask for all values. Here is a high-level overview of this bitmask:

* Every `ScopedValue` has a `hash`, which is generated randomly (as of Java 20, via a [Marsaglia xor shift generator](https://en.wikipedia.org/wiki/Xorshift))
* A `bitmask` is computed for any given ScopedValue, which serves as a fixed-size (though, potentially non-unique) fingerprint for the scoped value
* Every carrier, when bound, captures the bitmask of the `ScopedValue` for which it is bound. If the carrier has a previous carrier binding, the bitmask on the carrier is bitwise-or'ed with the bitmask of the previous. This additive nature makes a bitmask representing all carrier bindings
* Similarly, every snapshot has a bitmask equal to its head carrier's bitmask (which may represent several bindings), bitwise-or'ed with any prior snapshot bitmasks
* When traversing for a binding, the ScopedValue bitmask is compared to the snapshot
* If the mask is not set, the value is known to not be bound in that snapshot
* If the mask is set, the snapshot carriers are traversed, checking for a match unless/until the mask does not match
* If no carrier is found, the previous snapshot is traversed
* This process repeats until the most recent binding is found, or the mask/binding is not found

In effect, this bitmask acts as a [bloom filter](https://en.wikipedia.org/wiki/Bloom_filter), and allows for very efficient "likely" binding discovery, but can have false-positives in the case of bitmask collisions.

Here is a more concrete example of what this might look like using the example from above. I'll use a simplified bit-mask representation for the sake of this diagram, specifically with these bitmasks:

* `A = [1,0,0,1,0,0,0,0]`
* `B = [0,1,0,0,1,0,0,0]`
* `C = [0,0,1,0,0,1,0,0]`
* `D = [0,0,0,0,0,0,1,1]`

As you can see, for this simplified example, all slots are occupied and there are no collisions. The important detail to track here is that, in the case of collisions, the lookup logic will simply fall back to the slower model, however the bit space is ideally large enough and the number of in-use scoped values is ideally small enough that collisions are quite infrequent.

Here is how this bitmask organization would look in the snapshot hierarchy:

{{< mermaid >}}
flowchart TB
s3>"Snapshot 3
[1,1,1,1,1,1,1,1] (A+B+C+D)"]
s2>"Snapshot 2
[1,1,1,1,1,1,0,0] (A+B+C)"]
s1>"Snapshot 1
[1,1,0,1,1,0,0,0] (A+B)"]
s3c1(["D=d5
[0,0,0,0,0,0,1,1] (D)"])
s3c2(["A=a4
[1,0,0,1,0,0,0,0] (A)"])
s2c1(["C=c3
[0,0,1,0,0,1,0,0] (C)"])
s1c1(["B=b2
[1,1,0,1,1,0,0,0] (A+B)"])
s1c2(["A=a1
[1,0,0,1,0,0,0,0] (A)"])
s0>"Empty
[0,0,0,0,0,0,0,0]"]
s3 -- prev --> s2
s3 -- bindings --> s3c1
s3c1 -- prev --> s3c2
s2 -- prev --> s1
s2 -- bindings --> s2c1
s1 -- prev --> s0
s1 -- bindings --> s1c1
s1c1 -- prev --> s1c2
{{< /mermaid >}}

With this hierarchy it's clear to see that, while in snapshot 3, we can quickly verify that it is likely all the scoped values are set.

The other component that exists to help traversal performance even more is a lazy per-thread cache. Each "thread" carries a special `scopedValueCache` (simply an `Object[]`), which has a pre-determined, constant size. The `ScopedValue` hash is used to further facilitate the use of the cache:

* When storing a value in the cache, attempt a primary slot location or a secondary slot location, computed off of the hash
* If the primary slot is available,
* For the given hash calculate a primary slot where the scoped value might reside in the cache, and check that location
* If the value is not found, calculate a secondary slot where the value might reside


## Summary

TODO