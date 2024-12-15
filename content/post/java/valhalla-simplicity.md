---
title: "Project Valhalla: The Epic Refactor to Elegance"
summary: "Project Valhalla, the road to value types in Java, is finally on the horizon, and it's time to learn more about the latest state."
tags: ["java","jvm"]
date: 2024-12-15
---
Recently, Brian Goetz spoke at Devoxx, JVMLS, and other conferences about the latest state of Project Valhalla - which is the goal to enable Java to have struct-like primitive structures (stored contiguously in memory) that can still behave (mostly) like normal java classes. The tagline has been "codes like a class, works like an int", and it's also known as "Java's Epic Refactor". I spoke about this in the past couple years, and the progress has been significant. Let's dig in.

<!-- more -->

I've been posting about "value types" in Java since 2012, [when I first talked about value types via JEP-169]({{< relref "./value-objects-in-java.md" >}}). Time marched on, and somehow twelve years have past. More recently I mentioned value types in Java when I spoke about [Kotlin Value Classes]({{< relref "../kotlin/inline-classes.md" >}}),. which was in 2021... so still three years ago (😱😵).

Most recently, [Brian Goetz](https://github.com/briangoetz) spoke at the [JVMLS Summit 2024](https://openjdk.org/projects/mlvm/jvmlangsummit/) about the [current state of Valhalla](https://www.youtube.com/watch?v=IF9l8fYfSnI). The difference from what I discussed here in 2021 until now is huge, such that it is a completely different direction that merits a totally different perspective. But, still in service of the same base problem. 

Mr. Goetz spoke about the multiple generations of "value type" research in Java, and what came out of each:

1. "Generic Specialization Types" - This is evolved from the initial goals of JEP-169 in 2012, and really comes down to "how can we get primitives into generics and not break all the things" -- inevitably this resulted in better understanding the scope of the problem, but had a variety of edge cases that were never really fully resolved, and likely included more scope than intended initially (trying to solve for `List<int>` for example).
2. [The "L-World" Approach](https://wiki.openjdk.org/display/valhalla/L-World) - This is the first "solves all the problems" mechanical solution - and the one I referred to most recently. This solution really comes down to "what language, library, specification, and runtime changes must we make to get value types into Java from top to bottom (insert wilhelm scream of falling off a cliff) - the implication of the desired changes was at a scope that might not have matched what was reasonable and understandable enough to give to all Java developers. This was what Mr. Goetz refers to as the "peak of complexity".
3. Finally, the current, language-forward Approach - This time, rather than starting at the VM level, the team started at the language level, re-applying all of the things they had learned on previous passes through the effort. This resulted in a series of ways that developers could define acceptable type restrictions in code, such that the complexity can be inferred, rather than *specified* in the language (what Mr. Goetz refers to as the "virtuous collapse")

By the end of the L-World iteration of engineering R&D, the performance numbers were intensly good -- such that orders of magnitude in gains showed up in value-based operations around matricies and things that were naturally value-friendly. At the same time, the impact was incredibly pervasive to every layer of Java - so pervasive it would have a lasting impact on the way developers coded for years to come:

* Within the runtime, you had to know about all type permutations to handle all the bytecode and constant pool variants. This makes it hard to develop a JVM that can handle all of the new permutations safely and predictably
* The specification had to be expanded to support all new value type variants - This also makes it hard to make a JVM and also makes it hard to make a compiler, tools around the runtime, and anything that cares about bytecodes or memory models or really anything that happens below the `.java` language spec
* All libraries would have to be revisited to support this new type duality everywhere - this is a problem for source and binary compatibility across the board, especially as it pertains to public APIs used by everyone like `java.lang` and `java.util` - in other words, it would be like the introduction of `IntStream` and `LongStream`, but literally everywhere
* The language had to be enhanced to support this new value-based type - this deeply impacted every compiler integration in every IDE on the surface of the developer ecosystem in a very pervasive and messy way

Some of this I illustrated in the Kotlin value types post -- the idea being that one of these new value style types could be boxed and unboxed from their `.ref` and `.val` type variants. This is something that, as a developer, you'd have to pay attention to everywhere to even hope to get the performance gains and layout you expected.

In short, the cost of all of these changes was hugh, both in implementation cost, and in complexity impact to all Java programs... even with "clever tricks". As per the most recent talk, the impact was so pervasive that it felt similar to the impact of primitives vs. objects in Java 1.0 (which any Java developer has had to deal with since day one). Even worse, this is now just passing the effort on to teams with their own types (make your choice: is this a heap object or not -- good luck!).

In short, the problem of the `L-World` approach is that it pushed the "primitive-vs-heap" problem through every layer. If you needed a primitive structure, that choice would permeate the entire stack from compilation, runtime, and even tooling.

In the end, the issue may have stemmed from starting with the goal of trying to be faithful to the machine architecture. Favoring fitting into the machine vs fitting into the existing language created a parity mismatch between the Java language and the desired goal.

After pulling back and revisiting the problem, the team boiled out two major problems that needed to be solved to enable mapping language types into their most efficient runtime representations. To get rid of reference types, the JVM has to know:

1. ...that identity of the type doesn't matter to the program
2. ...that the type and its constiuent values cannot be null

When described this way, this seems oddly simple. And, in retrospect, perhaps it is. That's the benefit of years of staring at a complex problem while collecting huge amounts of community feedback -- the best path becomes far more apparent over time.

In short, if the JVM can learn these two bits of information from the program, everything else can be figured out by the runtime when it was most appropriate, and ignored and treated the way it normally would be otherwise. This has a huge number of immediate benefits:

* No need for a deep runtime specification overhaul
* Minimal need for a languuage update
* Deep VM and runtime implementation changes, but that's the easiest place to evolve and change without hurting anything as it's meant to be a black box
* A couple new constant values associated with the class and field declarations, such that we can give it the behavior it expects
* Source and binary compatibility can be maximized such that choices to make things "value-friendly" will almost certainly continue to work on runtimes not fully value-type-ready

So: let's investigate these two big questions, see what it changes to fix them, and see what's left on the other side.

## Losing Identity

Letting go of the need of identity of objects in Java is more complex that it might seem. At first it seems easy to dismiss - but in practice, it can break a lot of things:

* The intent of `a == b` changes fundamentally
* Semantics that rely on equality and comparison (Hashmaps/Hashsets, sorting, etc) will not behave the same way
* `synchronize (x)` is no longer possible on a type that has no identity 
* `System.identityHashcode(x)` isn't useful for targeting a specific object assuming the type is value-based

For teams that have operated on a language like Scala or Kotlin this may seem relatively straightforward or perhaps inconseqential, but in the pure Java world, this may have permeated into programs in surprising ways.

That said, if it's made "opt-in" by developers, it is possible for developers to walk into true value semantics on their own. The mechanic for "opting-in" to this behavior turns out to be quite simple: a new keyword that says "I don't care about identity". Similar to what has already been done with `record` in Java, the new `value` keyword would immediately acknowledge that you lose control over certain things that come with identity:

```java
// Traditional Java type with a single int field
public class MyClass {
  private int myValue;
}

// The same - but this time as a value type:
//   -- custom equality, custom hash codes, and synchronization are compiler errors
public value class MyValueClass {
  private int myValue;
}

// Record already implies much of the constraints already, so these are largely equivalent
public record MyRecord(int myValue) {}
public value record MyRecord(int myValue) {}
```

This change unlocks a huge amount of JVM optimizations at runtime. Further, it also enables the compiler to report a variety of mis-use scenarios, and also enables the JVM to fail with an `IdentityException` in cases where a value type might be tried to be used for something like synchronization.

# Recognizing Null-ness

The other big challenge is whether or not a value can be `null`. Nullity is a unique property of "heap types" in Java - `null` means a reference that points to nothing. Since primitive values can't point to nothing, they have no `null` state.

Having to support null with a non-reference type is challenging and can break all kinds of fundamental things, as it must be encoded *into the value*. There is no object header, no pointer, no memory complexity. If you have a 64-bit value, but it can be null, you have to choose **at least** one bit to capture that null state, in effect impacting the ability to store a full uncompromised native value.

This is a messy problem, and one that it would be preferrable to avoid. In reality, it often doesn't really make sense for most value types to be null - instead it proves to be a useful coding convention for the value type to have a default representation (much like `int` defaults to `0`).

Once we eliminate this null encoding complexity, we get rid of a whole host of problems like the `64-byte-except-1-bit` scenario. That makes a variety of value-copying complexities... no longer complex.

To accomplish this approach, another language update is required that allows the developer to call out values that are not nullable. The Java architects are exploring a handful of type markers that match or mirror other language choices to explicitly indicate nullity to the runtime:

```java
// A legacy value type where the developer hasn't defined intent
String s1 = "...";
// An explicitly non-null reference. The developer 
// is saying:
//   this can't be null, and the compiler 
//   should tell me if it is
String! s2 = "...";
// An explicitly null reference. The developer
// is saying:
//   this can be null - tell me if I 
//   don't treat it that way and treat it as an error
String? s3 = "...";
```

In effect, the nullity features familiar in languages like Kotlin, Scala, and others becomes a feature of Java, and immediately they become valuable for optimization all the way down to the JVM runtime.

## Value Class Restrictions

Value types will be limited in a few ways, in that they can only extend a type that is "value-safe" -- in practice this means `java.lang.Object` or any `abstract value` class. Further, they must be abstract or final -- a value class cannot be concrete and also have subclasses that are concrete.

All of this leads to the most important detail: all instance field initialization must be done before invoking `super()`. This work requires the [flexible constructor bodies](https://openjdk.org/jeps/482) feature to support doing this preliminary assignment at the language level.

In Java today, the only way to assign instance fields is *after* invoking `super()`. This creates a "larval" object state where the object has been allocated, but the fields have not yet been assigned. This causes all sorts of problems for value types, as the underlying allocation must know how much space and what values to store, but it can't if they aren't yet assigned.

For a simple (but non-record) value class, this is probably normal looking:

```java
public value class MyClass {
  private String! name;

  public MyClass(String! str) {
    this.name = str;
  }
}
```

However, this looks a little weirder when you consider super types:

```java
public abstract value class MyAbstractClass {
  private String! name;

  public MyAbstractClass(String! str) {
    this.name = str;
  }
}

public value class MyClass {
  private String! description;

  public MyClass(String! name, String! desc) {
    // Looks upside-down to traditional Java
    this.description = dec;
    super(name);
  }
}
```

# A Wealth of Runtime Optimizations

There are a huge number of optimizations the JVM can choose to do at runtime with value types, as many more assumptions can be made up-front. Here are a few small details:

## Value Scalarization

The example in the JEP is a `Color` type that is encoded as three bytes for `RGB`:

```java
value record Color(byte red, byte green, byte blue) {
  public Color mix(Color other) {
    return new Color(
      avg(this.red, other.red),
      avg(this.green, other.green),
      avg(this.blue, other.blue)
    );

  }
}
var first = new Color(1, 2, 3);
var second = new Color(4, 5, 6);
var mixed = first.mix(second);
```

In a normal Java program this looks like **three** object constructions on the heap. But, with value scalarization the JVM can avoid creating any complex value types on the stack, and can instead optimize down to something like this:

```java
void mix(byte red1, byte green1, byte blue1, byte red2, byte green2, byte blue2) {
  // These would subsequently be stored on the stack or in a register
  var redMix = avg(red1, red2);
  var greenMix = avg(green1, green2);
  var blueMix = avg(blue1, blue2);
}
```

(Note that the initial value type JEP addresses nullable value types as well with an additional boolean flag in the method - I've removed that here for brevity since in the final form value types will likely not ever be null either).

## Heap Flattening

Heap flattening is a similar concept that allows the JVM to remove heap indirections and remove complex encoding models thanks to all of the assumptions that can be made about value types. In effect, it can allow for compressing complex value structures into their simplest primitive variants wherever possible. The JEP uses the example of a `Color[]`. A traditional on-heap array of colors would look like this in memory:

```
    0           1     [...]     99
*------------------------------------*
|   *     |    *    | [...] |   *    |
*------------------------------------*
    |           |               |
    |           |               |
    v           v               v
*--------* *--------* [...] *--------*
| Color  | | Color  |       | Color  |
*--------* *--------*       *--------*
| Header | | Header |       | Header |
*--------* *--------*       *--------*
|  byte  | |  byte  |       |  byte  |
|  byte  | |  byte  |       |  byte  |
|  byte  | |  byte  |       |  byte  |
*--------* *--------*       *--------*
```

However, the JVM can now know that a color value object, being comprised of three bytes, can fit in a single 32-bit integer type. Therefore, in effect the color object can be in memory as a plain `int[]` (using something like `r & g << 8 & b << 16`)

```
   0     1            99
*-------------------------*
| int | int | [...] | int |
*-------------------------*
```

As can probably be guessed, this opens a ton of CPU-level optimizations (like vector instructions), cache sympathy, and also removes all the heap indirections that would normally need to be traversed, and all of the additional memory allocations that would normally put pressure on the GC.

Heap flattening is equally possible for local values as well as instance/static members on the heap. Value objects used in a local method can be elided away into stack memory. But, an object in memory can also be expanded in the internal memory representation to make room for storing value types into their own object space rather than pointer indirections.

## Inference and Escape Analysis

Even if existing code does not explicitly opt-in to all aspects of value types, escape analysis and other runtime affordances in the future can still allow for finding cases where objects never use identity features and can simply be "downgraded" to value types by the JIT compiler.

## Immediate Core Library Wins

To unlock immediate benefits, several core library classes are immediately being considered for value type conversion:

> Some classes in the standard library have been designated value-based, with the understanding that they would become value classes in a future release.
> 
> Under this JEP, when preview features are enabled, the following standard library classes are considered to be value classes, despite not having been declared or compiled with the value modifier:
>
>    java.lang.Number and the 8 primitive wrapper classes used for boxing
>    java.lang.Record
>    java.util.Optional, java.util.OptionalInt, etc.
>    Most of the public classes of java.time, including java.time.LocalDate and java.time.ZonedDateTime
>
> The migration of the primitive wrapper classes should significantly reduce boxing-related overhead.

This intuitively means that code like this will (rightly) no longer require any object or heap allocations, since in the end this is just a much better model for time arithmetic:

```
val tomorrow = Instant.now().plus(Duration.ofDays(1));
```


# Summary

Over the next several releases of Java, the various parts of value types and null-restrictions will begin to arrive, with each feature getting some time to be previewed and tried by developers, and evolved as needed.

Having access to these new value type models will of course unlock huge optimization opportunities for Java programs, but will also (perhaps more importantly) unlock safer and more expressive programming models, without any anxiety about using them.

# References
* [JEP-401 - Value Classes and Objects Preview](https://openjdk.org/jeps/401)
* [JEP-482 - Flexible Constructor Bodies Second Preview](https://openjdk.org/jeps/482)
* [Null-Restricted Value Types JEP](https://openjdk.org/jeps/8316779)
* [Brian Goetz on Project Valhalla](https://youtu.be/Dhn-JgZaBWo?si=NbQunpRgpG8d75A-)
* [Early Access Java Language Specification Updates](https://cr.openjdk.org/~dlsmith/jep401/jep401-20241108/specs/value-objects-jls.html)

