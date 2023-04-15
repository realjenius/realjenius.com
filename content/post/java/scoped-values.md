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

All of these descriptions may be confusing, so we can try a diagram combined with code to make it a little easier to understand. Consider this scoped value logic:

```java
TODO
```

Here is what this would look like in the modeled object hierarchy that is retained behind the scenes:

{{< mermaid >}}
graph BT
A(Snapshot2) -- prev --> B(Snapshot1)
B(Snapshot1) -- prev --> C(Empty)
B -- bindings --> F(carrier)
A -- bindings --> D(carrier)
F -- prev --> G
{{< /mermaid >}}

When an execution boundary completes, the snapshot (and all carriers) are popped, and whatever snapshot existed before becomes the "current" snapshot in play (which may be "empty"). Because each snapshot and "carrier chain" is immutable, it may now be apparent how this results in a much more efficient reuse across inherited threads:

* With traditional thread locals, every time a new child thread is created, the inheritable values are copied to the new thread
* With scoped values, this isn't the case. In fact, the only time in which new objects are created in this model is when scoped values are modified: changing a scoped value or adding additional scoped values using `where` results in new carriers and a new snapshot being created for the duration of that code execution

### Bonus ScopedValue Speedups

There are some other interesting efficiencies built-in to the scoped value implementation that are worth discussing.

* Snapshot
* Carrier
* Bindings
* Bitmask
* Cache
* 


## Summary

TODO