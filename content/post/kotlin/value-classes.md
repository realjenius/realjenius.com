---
title: "Values, Records, and Primitives (Oh My!) - Kotlin & Java's 'Valuable' Future"
date: 2021-05-09
tags: ["kotlin","java","jvm"]
---

A couple years ago, [I did a semi-deep-dive on Kotlin Inline Classes]({{< relref "./inline-classes.md" >}}) and how they were implemented. [Kotlin 1.5 was just released](https://blog.jetbrains.com/kotlin/2021/05/kotlin-1-5-0-released/), and with it came the evolution of inline classes into the start of [value classes](https://kotlinlang.org/docs/inline-classes.html). Meanwhile, [Kotlin 1.5 also now supports JVM Records](https://kotlinlang.org/docs/jvm-records.html), which at first read might sound like a very similar concept. Finally, with [JEP-401](https://openjdk.java.net/jeps/401) Java is going to bring "primitive classes" which *also* sounds like a very similar concept. This can sound all very confusing, so let's take a look!

<!--more-->

Kotlin, as a language, has changed a lot since the initial introduction of [inline classes as an experimental feature]({{< relref "./inline-classes.md" >}}). At the same time, the JVM has also evolved significantly, with releases every 6 months bringing major features, and [Java 16 being the latest major release](https://blogs.oracle.com/java-platform-group/the-arrival-of-java-16). As a result, there is a lot to consider now when thinking about Kotlin programming targeting the JVM.

Let's start with records, and get the first confusion out of the way.

## Records on the JVM and Data Classes in Kotlin

Java 14 added experimental record support. Records in Java are meant to be classes that represent a named grouping of fields, with pre-built `equals()`, `hashcode()`, and `toString()` implementations that simply aggregate across all the values of the record:

```java
public record Name(String firstName, String middleName, String lastName) { }
```

Basically, a record is meant to create a class that hits all of the defaults that many developers often desire when coding a class comprised of several values: immutable, property-based, and identity-less. If this sounds a lot like a Kotlin `data class` it should, as they are the same concept! Here is the same basic thing in Kotlin:

```kotlin
data class Name(val firstName: String, val middleName: String, val lastName: String)
```

Both records and data classes exist for the same general problem: to make declaring an object that is simply a composition of other values and objects as easy and fool-proof as possible.

Note that **I said nothing** about records being allocated differently than regular classes. That is because they are not! There seems to be some belief that records on the JVM have something to do with value types, but that is a misconception. Records in Java are still full heap-allocated objects under the covers as of Java 16 (though, the JVM runtime could always choose to do a variety of interesting things with records given proper escape analysis and sophistication).

Because of this close similarity, [Kotlin 1.5 can now compile data classes into Java records](https://kotlinlang.org/docs/jvm-records.html#declare-records-in-kotlin) via the use of the `@JvmRecord` annotation. This behavior is behind an annotation and not the default for a variety of reasons. Let's take a look at the differences by actually compiling the code. Here is what the `Name` data class looks like in Kotlin when analyzed by `javap` using the typical compilation method.

```java
public final class Name {
  public Name(java.lang.String, java.lang.String, java.lang.String);
  public final java.lang.String getFirstName();
  public final java.lang.String getMiddleName();
  public final java.lang.String getLastName();
  public final java.lang.String component1();
  public final java.lang.String component2();
  public final java.lang.String component3();
  public final Name copy(java.lang.String, java.lang.String, java.lang.String);
  public static Name copy$default(Name, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object);
  public java.lang.String toString();
  public int hashCode();
  public boolean equals(java.lang.Object);
}
```

In contrast, here is what the same class looks like when compiled with `@JvmRecord`:

```java
public final class Name extends java.lang.Record {
  public Name(java.lang.String, java.lang.String, java.lang.String);
  public final java.lang.String firstName();
  public final java.lang.String middleName();
  public final java.lang.String lastName();
  public final java.lang.String component1();
  public final java.lang.String component2();
  public final java.lang.String component3();
  public final Name copy(java.lang.String, java.lang.String, java.lang.String);
  public static Name copy$default(Name, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Object);
  public java.lang.String toString();
  public int hashCode();
  public boolean equals(java.lang.Object);
}
```

As you can see, a few things change that prevent this from being the default:

1. It only works on JVM bytecode 16+ (or 15 with preview enabled), and extends `java.lang.Record`
2. The "getters" change to the standard Java record format: instead of `getFirstName` we get `firstName`.
3. The implementation of `equals`, `hashCode`, and `toString` may vary for records than for data classes by default
3. None of the properties can be `vars` because Java records are implicitly immutable (otherwise you will get an error like `Constructor parameter of @JvmRecord class should be a val`)
4. Backing fields and non-primary-constructor fields are not possible with records (otherwise you will get an error like `It's not allowed to have non-constructor properties with backing filed in @JvmRecord class`)

That said, it still implements the `component` functions, and the `copy` function, which enable continued use with all the normal data class workflows.

To use this new structure, the kotlin `Name` class can be compiled on the JVM into the corresponding record very simply:

```kotlin
@JvmRecord // Tell the compiler to generate a Java record instead
data class Name(val firstName: String, val middleName: String, val lastName: String)
```

## Valhalla and Primitive Classes

[Project Valhalla](https://openjdk.java.net/projects/valhalla/) has been under design and development for a significant amount of time. The project genesis really came from the phrase: "codes like a class, behaves like an int". The idea being that on the JVM a developer could code a class of multiple value types that is never allocated on the heap, but can still be used in all the expressive ways of full heap-allocated types, including with generic specialization.

Even if as a reader this is all news to you, it is probably clear how big of a project this actually is. It has taken many years and many iterations to build a path forward. This specification will cross all of the tiers of Java, including the classfile spec, the runtime spec, the language spec. Making the wrong choices would haunt Java for years to come.

Multiple JEPs are being worked on in Java to enable the use and proliferation of "value types", now commonly referred to as primitive objects.

* [JEP 401](https://openjdk.java.net/jeps/401) defines the concept of a `primitive class` and the boundaries of that in the JVM (we will review this more below)
* [JEP 402](https://openjdk.java.net/jeps/402) retrofits the eight JVM primitives (boolean, int, long, etc) as primitive objects, meaning that `int` simply becomes a compile-time alias for the integer primitive class, in turn that means a variety of things, including that methods on `Integer` can be called directly on int, e.g.: `23.compareTo(42)`
* A pending JEP to implement generics support for primitive objects

Borrowing from the JEP, a class could be implemented called `Point` as a primitive class:

```java
primitive class Point implements Shape {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() { return x; }
    public double y() { return y; }

    public Point translate(double dx, double dy) {
        return new Point(x+dx, y+dy);
    }

    public boolean contains(Point p) {
        return equals(p);
    }
}

interface Shape {
    boolean contains(Point p);
}
```

Primitive classes will have a variety of restrictions to enable their super-power of flat, stack based memory representations, including:

* Implicit finality of all fields
* A primitive class cannot refer to itself directly or indirectly via its own "primitive" fields (this doesn't count for reference types, like String since this introduces a pointer indirection)
* No class hierarchy (though as the example above shows, interfaces are allowed)

Overall the goal is that, in memory, the above type could be allocated directly as this:

```
+--------+--------+
| 64-bit | 64-bit |
+--------+--------+
```

No object header, no pointers to heap memory, just a flat representation of the point coordinates. Further, a `Point[]` should be able to be represented in memory as follows:

```
 <-- Point[0] ---> <-- Point[1] --->  ...  <-- Point[N] --->
+--------+--------+--------+--------+-----+--------+--------+
| 64-bit | 64-bit | 64-bit | 64-bit | ... | 64-bit | 64-bit |
+--------+--------+--------+--------+-----+--------+--------+
```

Compare this to a typical Java reference-based list, and the result is far more complicated (and expensive to traverse):

```
+----------+----------+----------+
|  32-bit  |  32-bit  |  32-bit  |  (pointers)
+----------+----------+----------+
    |           |           |
    |           |           |
+--------+  +--------+  +--------+
| header |  | header |  | header |  (heap objects)
+--------+  +--------+  +--------+
| fields |  | fields |  | fields |
| [....] |  | [....] |  | [....] |
+--------+  +--------+  +--------+
```

Even conservatively, on a 64-bit platform, with typical Java reference types each object would include:

1. The full reference/pointer of at least `32-bit`s each
2. A `16-byte`/`128-bit` (padded) object header each
3. The actual data (which in this case is `128-bits`)

Consequently, if we assume twenty objects, the full cost in Java memory would be: `(128*20) + (128*20) + (32*20) =` **`5760 bits`** or `720 bytes`.

Conversely, assuming waste-free storage usage with value types, this would be simply be `128 bits * 20 objects` = **`2560 bits`** or `320 bytes`.

This disparity gets worse as the scale grows, and of course one of the largest hurdles here is the "[NUMA](https://en.wikipedia.org/wiki/Non-uniform_memory_access)" cost, where fetching records off of the heap will simply be far more expensive due to the damage it causes to CPU cache coherence. As the program traverses the array (which most programs tend to do), it will be following pointer after pointer, fetching blocks of data from each particular Java reference into CPU cache, and then dumping it right out to fetch the next one. With true cache coherence, this doesn't have to work this way, as the data is inline with the starting point of the object, so when fetching a block of data for array traveral, multiple contiguous chunks are pulled in to CPU cache at once.

Anyone familiar with programming using structs or similar concepts in a language like C is familiar with this "higher-level" stack value concept, and the benefits. That said, with Java primitive classes the goal is to not lose language expressiveness or power, allowing for complex concepts and data-structures to be modeled around primitive types transparently without losing the performance benefits (for example, a `java.util.List` that supports both reference and primitive objects).

Primitive classes in Java will always have an explicit duality: they can exist both as a primitive "stack-based" value type, or as a reference "heap-based" type, and conversion between the two is handled by the compiler and runtime where appropriate.

In existing Java terms this is like the difference between an unboxed and boxed primitive type, where `int` is the stack-based value type, and `java.lang.Integer` is the heap-based reference type. So, this conversion concept of "boxing" in Java is lifted into a new "reference type conversion" concept in Valhalla. Specifically, the primitive class itself will default to an "unboxed" value type, and a new `.ref` type is introduced to allows for treating the primitive type as a reference type:

```java
Point p1 = new Point(1.0, -1.0); // value type, 128-bit memory footprint.
Point.ref p1Ref = p1; // Automatic conversion to ref, Pointer with heap-allocated object
Point p1a = p1Ref; // Automatic conversion to value type, 128-bit memory footprint.
```

To minimize breakage in existing code but still enable migrating current Java types, primitive classes also support making the reference type the default via the declaration of `.val` in the primitive class name (i.e. `primitive class Point.val`), which implies the "reference" type should be the default. As a result, the eight primitive types in Java will work a little differently than our imaginary point class:

```java
Integer i1 = Integer.valueOf(1234); // Reference type
int i1Val = i1; // automatic conversion to primitive type, 32 bit memory.
Integer.val i1Val2 = i1Val; // int is an alias to Integer.val in this spec
Integer i1a = i1Val2; // automatic conversion to another reference instance.
```

This means that if you have code like `List<Integer>` it will still deal with reference types, **but** if all goes well then a `List<int>` will also be possible after Valhalla ships.

## Side Note: Records and Primitives - Chocolate and Peanut Butter?

If the expectations of records and primitive classes feel very closely related to each other, that is by design. Most of the restrictions in primitives exist in records as well. Adding "records" to primitives is something that has been discussed, to get the feature benefits of both in one ideal state. [Brian Goetz discusses it a bit on Stack Overflow](https://stackoverflow.com/a/63365195):

> A record requires you to give up on extension, mutability, and the ability to decouple the representation from the API. In return, you get implementations of constructors, accessors, equals, hashCode, and more.
>
>A primitive class requires you to give up on identity, which includes giving up on extension and mutability, as well as some other things (e.g.,    synchronization). In return, you get a different set of benefits -- flattened representation, optimized calling sequences, and state-based equals and hashCode.
>
> If you are willing to make both compromises, you can get both sets of benefits -- this would be a `primitive record`. There are lots of use cases for primitive records, so classes that are records today could be primitive records tomorrow, and would just get faster.

## Kotlin 1.5: Inline Classes Start to Grow Up

Primitive classes in Java sound similar to Kotlin `inline` classes for good reason. Inline classes exist to add expressivity to primitive types in Kotlin without losing their performance benefits.

However, their current implementation, being a compiler-driven solution for multiple platforms, has some limitations and differences. Let's take a deeper dive.

### Inline Classes Before 1.5

As a reminder, inline classes were introduced in Kotlin (first experimental in 1.2.30), and simply have an `inline` keyword:

```kotlin
// 1.3.x Inline Class Example
inline class Price(val amount: Int) {
  fun dollarAmount() = amount / 100
  fun centsAmount() = amount % 100
}
```

Inline classes were intended to add expressivity, validation, and type-safety to a primitive value without adding memory overhead. Under the covers, an inline class would be compiled to the "raw" primitive type wherever possible, and only boxed into a heap-allocated-object when needed by the underlying platform (for the JVM that means any place it is used as a `java.lang.Object` or other reference type).

For example, in Kotlin code a program might use this price code like this:

```kotlin
fun printAmount(price: Price) {
  println("Dollars: ${price.dollarAmount()}")
  println("Cents: ${price.centsAmount()}")
}
```

On the JVM the generated Java bytecode would look like something like this Java code instead:

```java
public void printAmount(int price) {
   System.out.println("Dollars: " + Price.dollarAmount(price));
   System.out.println("Cents: " + Price.centsAmount(price));
}
```

*(**Note**: In practice due to name mangling the actual implementation would be uglier than this, but this shows the conceptual model)*

The static helpers on the generated `Price` class know how to perform logic on the underlying Java primitive to get the values out that are part of the member functions on the Kotlin side, without ever turning it into a reference type which introduces all of the aforementiond memory troubles.

This can be seen by looking at the generated `javap` output for this class:

```java
public final class Price {
  public static final int dollarAmount-impl(int);
  public static final int centsAmount-impl(int);
  public static java.lang.String toString-impl(int);
  public static boolean equals-impl(int, java.lang.Object);
  public static int constructor-impl(int);
  public static final realjenius.Price box-impl(int);
  public static final boolean equals-impl0(int, int);
  public static int hashCode-impl(int);
  public final int getAmount();
  public java.lang.String toString();
  public int hashCode();
  public boolean equals(java.lang.Object);
  public final int unbox-impl();
}
```

Note that for a given `int` (deemed by the Kotlin compiler to be a price type when generating bytecode), there are static counterparts for `dollarAmount`, `centsAmount`, `toString`, `equals`, `hashCode`, and also has hooks for boxing, unboxing, and running constructor logic.

### Evolving into Value Classes

With Kotlin 1.5, the `inline` keyword is now deprecated, and instead the `value` keyword is introduced, along with making value classes "stable". This is paving the way for the future of value classes in Kotlin, though at this point, only the same `inline class` behavior from pre 1.5 language implementations is supported.

Additionally, when using the `value` keyword to define inline classes, JVM-based developers must also use the `@JvmInline` annotation to compile to the JVM (this will be discussed in more detail below). Revising our previous example for 1.5:

```kotlin
// 1.5.x Inline Class Example
@JvmInline
value class Price(val amount: Int) {
  fun dollarAmount() = amount / 100
  fun centsAmount() = amount % 100
}
```

The name was changed in part to remove any false correlation with `inline` functions (since inline classes are not comparable in any practical way). However, the name change is also paving the way to more powerful "non-identity-based" object, much like the primitive class in Java, which will be reviewed more below.

Current value types in Kotlin (unlike the primitive class proposal in Java) are designed to be "transparent" to the developer. Rather than forcing the developer to choose whether a value or reference type is most appropriate, Kotlin is going to try to make the most optimal choice given the context. This would have been nearly impossible in Java, and would certainly have been a significant breaking change for most runtimes because of the "loss-of-identity" challenge.

Kotlin, being a relatively new language, has the opportunity to continue to intelligently mask the distinction between value and referenced-based types since it has yet to leak those nuanced details into the language specification. This is much like how `Int` in Kotlin is interchangeable as a value or reference already, and the compiler determines when is the most appropriate use for each depending on the targeted platform.

Kotlin inline classes have some subtle distinctions between the Java Valhalla proposal, but the most striking difference currently is that they can only wrap a single value. The reason for this in the first implementation of inline classes is a practical one: it makes for a far simpler conversion in the compiler when defining method signatures. For example, `fun someFunction() : Price` can easily be converted to the Java counterpart `public int someFunction()`; conversely the story for returning a price with multiple fields will always require some form of boxing, as Java has no "multi-field value type" support without Valhalla primitive classes.

### Why `@JvmInline`?

This leads to the new annotation that is required with 1.5. It may feel confusing that this annotation is required on value classes in the stable value class implementation when targeting the Java runtime, but the reason for this is based on the transitional nature of the JVM. As described in the [Value Class KEEP](https://github.com/Kotlin/KEEP/blob/master/notes/value-classes.md):

> The @JvmInline annotation makes it explicit that something special with this class is going on in JVM, and it enables us to support non-annotated value class in the future Valhalla JVM by compiling them using the capabilities of the Project Valhalla.
>
>> The key design driver to naming the annotation @JvmInline was conceptual continuity for early adopters of Kotlin value/inline classes. This naming move is still risky, since it assumes that the corresponding Valhalla concept will indeed get named a “primitive class”. If Valhalla matures to a release with “inline class” naming then we’ll have to deprecate Kotlin @JvmInline annotation and find another name for it to avoid confusion with a stable Valhalla JVM release.
>
> Why not the reverse? Why not a use a plain value class now, and add some @Valhalla value class in the future? The answer is that, so far, Valhalla promises to be the right way to implement value classes on JVM and Kotlin’s key design principle in choosing defaults is such that “the right thing”, the thing that developers will be using most of the time, should correspond to the shorter code. Ultimately, with the Project Valhalla, it will be the right thing to compile a default (non-annotated) value class with Valhalla, so it means that we must require an annotation now, with pre-Valhalla value classes, to avoid breaking changes for stable Kotlin libraries in the future.

So, in other words, once Valhalla is shipped, and Kotlin has support for it, a program compiling on this backend can remove the `@JvmInline` annotation, which will stop generating the Kotlin-based value class specification, and instead will just switch to using `primitive class` Java types. This makes the ABI very different for the JVM implementation (indicated by the annotation change), but also makes Kotlin value classes as fast and powerful as the Java counterpart.

As for why this is a JVM-only concern: this challenge is unique to the JVM backend. The JS and Native backends for Kotlin are already operating in a "closed world" compilation model, where the assumption is that the ABI of the underlying code is completely fungible without worrying about breaking other non-Kotlin code. With the JVM, Kotlin and Java interoperability is a feature that must be maintained release to release.

## What's Next for Kotlin Value Types?

The [Value Class KEEP](https://github.com/Kotlin/KEEP/blob/master/notes/value-classes.md) is absolutely chock full of research and discussions around the various features that value classes should have moving forward. Here are a few interesting details being explored in relation to value types:

### Multi-field support

Despite the implementation being challenging on the Java backend, the plan is currently to move forward with multi-field support to make it available for other backends.

The implementation may not be optimally performant on the JVM (likely it will trigger boxing almost everywhere), but on other platforms like Kotlin/Native (a LLVM-based platform) there are a variety of tools available for encoding a fast value type already today. And once Java unleashes primitive classes, Kotlin will be ready to start compiling to that target, getting all of the same performance benefits.

### Immutability and Copying

One of the challenges for clean programming with immutable types is that mutation and assignment operators generally become useless. For example:

```kotlin
data class Person(val name: String, val age: Int)

val bob = Person("Bob", 35)
// Would be a compiler error
// bob.age += 1

// Instead:
val olderBob = bob.copy(age = bob.age+1)
```

There are a couple problems with this. First, it is verbose, even with Kotlin's named property syntax. Second, the use of `val` is counter-intuitively discouraged for developers, because now the scope is polluted with both `bob` and `olderBob`. The value type specification would bring this even more to the forefront as they are completely immutable by design, meaning that copying is the only way to work with them.

To combat this, the Kotlin designers are considering a special type of `copy var` type that allows for implicit copying of objects (by the compiler):

```kotlin
value class Person(
  val name: String,
  copy var age: Int // Indicates mutation is allowed, but only via copy variable
)

val bob = Person("Bob", 35)
bob.age += 1 // actually copying bob and making the change on the new object
```

Age is marked as a `copy var`, meaning it can be mutated, but doing so results in the object being copied, and the reference to the object being swapped (in this case, re-assigning `val bob`). Because only the reference that was mutated is swapped, it gives the benefits of immutable objects, as references are copy-by-value, meaning that changing the data that a variable points to only impacts the local function.

Further, Kotlin will generally be able to detect when multiple mutations happen one after another and escape them into a single copy of the object versus a chain of several mutations, meaning that Kotlin could ensure that this will still result in only a single object copy:

```kotlin
val bob = Person("Bob", 35)
bob.name = "Robert" // change 1
bob.age += 1 // change 2 -- implies the "REAL" copy happens here
```

This proposed `copy` model becomes even more impressive when dealing with deep mutation chains. Here is an example from the KEEP where an order has a copy-mutatable delivery, the delivery has a copy-mutatable status, and the status has a copy-mutatable message. In this model, changing the message is as simple as this:

```kotlin
order.delivery.status.message = updatedMessage
```

Without "magic copy" support on variables, this works out to something more like this:

```kotlin
order = order.copy(
    delivery = order.delivery.copy(
        status = order.delivery.status.copy(
            message = updatedMessage
        )
    )
)
```

Under the covers, Kotlin is still copying and swapping variables, but the `copy var` syntax makes it much easier to reason about and less error prone.

#### Copy-Vars on the JVM

Since this is a JVM-based article, it is worth discussing how this will leverage the JVM... eventually. On the JVM the current plan is around using "wither" syntax for these mutations, where each `copy var` gets a `with<VarName>` counterpart that performs the mutation and copy. This means that the underlying JVM code would look something like this:

```java
Person bob = new Person("Bob", 35)
bob = bob.withAge(bob.age+1)
```

When dealing with cases of multiple mutations, the Kotlin compiler can try to optimize by hanving something like `Person withNameAndAge(String name, int age)`.


#### Other Copy Var Fun

There is a significant amount of further nuance around this concept including:

* Handling multi-field-mutation efficiently on multiple backends (avoiding/escaping multiple copies)
* Handling copyable properties from within member functions
* Mutable copy properties with `get/set`
* Extension functions for copying with immutablility
* Immutable and persistent collection support
* Declaring copy constructs on interfaces
* Returning values from copy functions
* Copy operator support
* Copying Lambdas and Functions

Be sure to check out the KEEP for more details on all of this. I also hope to talk more about the details in the keep and the implementation effort in future blog posts.

### Retrofitting Existing Kotlin Types

Another key component is to use all of the above immutable value type support to migrate built-in types like `Int` and `Long` into value types, which immediately enables box/reference-free extensions that do mutations. Other types, such as `String` would also be interesting targets, but they introduce additional challenges; just like with Java, String currently has an `identity` meaning that developers may be relying on it being a full object. While this is not typically good "idiomatic" code, it *is* valid.

### Array Plans

No discussion about value types is really complete without talking about a way to store a bunch of them contiguously with arrays. When talking about "big math" problems that benefit from value types, inevitably it comes down to a lot of instances of a grouping of values; back to the "array of doubles" challenge of primitive classes as described above.

Kotlin already has a bit of a leaky abstraction thanks to Java. `IntArray` and `Array<Int>` are not actually the same thing in Kotlin (though they code the same way to developers). An `IntArray` is explicitly a Java `int[]` (meaning stack allocated value types), where-as an `Array<Int>` is an `Integer[]` (meaning heap allocated reference types).

Because Java only has support for the eight primitives and then reified "object arrays", there is no clean generic way to store a value type array in Java today without boxing. Obviously with byte arrays and var-handles binary data can be encoded and decoded (which is often used by some libraries that need to eliminate heap overhead), but this has a cost associated with it too, and is incredibly complex code. Valhalla will, at some point, add primitive class arrays, but until that time, Kotlin cannot rely on it.

One option discussed in the KEEP is to have a type that is magically desugared by the compiler. Something like `VArray<Int>`, where the compiler knows that a `VArray` must be translated into the most appropriate value array type. Further, for something like a `VArray<Color>` where is defined as `value class Color(val color: Int)`, the compiler could be smart enough to compile this as an `int[]` for Java.

However, for this to work, the compiler has to *know* the type. That means the type cannot be parameterized at runtime (no `VArray<T>` allowed), which adds restrictions in code. This makes implementing things like `class MyCollection<T> { ... }` (where T is a value type) difficult/impractical, as the `VArray` cannot be referred to generically. Additionally, arrays must be "pre-filled" when created, and if the real type of the value is not known, it is difficult to sensibly pre-fill an array of primitives generically. Finally, this is only truly straightforward when referring to a single-variable inline type; once multiple variables exist, the problem becomes exponentially more complicated (does Kotlin try to hide managing multiple contiguous arrays?).

The KEEP is clear on this point: this area needs a lot more design.

### Big Constructor Musings

As immutability becomes more central to program design, one of the challenges of very big immutable classes that shows up (I've personally run into it with data classes representing typed `options` for configuring some complex service) is managing the constructor growth and complexity. Being that Kotlin (rightfully) requires that all properties be part of the primary constructor for data and value classes, evolving the API of a class when you add properties can sometimes result in frustrating compromises. Adding properties to the end with defaults is often the only option, as reordering will break consumers, and renaming properties (even if private) will break named property constructor users.

One option is to consider a special "builder style" DSL that is much like an `apply` style construction that still fulfills the primary constructor:

```kotlin
Person {
  firstName = "Bob"
  age = 35
  // any other properties are given their default or treated as a compile-time error
}
```

This is subtly different from the constructor syntax, and may seem basically identical at first glance, but it has some key differences:

1. Arbitrary logic can be applied in the code block, making dependent (and conditional) property initialization much easier
2. Order no longer matters and names are required
3. Regular constructor parameters can be mixed with it, allowing for the block to only handle properties that are required but not already initialized

In the end, this is effectively a way to compiler-codify a "builder" pattern into classes without forcing the developer to write a builder pattern (something Kotlin has worked hard to make unnecessary in many cases).

## Final Thoughts

This is an exciting transitional state for both Java and Kotlin. Value types will significantly change the way we design APIs and solve problems with performance in mind.

A lot of changes are coming in the next 12-18 months that will vastly change the types of programs both languages are capable of producing, but, there is a lot of design and challenges ahead on both sides. As things change, I hope to keep posting updates and analysis.
