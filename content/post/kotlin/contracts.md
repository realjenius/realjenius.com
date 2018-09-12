---
title: "Contracts in Kotlin 1.3"
date: 2018-09-11
tags: ["kotlin"]
---
Kotlin 1.3 is bringing a new experimental feature to the compiler to help with improving program correctness via the type-system: **contracts**.

Contracts are a tool that allow library code (both standard library and your libraries) hint to the compiler about the constraints that are ensured on types based on the use of functions in your program. Let's see how this can be used in practice.

<!--more-->

Today in Java, there are a variety of APIs that exist to perform contractual checking at runtime. For example, many developers are familiar with Guava's `Preconditions` class, or Java 8's `Objects` class, both of which let you perform verification like this:

```java
public void someMethod(String input) {
  String[] parts = Objects.nonNull(input).split(",");
}
```

If `input` is null, then `Objects.nonNull` will fail. We know by the contract of the method that it will only return non-null objects. However, just because that method guarantees that condition, the Java compiler doesn't actually *know* that to be the case; it's a runtime condition only.

To help facilitate tracking nullity through programs, the `@NotNull` and `@Nullable` annotations were developed as part of [JSR-305](https://jcp.org/en/jsr/detail?id=305), which exists specifically to help hint to the compiler about input parameter and return value constraints regarding nulls.

_**Sidebar**: In fact the Kotlin compiler will generate JSR-305 null annotations when compiling into class files, and will also respect JSR-305 annotations in Java classes - so this aspect is already covered by Kotlin's compiler._

Kotlin contracts are much like the `JSR-305` annotations in spirit: A way for functions to express the effects they have on variables such that the compiler can make some deductions and allow or disallow certain usage of those variables later in the program.

The Kotlin compiler already has some sophistication in the form of smart-casts when using keywords like `is`. For example, in Java you often have to write something like this:

```java
public void someMethod(Object input) {
  // First check it is a string
  if(input instanceof String) {
    // Now cast as a string... feels redundant
    String inputAsString = (String) input;
    String[] parts = inputAsString.split(",");
    // ...
  }
}
```

Kotlin, on the other hand is smart enough to deduce that, within the instance check, the input *must* be a string and lets you use it as such:

```kotlin
fun someMethod(input: Any) {
  if(input is String) {
    // Kotlin lets you use input as a String directly.
    val parts = input.split(",")
  }
}
```

Kotlin contracts [are coming in 1.3 as an experimental feature](https://blog.jetbrains.com/kotlin/2018/08/kotlin-1-3-m2/) and take this idea of "smart casts" to the next level: your functions can now give these same hints about variables to the Kotlin compiler.

_**Disclaimer:** All Kotlin contracts functionality is stamped and branded as experimental. It is possible (and perhaps likely) that it will go through revisions before being released, or will never be released, so exercise caution before adopting. Hopefully this is clear since, with Kotlin 1.3M2, to use contracts you must annotate your contract-using methods with `@ExperimentalContracts`._

When defining contracts on methods, you are largely defining one of two things:

1. What is implied about a variable based on when/how/what a function returns
1. What behavior a function has on any lambdas it receives

As can be imagined, there are a variety of ways you could conceive to express these rules in a language, and there are pros and cons to each. With Java, the choice to use annotations was made with JSR-305. With Kotlin, as the [contacts KEEP proposal shows](https://github.com/Kotlin/KEEP/blob/master/proposals/kotlin-contracts.md), a variety of approaches were explored, including annotations, but for this initial release they chose to build the functionality on a series of library APIs that the compiler can detect and interpret. The APIs don't actually do anything; they exist purely for the compiler.

To define contracts, a method must start with the `contract` function, and must then define one-to-many implied contractual constraints inside the lambda.

# Return Value Contracts

Let's revisit the idea of the `notNull` method in Java, but add contracts in Kotlin:

```kotlin
@ExperimentalContracts
fun <T> notNull(value: T?) {
  contract {
    returns() implies (value != null)
  }
  if(value == null) throw NullPointerException()
}
```

Here we have a function which only returns cleanly if the value is not-null. (Note that I've intentionally avoided returning anything from this function to show that it is not required for any sort of type clarification.)

The Kotlin compiler can use this "contract" block to know that if this function completes cleanly, after that point the variable provided can safely be consiered "not null":

{{<highlight kotlin "hl_lines=3">}}
val var1: String? = // ..
notNull(var1)
var1.split(...) // no compiler error here
{{< /highlight >}}

Even though `var1` is a `String?`, the kotlin compiler knows that `notNull` will only return cleanly if the value is not null, therefore by line 3, the Kotlin compiler treats var1 as a `String` (non-null variable) instead.

_Recall that while this is actually using runtime libraries to denote the contract specification, contracts are a compiler-only piece of functionality - the package `kotlin.contracts` purely exists to make it possible to write the code the compiler reviews to refine type information. As the KEEP states:_

> contract-call is never evaluated as a Kotlin expression (be it compile-time or runtime)!
Therefore, exact implementations of classes and methods of DSL in stdlib do not matter (in fact, they are implemented as empty declarations). Their sole purpose is to provide human-readable definition of contracts, type checking, and coding assistance. Contract DSL is processed by the compiler in a special way, by extracting semantics from contract declaration

Of course, the built-in function `require` in Kotlin already provides this `notNull` feature (and already has contracts), so we don't need to build it ourselves. Instead, we can build more sophisticated and nuanced use-cases. Consider this multi-variable case:

```kotlin
@ExperimentalContracts
fun stringIntAndBool(val1: Any?, val2: Any?, val3: Any?): Boolean {
  contract {
    returns(true) implies (
        val1 != null && val1 is String &&
        val2 != null && val2 is Int &&
        val3 != null && val3 is Boolean
    )
  }
  return (val1 is String && val2 is Int && val3 is Boolean)
}
```

This time, the contract is only implied if the method returns `true` (instead of returning at all), and the resulting implication is much more complex: `val1` is a `String`, `val2` is an `Int`, and `val3` is a `Boolean` (all not null). This allows us in code to do something like this:

```kotlin
fun someFunction(firstArg: Any?, secondArg: Any?, thirdArg: Any?) {
  if(stringIntAndBool(firstArg, secondArg, thirdArg)) {
    val parts = firstArg.split(".") // call String.split
    val intRange: IntRange = secondArg.rangeTo(200) // call Int.rangeTo
    if(any3) { // use any3 as a primitive boolean
      // do something
    }
  }
}
```

As you can see, the compiler understands the contract enforced by the method over multiple variables, and treats those rules as smart-casts in subsequent code. In Kotlin 1.2.x, the above would not compile as the compiler doesn't know the function `stringIntAndBool` handles this verification; instead it would complain that you can't call `split` or `rangeTo` on an `Any?`, and you can't use an `Any` as a boolean condition.

Return value contracts may be constrained in one of three ways with the current implementation:

* `returns()` means that the contract is implied if the method returns at all.
* `returns(value: Any?)` means that the contract is implied if the method returns the specific value provided (Currently that value must be `true`, `false`, or `null`)
* `returnsNotNull()` means the contract is implied if the method returns a value that is, as the name implies, not null.

In all cases, the boolean expression provided to define the implied constraints on the variable must be *only* a combination of null conditions (`== null` and `!= null`) and type constraints (`is` and `!is`). Remember, the Kotlin *compiler* is what is interpreting these conditions, not the Kotlin runtime. Consequently, the conditions must represent constraints the compiler can enforce and refine (namely the nullity and types of variables). The scope of supported conditions may change over type (for example introducing more sophisticated dependent typing), but that is not part of this initial release.

Here is another example that shows how return type values can be used. In this example the method returns true if the given values is a String, false if it is an Int, and fails with an exception if the value is anything else:

```kotlin
fun stringOrNumber(value: Any?) : Boolean {
  contract {
    returns() implies(value != null)
    returns(true) implies(value is String)
    returns(false) implies(value is Int)
  }
  return when(value) {
    is String -> true
    is Int -> false
    else -> throw IllegalArgumentException()
  }
}
```

Here is an example of how this can be used in code:

```kotlin
val something: Any? = // ...
if(stringOrNumber(something)) {
  something.split(",")
} else {
  something.rangeTo(1234)
}
```

# Lambda Invocation contracts

The other currently supported contract in Kotlin contracts is about the usage of lambdas that are provided to a function. Today, for example, this is possible:

```kotlin
class SomeClass {
  val someValue: String

  init {
    someValue = upperCase("test")
  }
}
private fun upperCase(someString: String) : String {
  return someString.toUpperCase()
}
```

However, this is not:

```kotlin
class SomeClass {
  val someValue: String

  init {
    upperCase("test") {
      someValue = it // compiler error
    }
  }
}
private fun upperCase(someString: String, callback: (String) -> Unit) {
  callback.invoke(someString.toUpperCase())
}
```

Arguably, these are doing the same thing (though admittedly using a callback for assignment is a bit contrived). The error we receive is `Captured member values initialization is forbidden due to possible reassignment`. Put another way, the compiler is saying that it can't ensure the callback is only invoked once, therefore it cannot be sure that the `val` is not assigned multiple times by the lambda (or that perhaps it is never assigned at all!)

We can fix this by giving the compiler assurances that we will do the right thing by introducing a contract. This can be done via the `callsInPlace` contract implication:

```kotlin
@ExperimentalContracts
private fun upperCase(someString: String, callback: (String) -> Unit) {
  contract {
    callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
  }
  callback.invoke(someString.toUpperCase())
}
```

This says that the `callback` variable will be invoked by this function *exactly once*, which allows the compiler to relax and know that the `val` will be properly initialized properly because the lambda will be invoked one time.

There are four invocation kinds:

* `UNKNOWN` - this is the default assumed by the compiler
* `EXACTLY_ONCE` - this implies the lambda is always invoked once, but only once
* `AT_LEAST_ONCE` - the lambda will always be invoked, but possibly multiple times
* `AT_MOST_ONCE` - if the lambda is invoked, it will only be invoked once

This can be useful in a lot of code that might need to work with lambda functions for things like transaction management:

```kotlin
val result: String

withTransaction {
  result = db.readString("some-query-here")
}
```

Prior to 1.3.x, the above code cannot compile (instead, `withTransaction` would probably need a return type). With contracts the above can be made to work by applying a `callsInPlace` constraint. While `EXACTLY_ONCE` is the most immediately applicable, `AT_LEAST_ONCE` could also be quite useful for a `var`, where the lambda may assign the variable multiple times, but the compiler can at least be confident it will have been assigned.

Hopefully this has been a useful look into the power that contracts can provide for Kotlin programs moving forward. While this is an experimental feature and the final form will likely look somewhat different, the ability for programs to more precisely express the nature of values and types to the compiler is a key benefit of a strongly-typed language, making programs less error-prone and more self-explanatory.
