---
---

With Kotlin 1.3, coroutines are coming as a stable feature of the language. TODO summary

<!-- more -->

Coroutines in Kotlin are a form of continuation programming -- in effect they serve as a combination of lightweight threads and frame suspension capabilities. This gives several benefits:

* Programming does not require that developers treat units of work as scarce resources like what might be done with threads. If you want to create a million units of work to solve a parallel program it can be done, and very cheaply (where-as a million threads would OOM most JVMs).
* Writing code that waits on asynchronous work to complete does not require threading models using futures or callbacks or other "library" solutions to work being done on another thread.

However, as readers may know, the JVM does not actually support continuations at the runtime-level. There are projects exploring that very idea, including [Project Loom](http://cr.openjdk.java.net/~rpressler/loom/Loom-Proposal.html), but they are at this time just that: proposals and works-in-progress. So, how does Kotlin pull this off without the JVM? Compiler features and a library solution under the covers. Let's take a look!

# Suspension Opt-In

The starting magic trick of coroutines is the `suspend` keyword. Without this keyword, the compiler has nothing to hang its hat on. This keyword tells the Kotlin compiler that anything within this function is capable of also being a "suspendable" operation, and so Kotlin should build in "infrastructure" so that all of the code in there can cooperate to pause and share work.

Let's look at an example: consider this function:

```kotlin
suspend fun coroutineFun() {
  doSomeWork()
  delay(1000)
  doSomeMoreWork()
}
```

In this example, `delay` is a special Kotlin function that explicitly knows how to relinquish control when it is called. Further, because our `coroutineFun` function is a `suspend` style function, when `delay` relinquishes control, it knows how to as well; this means that a chain of `suspend` functions can all cooperate together and suspend an *entire tree of function calls*.

To many UI (read: Unity) or Javascript developers this may seem obvious - this is known as [Cooperative Multitasking](https://en.wikipedia.org/wiki/Cooperative_multitasking), and is a fundamental feature of programming languages that have "multi-threading" programming models without true threading. Many languages favor a single thread with multi-threaded programming semantics because even if you only have a single thread, in most cases you get a significant percentage of the theoretical benefits of multi-threaded programming since most requests are generally waiting on responses from some other process (I/O, network, etc), which are easily known to be interruptible.

The idea of cooperative multitasking is that the language and runtime can "pause" program execution at a function, like "delay", do some work elsewhere, and then come back when "delay" is ready to proceed. This may sound a lot like threading, and that is because it is fundamentally the same idea.

The difference is a matter of approach: With true process-level threading, the OS is pausing execution of a program forcibly at the instruction level; Processing is told to stop via interruption, another thread takes over, and when the thread has available resources to proceed, the OS wakes it up. This communication and coordination is expensive, but it is also "general purpose" (meaning the OS can pause a program *anywhere*). This makes it extremely useful, with a cost. However, with cooperative multitasking, the program never involves the OS. Instead, the program coordinates with *itself* and chooses when to share compute resources with other units of work. This is the tradeoff of cooperation: the OS doesn't have to be involved if the program is ok with only stopping at "known suspend points" based on how the program is written. So let's dig into how Kotlin achieves cooperative multitasking on the JVM, even when the JVM has no such thing!

When you write a function in Kotlin that declares itself with the `suspend` keyword, that function is saying something very specific: "I may contain functionality (direct, or indirect) that can pause and choose to give up control, and I want the runtime to give me control

This "contract" is a subset of what is possible with general-purpose threading because the function also is willing to pass control back to any function calls it may have received a halt from. This conceptual model of "stack suspension" is called continuations. Imagine this example:

```plain
main
|
+-- function1
|   |
|   +--- function2
|        |
|        +--- function3
|             |
|             +--- slow-io // suspend here
|             |
|             +--- some-other-work
|
+-- function99
    |
    +-- (...)
```

When "slow-io" occurs, the work being done on "function1", "function2", and "function3" must be frozen, stored, and effectively put in a drawer so that "function99" may proceed. Then, at some point later, "function3" must wake up after "expensive-io" is done to finish the work it must do (and allow parent functions to resume). What happens here is known as




* async block
* suspend
* async start model
* Deferred return type

https://kotlinlang.org/docs/reference/coroutines.html
https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md
https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md
