---
title: Unsigned Integers with Java and Kotlin 1.3
date: 2019-02-05
tags: ["kotlin","java","unsigned"]
---

Something that has always been a bit of a limitation in the Java numeric type system is the lack of support for unsigned integers. It is not particularly common to have to work with unsigned types, but when it does happen, it's usually unpleasant in some way. Libraries like Guava have provided utilities to make it cleaner, and recent updates to Java 8 also included some unsigned helper methods.

Kotlin 1.3 has an experimental feature to make unsigned types a full citizen of the type system, while still having all of the performance of primitive integer types. Let's take a look!

<!--more-->

# Unsigned Integers and Java

To recap, in Java there are these types for integers:

* `byte` - 8-bit, Signed, Twos-complement. Supports: `[-128..127]`.
* `short` - 16-bit, Signed, Twos-complement. Supports: `[-32768..32767]`.
* `int` - 32-bit, Signed, Twos-complement. Supports: `[-2,147,483,648..2,147,483,647]`.
* `long` - 64-bit, Signed, Twos-complement. Supports: `[-9,223,372,036,854,775,808..9,223,372,036,854,775,807]`.
* `char` - 16-bit - Technically, `char` is implemented as an unsigned 16-bit value, but usage in that form is not at all recommended.

If you wished to treat an int as an unsigned value in Java, generally you could, but it required using a lot of tricks to juggle around the signed orientation of the values in Java (basically, hiding an unsigned number in a signed bucket).

For example, to encode a signed long into an unsigned integer, you can simply coerce it and accept the rollover. You can even add values to it. But, to coerce it back to a signed value (which is often necessary for things like displaying the value or working with other signed values), bitwise masking to a long data type must be used:

```java
int bigInt = (int) 4000000000L;
int added = bigInt + 1000;
System.out.println("Signed: " + added + ", Unsigned: " + (added & 0xffffffffL));
```

This prints: `Signed: -294966296, Unsigned: 4000001000`.

# Java 8's Helper Methods

With Java 8, unsigned helper methods were added to the various boxed numeric types to make treating the primitives as unsigned values. The general goal of the APIs are really to make it less cryptic when Java primitives are being used as unsigned values.

Here is an example that does some arithmetic on integers representing unsigned values, and then prints them out:

```java
int bigInt = (int) 4000000000L;
int result = bigInt + 1000;
System.out.println("bigInt + 1000: " + Integer.toUnsignedString(result));

int quotient = Integer.divideUnsigned(result, 5);
int remainder = Integer.remainderUnsigned(result, 5);
System.out.println(
        "Quotient: " + Integer.toUnsignedString(quotient) +
        ", Remainder: " + Integer.toUnsignedString(remainder)
);

int compare = Integer.compareUnsigned(bigInt, (int) 2500000000L);
System.out.println("Comparison: " + compare);
```

This will print out:

```
A + 1000: 4000001000
Quotient: 800000200, Remainder: 0
Comparison: 1
```

In the end, these static methods are just a wrapper around the unintuitive bitwise and rollover operations that are required to do unsigned operations in Java - providing more human readable context. So, while these APIs allow for writing unsigned code that is effectively as fast as signed code using existing primitives, it results in pretty unpleasant code. The helper methods permeate all use of the integers.

Further (and perhaps more problematic) the type system does not indicate that the numbers are unsigned, which makes understanding the nature of the integers in code a pure matter of convention and purely up to the author; the compiler doesn't help you. It is still very possible to mix signed and unsigned values, and when that happens, all sorts of arithmetic errors are bound to occur.

In defense of these static method additions to Java, they were intentionally conservative in design. They simply wrap simple rollover and bit-shifting logic on the existing values that people are already familiar with. Anything that works as per usual (such as addition), it's expected the developer will keep using the native language tools for that (there is no `addUnsigned(int a, int b)` for example).

While adding unsigned types to the system (like `uint` and `java.lang.UnsignedInt`) would enable type-safety and make the code far more expressive, it has two major problems:

