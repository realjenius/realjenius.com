---
draft: true
---

With Kotlin 1.3, coroutines are now a stable feature of the language. TODO summary

<!-- more -->

Coroutines in Kotlin are a way to model asynchronous code such that:

1. It looks like synchronous blocking code to the reader of the code
1. The runtime can decide the most efficient way to execute the code (sharing the OS thread, for example)

The easiest way to see the benefits of coroutines is to see them in action. So, let's look at an example. Here is some code that does some work in a stack of functions, using callbacks to hook into the completion of the asynchronous part of child function invocations.

```kotlin
fun saveNote(response: ResponseCallback, note: Note) {
  validateNote(note)
  persistNoteAndSendEvent(note) {
    sendResponseToUser(note, response)
  }
}

fun persistNoteAndSendEvent(note: Note, callback: () -> Unit) {
  db.persist(note) {
    dispatchEvent(NoteSavedEvent(note)) {
      callback()
    }
  }

}

fun dispatchEvent(event: Event, eventCallback: () -> Unit) {
  bus.sendEvent(event) {
    eventCallback()
  }
}
```

In this case, when the functions work with "db" and "bus", those resources provide a means to invoke a callback that lets you run code after it is complete. This callback pattern is pervasive to the code. It starts at the inner most layers and works its way all the way out to the outermost layers.

Here is that same program model with coroutines in Kotlin (assuming "db" and "bus" supported coroutines, of course):

```kotlin
suspend fun saveNote(response: ResponseCallback, note: Note) {
  validateNote(note)
  persistNoteAndSendEvent(note)
  sendResponseToUser(note, response)
}

suspend fun persistNoteAndSendEvent(note: Note) {
  db.persist(note)
  dispatchEvent(NoteSavedEvent(note))
}

suspend fun dispatchEvent(event: Event) {
  bus.sendEvent(event)
}
```

As you can see, while the actual logic is asynchronous and non-blocking under the covers, to the casual reader of the code it looks like standard blocking imperative programming style. It makes it much easier to intuit how the program execution will flow, even if, in practical terms, a lot more juggling is going on internally.

# Academic and Historical Perspective

Coroutines in Kotlin are an implementation of the general concept of [coroutines in computer science](https://en.wikipedia.org/wiki/Coroutine), which are a scheme for modeling [cooperative multi-tasking](https://en.wikipedia.org/wiki/Cooperative_multitasking) within the functions of a program. Every function that is declared to be part of the coroutine participates in carrying the control state around and being prepared to pause and resume execution of different parts of the stack.

Coroutines are closely related to many other compsci concepts, including:

* Fibers
* Continuations (and more specifically call/cc)
* Green threads
* Generators and infinite lists

Coroutines have sometimes been referred to as "entering the room once and leaving it twice" (or really, it should be entering once and leaving N times), as any given suspendable function may "pause" and relinquish control to other functions elsewhere, and then get control back later to complete its own work. This is counter to how we think of programs typically working - specifically that all work will proceed down the call stack function-by-function until all of the work is complete insde the function, and then that outer function can return.

The idea of coroutine programming is not new. In fact, it is an idea as old as Lisp itself. [Scheme introduced the call/cc (call with current continuation) function](https://en.wikipedia.org/wiki/Call-with-current-continuation), which are often used as the foundation [for building coroutines](http://wiki.c2.com/?SchemeCoroutineExample).

Other more recent languages have invested in forms of this specific model:
* Ruby models coroutines via the [Fiber library](https://ruby-doc.org/core-2.1.1/Fiber.html), and over the years, has flirted with continuations as a kernel level feature, though [many problems have been found trying to use it safely](http://okmij.org/ftp/continuations/against-callcc.html) so it is now obsolete.
* Arguably, the most popular coroutine model in the wild right now is [`await/async` in ECMAScript](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function) using promises.
* The [Unity Game Platform](https://docs.unity3d.com/Manual/Coroutines.html) models coroutines as part of the built-in mono runtime to perform cooperative multitasking on the game thread.

Interestingly, all of the above examples are single-threaded programming runtimes with asynchronous concepts (traditional Ruby is single-threaded, though [JRuby](http://jruby.org) is not), and as a result they have a lot of incentive to make cooperative multi-tasking more accessible to Developers, as it's the only way to do more than one thing at a time in those languages. Clearly, given the success of [NodeJS](https://nodejs.org), cooperative multi-tasking is possible, and can scale to meet many needs.

-- TODO

* Programming does not require that developers treat units of work as scarce resources like what might be done with threads. If you want to create a million units of work to solve a parallel program it can be done, and very cheaply (where-as a million threads would OOM most JVMs).
* Writing code that waits on asynchronous work to complete does not require threading models using futures or callbacks or other "library" solutions to work being done on another thread.

However, as readers may know, the JVM does not actually support continuations at the runtime-level. There are projects exploring that very idea, including [Project Loom](http://cr.openjdk.java.net/~rpressler/loom/Loom-Proposal.html), but they are at this time just that: proposals and works-in-progress. So, how does Kotlin pull this off without the JVM? Compiler features and a library solution under the covers. Let's take a look!

# Suspension Opt-In

The starting magic trick of coroutines is the `suspend` keyword. Without this keyword, the compiler has nothing to hang its hat on. This keyword tells the Kotlin compiler that anything called from within this function is capable of also being a "suspendable" operation, and so Kotlin should build in infrastructure so that all of the code in that stack can cooperate to pause and share work.

Let's look at an example: consider this function:

```kotlin
class MyClass {
  suspend fun functionA() {
      childFunctionB();
      childFunctionC();
      childFunctionD();
  }

  suspend fun childFunctionB() {
    delay(1000);
    println("childFunctionB");
  }

  fun childFunctionC() {
      println("Test")
  }

  suspend fun childFunctionD() {
      delay(1000)
      println("childFunctionD")
  }
}
```

In this example, `delay` is a special Kotlin function that explicitly knows how to relinquish control when it is called. This means that, unlike `Thread.sleep` which just pauses, `delay` will allow the thread to go do other coroutine work while waiting.

Because our `childFunctionB` function is a `suspend` style function, when `delay` relinquishes control, `childFunctionB` knows how to as well. Finally, `functionA` is also suspendable; which means that an entire chain of `suspend` functions can all cooperate together and suspend an *entire tree of function calls* (`functionA > childFunctionB > delay`).

Note, however, that `childFunctionC` is a normal function and can't do anything coroutine-esque. It's a "black box" that must complete before the thread can be shared with other coroutines.

This stack based cooperation is the key, and likely feels similar to how exceptions bubble up when they occur, as that's what it is emulating.

So how does it work without JVM support? The secret sauce is in what Kotlin does to the underlying implementation. Specifically, if you were to look at the Java code for the above functions, it would look something like this:

```java
public Object a(Continuation continuation) {
  b();
  return c(continuation);
}

public void doSomethingElse() {
  System.out.println("Test");
}

public Object doSomethingSuspe
```
Simi
TODO - show a non magical form.

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
