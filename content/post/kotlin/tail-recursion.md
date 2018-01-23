---
title: Tail Recursion in Kotlin
date: 2018-01-22
tags: ["kotlin","java","recursion"]
---
Tail recursion can be a very elegant way to solve problems that involve a significant number of iterations but are better organized using recursion, without paying the cost you normally pay for recursion. Kotlin can enable the use of tail recursion. To understand the benefits of this, a quick recap would be of value.

<!--more-->

The name "tail recursion" comes from the use of the final call of the function (the tail) as the recursive call, and requiring no context from the surrounding function to be invoked. For example, consider this basic sum implementation using recursion (yes, I know it's silly to calculate sums using recursion but it illustrates the point without a lot of extra context). In this case we want to sum all of the numbers below and including the value provided:

```java
long sum(int n) {
  if(n < 2) {
    return n;
  } else {
    return n + sum(n-1);
  }
}
```

This implementation is straightforward, easy to read, and elegant. However, it is not strict tail recursion. For this method to work, the outer invocation of `sum` must retain its variable scope; the value of `n` must stick around so it can be added to the result of the inner invocation before it can return. In other words, because the outer sum performs transformation to the value after the inner invocation completes, it is still "pending completion", and the recursion is not the "tail". It can be easier to see this if we re-write the method to be separate steps:

```java
long sum(int n) {
  if(n < 2) {
    return 1;
  } else {
    // While this is running, this method's stack (aka context) must also be preserved
    long innerValue = sum(n-1);
    // We do more work AFTER the recursion is done
    return n + innerValue;
  }
}
```

It may not be immediately clear why this is a problem. Consider the case of wanting to calculate `sum(100000)`. In Java, this will result in a series of stack frames in memory for the methods like this:

```
+--------------------------+
| sum(1)      | n = 1      |
|           [...]          |
| sum(99997)  | n = 99997  |
| sum(99998)  | n = 99998  |
| sum(99999)  | n = 99999  |
| sum(100000) | n = 100000 |
| main(...)   |  ...       |
+--------------------------+
```

Java has a stack size limit (as do pretty much all programming languages with stack frames), and the above program will eventually fail before it ever reaches `sum(1)` with something like this: `Exception in thread "main" java.lang.StackOverflowError`. You have to add environment variables to allocate more stack memory, but that is horribly inefficent; not only will Java have to allocate a ton of memory per function call, but it has to hold onto all of it during the entire invocation. There is little incentive not to just unroll your recursion into a loop instead.

We can rewrite this solution to be strictly tail recursive:

```java
long sum(long n) {
  return doSum(n, 0);
}

private long doSum(int n, long accumulator) {
  if(n < 2) {
    return n + accumulator;
  }
  return doSum(n-1, n + accumulator);
}
```

The outer `sum` method exists to make the signature as elegant as possible, while the inner `doSum` method uses an "accumulator" value to carry the current understood value of the recursion up to this point. This accumulator format is a common pattern in tail recursion as it pushes the context into the next method call, eliminating the need to retain the stack (aka the state of the parent).

In a platform with enough wiggle room, the runtime can make the choice of saying "I can tell at this point the method needs to do nothing else with the stack, so I don't need to retain this stack anymore, and I'm going to compress it away" at which point the stack can be made to look like this no matter how deep we go:

```
+--------------------------+
| doSum(N)    | n = N      |
| sum(100000) | n = 100000 |
| main(...)   |  ...       |
+--------------------------+
```

Sadly, Java does not support tail recursion optimization in Hotpsot. The reasons mostly involve security around stack frames and are not really worth re-hashing here, but as a result the JVM cannot look at the method above and *automatically* say "I don't need to retain this stack any more" safely, so there is no such optimization (though hopefully at some point in the future Java will handle this gracefully).

That said, given the correct hints, Kotlin *does* support optimized tail recursion on the JVM. Granted, Kotlin tail recursion is a compiler trick; Kotlin chooses to unroll the recursion into a loop at the bytecode level to avoid the JVM being stuck with stacks. However, for the most part you can consider that an implementation detail; just know that as of now you have to hint to the compiler you want it to try this optimization. Let's reframe our solution using Kotlin instead. First, here is the naive solution:

```java
fun sum(n: Int): Long = if(n < 2) n.toLong() else sum(n-1) + n
```

This will fail with `StackOverflowError` just as Java would. If we look at the Java bytecode generation  we can see it generates something like this:

```java
long sum(int n) {
  if(n < 2) return (long) n;
  else return sum(n-1) + n;
}
```

However, let's reorganize it to be tail call recursive and add the `tailrec` modifier:

```java
fun sum(n: Int) = doSum(n, 0)

private tailrec fun doSum(n: Int, accumulator: Long): Long =
        if(n < 2) n+accumulator
        else doSum(n-1, n+accumulator)
```

Not only can we invoke `sum` with large values, but we can do so and it executes very quickly and without using much memory at all. Here is *roughly* what the corresponding Java code would like in this case, dissecting the bytecode:

```java
long sum(int n) {
  return doSum(n, 0);
}

private long doSum(int n, long accumulator) {
  while(n >= 2) {
    accumulator = accumulator + n;
    n = n-1;
  }
  accumulator = accumulator + n;
  return accumulator;  
}
```

The main caveat to remember is that, like inline functions, `tailrec` will eliminate methods in the call-stack. So if you were to abruptly force a termination at `sum(10)` with an exception, the stack-trace would *not* have 99,990 methods in it - instead it would look like this:

```
Exception in thread "main" java.lang.Exception: Reached "10"
	at realjenius.kotlin.TestKt.doSum(test.kt:13)
  // Note here we don't have a ton of extra stack members here
	at realjenius.kotlin.TestKt.sum(test.kt:9)
	at realjenius.kotlin.TestKt.main(test.kt:5)
```

Kotlin's Tail recursion modifier allows you to still use the elegance and power of recursion without paying the overhead of recursion you typically incur, and is well worth investigating in heavily recursive programs.