* Adding new primitive types to Java is a huge undertaking. It impacts every level of the compiler, the runtime, the bytecode spec, the language spec, and the libraries. Further, it does imply a new keyword to the language, since primitive types are in fact keywords. As a result, it would be hard (impossible?) to justify all that work and disruption given that Java hasn't had unsigned types for years and has been as successful as it has.
* Choosing to follow the existing pattern for primitives would potentially limit options for solving the unsigned problem in more efficient and creative ways in the future as the Java platform evolves (such as with [value types](https://openjdk.java.net/jeps/169), for example). Once a new language feature and library is added, it cannot be removed easily at all, and it becomes a lot harder to modify.

So, Java as a language (at least in the near term) is left with fairly spartan, unsafe, and low-level unsigned integer support; a thin veneer over juggling bits to get around twos-complement signed values. It is totally possible to write unsigned code, but the author better understand the nature of Java's integer type system, and better be very careful about how code is organized to make it clear which numbers are signed and which aren't.

# Kotlin 1.2: No Better

Kotlin, unlike Java, chooses not to expose bitwise operators to the language, and instead to offer infix functions. As a result, unsigned coersion with Kotlin pre-Java-8 is no clearer to the reader:

```kotlin
val bigInt = 4000000000L.toInt()
val added = bigInt + 1000
println("Signed: $added, Unsigned: ${(added.toLong() and 0xFFFF_FFFF)}")
```

Lots of weird conversions to and from long values, and still bitwise modifications. Of course, with Java 8 the various static helper methods are still available, which might help.

# Kotlin 1.3: Experiment Unsigned Types

Kotlin 1.3 introduces an experimental unsigned type feature [via this proposal](https://github.com/Kotlin/KEEP/blob/master/proposals/unsigned-types.md). This feature introduces new types: `UByte`, `UShort`, `UInt`, and `ULong`. To use these without any warnings, the relevant functions or classes should be annotated with `@ExperimentalUnsignedTypes`, or the compiler flag `-Xuse-experimental=kotlin.ExperimentalUnsignedTypes` should be set.

As with all experimental features, keep in mind that the implementation and compatibility of the API is reserved to change (hence the use of experimental).

Using unsigned types is straightforward:

```kotlin
val bigInt: UInt = 4000000000U
val another: UInt = 10.toUint()
```

To protect against mis-use, the compiler now adds protections around unsigned types. For example:

```kotlin
val bigInt = 4000000000U
val withSigned = bigInt + 5 // compiler error:  Conversion of signed constants to unsigned prohibited
val withUnsigned = bigInt + 5U // OK!
```

As with their signed counterparts, these can be made nullable with the `?` modifier:

```kotlin
val anotherBigInt : UInt? = null
```

Using unsigned ints, our previous Java example becomes much, much cleaner:

```kotlin
val bigInt = 4000000000U
val result = bigInt + 1000U
println("bigInt + 1000: $result")
val quotient = result / 5U
val remainder = result % 5U
println("Quotient: $quotient, Remainder: $remainder")
val compare = bigInt.compareTo(2500000000U)
println("Comparison: $compare")
```

Perhaps the biggest benefit to a type-forward language like Kotlin: by making unsigned integer types part of the type system, methods can now make it clear that they accept or return unsigned values exclusively, and the compiler will respect this restriction, preventing signed values from being inter-mixed without explicit conversion by the developer:

```kotlin
fun readUnsignedInt(): UInt { ... }

fun writeUnsignedByte(value: UByte) { ... }
```

The other major benefit of the Kotlin implementation is that there is no significant overhead (at least, when referring to the JVM). As with `Int`, `Long`, and `Short`, when using unsigned integer types in Kotlin, they still compile down to Java primitive ints, longs, and shorts.

Of course, once nullability is introduced or they are used in APIs that only work with Object types (e.g. `List<UInt>`), a boxed type will be used instead.

# How Does it Work?

If you read [my post on Inline Classes in Kotlin]({{< ref "/post/kotlin/inline-classes.md" >}}), then the solution may already be apparent. Under the covers, all of the unsigned types in Kotlin are implemented as inline classes. In other words, roughly something like this:

```kotlin
public inline class UInt(val data: Int) : Comparable<UInt> {
  // Conversion functions to match other types, like this:
  public inline fun toLong(): Long = data.toLong() and 0xFFFF_FFFF

  // Operator Overloads for all numeric operations, including things like this:
  public inline operator fun div(other: UInt): UInt
      = (this.toLong() / other.toLong()).toUInt()
}
```

By leaning on the inline classes feature in this way, the unsigned types work exactly as expected; there is very little additional sophistication required in the various implementations to create a new primitive-esque type. All of the benefits of inline classes are available:

* Custom methods and constants can be provided (including `UInt.MAX_VALUE` and `UInt.MIN_VALUE`)
* Operators can be overloaded (as seen in use here)
* Wherever possible, all operations on the underlying primitive data type will be inlined in the resulting classfiles, resulting in zero runtime overhead.
* A boxed type is automatically generated, and will be used whenever necessary.
* As inline classes evolve and become more formalized, the unsigned support will formalize as well.

In effect, UInt codes just like an Int, which is the goal. But, it is also a type of its own, which ensures you cannot unintentionally mix paint with signed values.
