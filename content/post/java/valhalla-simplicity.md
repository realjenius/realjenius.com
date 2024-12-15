---
Recently, Brian Goetz spoke at Devoxx about the latest state of Project Valhalla - which is the goal to enable Java to have struct-like primitive structures (stored contiguously in memory) while behaving (mostly) like normal java classes. The tagline has been "codes like a class, works like an int", and it's also known as "Java's Epic Refactor". I spoke about this in the past couple years, and the progress has been significant. Let's dig in.

<!-- more -->

I've been talking about "value types" in Java since 2012, [when I first talked about value types via JEP-169]({{ relref "./value-objects-in-java.md" }}). Since then, as time generally enforces, twelve years have past. More recently I mentioned value types in Java when I spoke about [Kotlin Value Classes]({{ relref "../kotlin/inline-classes.md" }}),. which was in 2021 (`:mildpanic:`).

Most recently, [Brian Goetz](https://github.com/briangoetz) spoke at the [JVMLS Summit 2024](https://openjdk.org/projects/mlvm/jvmlangsummit/) about the [current state of Valhalla](https://www.youtube.com/watch?v=IF9l8fYfSnI). The difference from what I discussed here in 2021 until now is huge, such that it is a completely different direction that merits a totally different perspective. But, still in service of the same base problem. 

Mr. Goetz spoke about the multiple generations of "value type" research in Java, and what came out of each:

1. "Generic Specialization Types" - This is evolved from the initial goals of JEP-169 in 2012, and really comes down to "how can we get primitives into generics and not break all the things" -- inevitably this resulted in better understanding the scope of the problem
2. The "L-World" Approach - This is the first "solves all the problems" mechanical solution - and the one I referred to most recently. This solution really comes down to "what language, library, specification, and runtime changes must we make to get value types into Java from top to bottom (insert wilhelm scream of falling off a cliff) - the implication of the desired changes was at a scope that might not have matched what was comfortable to give to all developers on Java. This was what Mr. Goetz refers to as the "peak of complexity".
3. The current, language-forward Approach - The goal backed away from the prior approach, to instead give developers the tools to define their intent, such that the complexity can be inferred, rather than *specified* in the language (what Mr. Goetz refers to as the "virtuous collapse")

By the end of the L-World iteration of engineering R&D, the performance numbers were intesely good -- such that orders of magnitude in gains showed up in value-based operations around matricies and things that were naturally value-friendly. At the same time, the impact was still pervasive to every layer of Java, and would hurt developers everywhere, much like choices of the past:

* Within the runtime, you had to know about types of all stripes and flavors to handle all the bytecode and constant pool variants  - This makes it hard to make a JVM that can do all the things expected of a JVM
* The specification had to be expanded to support all new value type variants - This also makes it hard to make a JVM and also makes it hard to make a compiler, tools around the runtime, and anything that cares about Java below the `.java` language specification
* The libraries had to be revisited to support this new type duality everywhere - this is a problem for source and binary compatibility across the board, especially as it pertains to public APIs used by everyone like `java.lang` and `java.util` - do we want another `IntStream` problem, but for everything?
* The language had to have support for this new value-based type that also happened to navigate the runtime like a class - this impacted every compiler integration in every IDE on the surface of the developer ecosystem in a very pervasive and messy way

In short, the cost of all of these was quite high, even with "clever tricks". As per the most recent talk, the impact was so pervasive that it felt similar to the impact of primitives vs. objects in Java 1.0 (which any Java developer has felt non-stop if they have encountered it even once), but now just passing the buck on to teams with their own types (make your choice: is this a heap object or not -- good luck!).

The problem of the `L-World` approach was that it pushed the "primitive-vs-heap" problem through every layer. If you needed a primitive, that choice permeated the entire stack from compilation, to runtime, and even runtime tools.

This meant that everything was burdened with this new complexity, even if that complexity was only one of "How is the data mapped to the underlying machine". This machine fidelity was, in retrospect, part of the problem of this approach - favoring fitting into the machine was the parity mismatch between the language and the goal being achieved.

After pulling back and revisiting the problem, the team boiled down to two major problems that needed to be solved to enable mapping language types into their most efficient runtime representations down to:

1. We need to know that identity doesn't matter
2. We need to know whether null-ability matters

This seems oddly simple. And, in retrospect, perhaps it is. That's the beauty of years of staring at a complex problem while collecting huge amounts of community feedback -- the best path becomes far more clear over time.

What the team learned is: if they could get those two bits of information from the running program (no matter how old), then everything else could be figured out by the runtime when it was most appropriate, and ignored and treated the way it normally would otherwise. This resulted in:

* No needs for a specification update
* Minimal needs for a languuage update
* Runtime updates a-hoy, but that's the easiest place to evolve and change without hurting anything
* A couple new constant values associated with the class, such that we can give it the behavior it expects
* Source and binary compatibility can be maximized such that hard choices to make things "value-friendly" likely can continue to work on runtimes not prepared for it

So: let's investigate these two big questions, see what it changes to fix them, and see what's left on the other side.
investigate Identity

Letting go of the need of identity is more complex that it might seem. At first it seems simple: I don't care! But in practice, it can break a lot of things:

* The intent of `a == b` changes fundamentally
* Semantics that rely on equality and comparison will not work the same way as if identity was ever considered
* `synchronize (x)` is no longer possible on a type that has no identity 
* `System.identityHashcode(x)` isn't useful for targeting a specific object assuming the type is value-based

For teams that have operated on a language like Scala or Kotlin this may seem relatively straightforward or perhaps inconseqential, but in the pure Java world, this is a non-trivial impact to some existing runtimes.

The mechanic for "opting-in" to this behavior turns out to be quite simple: a new keyword that says "I don't care about identity":

```java
// Traditional Java type -- not super useful
public class MyClass {
  private int myValue;
}

// The same - but this time as a value type:
//   -- equality, hashCode, and synchronization are off the table
public value class MyValueClass {
  private int myValue;
}

// These two are so close to as not matter
public record MyRecord(int myValue) {}
public value record MyRecord(int myValue) {}
```

By specifying the type is `value`, you are opting-in to optimizations that involve foregoing the requirement of identity. This unlocks a huge amount of JVM optimizations at runtime.

# Forgiving Null-ness

The other big challenge is whether or not a value can be `null`. Nullity is a unique problem that is just implied by "heap types" in Java - things that won't immediate occupy a register the minute they are created only matter if you intended to actually store them in a register - if they are heap-based, nullity is intuitive by the design.

This "can the value be null" information matters a lot with primitives, as it must be encoded *into the value*. If you have a 64-bit value, but it can be null, you have to choose **at least** one bit to capture that null state, in effect impacting the ability to store the native value.

Once we eliminate this complexity, we eliminate the challenge of figuring that out. That makes a variety of value-copying complexities... no longer complex.

To accomplish this approach, they are exploring a series of type markers that match other languages to explicitly indicate nullity such that the runtime can make the right choices:

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

And, even if existing code does not explicitly opt-in, escape analysis and other runtime affordances still allow for finding the path through such that "stack-level" optimiziations might be possible.

