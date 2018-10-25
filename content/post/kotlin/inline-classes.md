---
title: "A Semi-Deep Dive into Kotlin Inline Classes"
date: 2018-10-24
tags: ["kotlin","java","jvm"]
---
With Kotlin 1.3, a new experimental feature called "Inline Classes" is now available. This post is a somewhat deep dive into the nature of the implementation, how it works, where the edges are, and what limitations currently exist. Let's take a look!

<!--more-->

It is quite common for programs to have values that are definable as a single primitive type, but have a more specific purpose, or perhaps a set of enforced constraints.

For example, consider a system that models time. In almost all Java libraries that tackle this problem, hours, minutes, and seconds are modeled as integers for practical reasons. Here is a simplified but not entirely unrealistic constructor for some `Time` data type:

```kotlin
data class Time(val hours: Int, val minutes: Int, val seconds: Int)
```

In a perfect world, proper types for the individual components would make this far more readable, type-safe, validate-able, and would also even allow for the components (like `Hours`) to be used independent of the containing type, including having custom functions on the individual types. With Kotlin 1.2 you *could* write this:

```kotlin
data class Hours(private val value: Int) {
  fun toMinutes() = Minutes(value * 60)
  fun validate() = value in (0..24)
}
data class Minutes(private val value: Int) {
  fun toSeconds() = Seconds(value * 60)
  fun validate() = value in (0..60)
}
data class Seconds(private val value: Int) {
  fun validate() = value in (0..60)
}

data class Time(val hours: Hours, val minutes: Minutes, val seconds: Seconds) : Time

val time Time(Hours(12), Minutes(30), Seconds(15))
val hours = time.hours
val asMinutes = time.toMinutes()
```

While it may be academically pleasing and "pure" to create a time system where hours, minutes, and seconds are all separate types with independent rules and functions and validations, it has a real impact on the practical performance of a system. If your program is dealing with thousands of `Time` objects a second, enforcing four heap indirections (as opposed to one) to model a simple wall-clock of time is asking a lot of a program, just for a small amount of API purity. When considering that most "time" APIs are built to be used in a variety of different programming models (e.g. shipping as a common reusable lib or part of the JDK...) -- it's entirely possible in these cases that performance may be a real practical factor, so primitive ints prevail!

As a result, most systems that model time, unsurprisingly, make the compromise of using `ints` (and possibly types like `Hours` filled with static helper methods to check and box those ints internally) to model the fundamental parts of the time and date components of the library. This is usable, perhaps, but not super expressive. It results in constructors or factory methods that look like: `LocalDate.of(int,int,int,int,int,int,int,int,ZoneId)`. It would be great to have a way to clarify a types use to be more specific and more constrained to the actual purpose, without buying into a huge practical memory overhead, simply as a result of that API choice.

## Why Not Type Aliases?

In many Kotlin programs in the wild today, programmers have resorted to [type-aliases](https://kotlinlang.org/docs/reference/type-aliases.html) to provide more clarity to usage of fields. For example, the previous example may have aliases introduced like this:

```kotlin
typealias Hours = Int
typealias Minutes = Int
typealias Seconds = Int

data class Time(val hours: Hours, val minutes: Minutes, val seconds: Seconds)
```

This does add clarity to readers of the code, however it doesn't actually enforce anything, and doesn't add clarity to most callers of the API. Any random Ints can still be passed into APIs using type aliases, as they are only aliases. The API is still, in fact, a regular "int", even to the compiler. So while this adds some nice documentation, it does nothing to improve safety or add functionality. This is legal to the above API, for example:
```kotlin
val time = Time(99, -400, 111111111)
```

[Inline classes](https://kotlinlang.org/docs/reference/inline-classes.html) are an experimental 1.3 Kotlin feature intended to add compile-time clarity and safety to cases like these, without adding the runtime overhead and indirection that traditional objects would imply.

Following the last example, let's consider this use of inline classes:

```kotlin
inline class Hours(private val value: Int) {
  fun toMinutes() = Minutes(value * 60)
  fun validate() = value in (0..24)
}
inline class Minutes(private val value: Int) {
  fun toSeconds() = Seconds(value * 60)
  fun validate() = value in (0..60)
}
inline class Seconds(private val value: Int) {
  fun validate() = value in (0..60)
}

data class Time(val hours: Hours, val minutes: Minutes, val seconds: Seconds)
```

This is only different from the previous example in that `data` was replaced with `inline`.

First, if we were to look at our time data class in the compiled form, we would see, perhaps unsurprisingly, that it is comprised of three primitive values - it is (nearly) the effective counterpart of writing the following class in Java code:

```kotlin
public class Time {
  private final int hours;
  private final int minutes;
  private final int seconds;

  public Time(int hours, int minutes, int seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public int getHours() { return hours; }
  public int getMinutes() { return minutes; }
  public int getSeconds() { return seconds; }
}
```

(**Note**: This "simple" mapping to Java can be a bit of a lie with inline classes. More on that below)

This should demonstrate that wherever possible, Kotlin inline classes are a compile-time-only technique, and are, ideally, as efficient as the value type they are intended to wrap (in this case, an integer).

## Boxing It Up

I've said "wherever possible" here, because sometimes inline classes are treated as full objects, anyway. At first, it may seem surprising that the inline class has a boxed type at all; after all, isn't the point of inline classes that they are represented purely as their primitive type in the runtime?

However, in practice, object types leak in to any program; anyone that has worked with any Java primitive types has run into this. Boxed types exist for the same reasons classes like `java.lang.Integer`, `java.lang.Long`, and `java.lang.Boolean` exist: there are cases where a primitive simply cannot be used.

Consider this case:

```kotlin
val listOfHours: List<Hours> = arrayListOf(Hours(1), Hours(2), Hours(3))
```

In Java 11 and prior (at least), there is no way to represent a list of primitives. Java, when confronted with a list of `ints` would instead create a `List<Integer>`. Kotlin inline classes will do the same here, but it will be a `List<Hours>` instead. The idea (just like Java auto-boxing) is to make those transition boundaries seamless. That doesn't mean, however, that they are performant (i.e.: Boxing is expensive).

With any given inline class, Kotlin will generate two types: a `boxed` type, and a static utility class to facilitate both primitive use and boxing. If we take a deeper look at `Hours`, we will see these two types in the compiled code (illustrated as Java source code):

```kotlin
public class Hours {
  private int value;
  public Hours(int value) {
    this.value = value;
  }

  public int unbox() {
    return value;
  }

  public String toString() {
    return Hours$Erased.toString(this.value);
  }
  public boolean equals(Object other) {
    return Hours$Erased.equals(this.value, seconds);
  }
  public int hashcode() { return Hours$Erased.hashcode(this.value); }
}

public class Hours$Erased {
  public static int constructor(int value) {
    /* ... */
  }
  public static final Hours box(int value) {
    return new Hours(value);
  }

  public static int hashcode(int value) {
    return Integer.hashcode(value);
  }

  public static boolean equals(int value, Object other) {
    boolean result = false;
    if (other instanceof Hours) {
      result = ((Hours) other).unbox() == value;
    }
    return result;
  }

  public static String toString(int value) {
    /* ... */
  }
}
```

As shown here, the boxed type delegates to the "erased" peer for all major operations. The "erased" type serves as a static helper class through which the underlying types of the inline class can be passed to solve various problems, like equality checks and hashcode generation.

Per the spec, inline types will be converted to the "boxed" object representation in a variety of cases:

* When used via any API that only supports `Object` or object subclasses
* When used as a "nullable" type in Kotlin (e.g. `Hours?`)
* When referred to as an implemented interface

When Kotlin encounters an inline class as its primitive type in use of a given function, it will defer to the primitive type when it can. For example, given this code:

```kotlin
fun doHoursEqual(a: Hours, b: Hours) {
  println("$a == $b")
  return a == b
}
```

The compiled "java" code for this would effectively look like this:

```java
public boolean doHoursEqual(int a, int b) {
  System.out.println(a + " == " + b);
  return a == b;
}
```

Note that it is passing the primitive type directly into the the code wherever possible.
However, given this code, things get more complicated:

```kotlin
fun doAnyHoursEqual(a: Hours, b: List<Hours>) {
  for (val someHours in b) {
    println("$a == $someHours")
    if (someHours == a) {
      return true
    }
  }
  return false
}
```

(This is not idiomatic Kotlin code and is "ugly", but I wanted it very simple and procedural to be easy to demonstrate what happens).

In this case, we are now dealing with a mix of primitive "inline" types and boxed inline types due to the use of a `List`. The compiled counterpart in Java would look something like this (at least, conceptually):

```java
public boolean doAnyHoursEqual(int a, List<Hours> b) {
  for (int i=0; i<b.size(); i++) {
    Hours someHours = b.get(i);
    System.out.println(a + " == " + Hours$Escaped.toString(someHours));
    if(Hours$Escaped.equals(a, someHours)) {
      return true;
    }
  }
  return false;
}
```

As illustrated, when boxed types are required, the static helpers are used to dereference the type at runtime. In cases where Kotlin can refer to the underlying value type itself in the compiled code it will, but when unboxing is required, it will unbox or defer primitive operations like equality or hashcode to the utility `...$Escaped` type.

The same is true when passing inline types into boxed receiver sites. The previous example of creating a new array list also would use the helper APIs:

```kotlin
val hours = arrayListOf(Hours(1), Hours(2), Hours(12))
```

Roughly, this looks like this in Java:

```java
List<Hours> hours = new ArrayList<>(
  Arrays.asList(
    Hours$Escaped.box(1),
    Hours$Escaped.box(2),
    Hours$Escaped.box(12)
  )
);
```

## It's Not Just For primitives

My current description has focused on the raw and inescapable performance benefits of primitives as classes. However, there is a practical API benefits to other types -- in particular `Strings` and `UUIDs` which frequently serve as the raw data type for a variety of IDs and other referential values like names, which have constraints and requirements, but expressing them as a wrapped time is runtime indirection that causes headache.

However, be careful when thinking about applying this everywhere... here be dragons...

## To Use From Java (or NOT)

If you are like me, looking at this representative inline class type, it may seem immediately ideal to use these with a variety of bean-based Java tooling such as JSON and database mapping tools, which use property conventions to facilitate data mapping. Wouldn't it be great to have this JSON definition:

```json
{
  "hours": 10,
  "minutes": 30,
  "seconds": 15
}
```

... and then over on the Kotlin side have inline classes that work perfectly as their "int" counterparts, without having to write any custom mapping code or registrations for your specific library of choice (meaning, Jackson sees it as a series of integers, and you can go about your business without having to constantly muck with the object mapper)?

```kotlin
data class Time(val hours: Hours, val minutes: Minutes, val seconds: Seconds)
```

Caution must be considered here. For now, the documentation is very clear that Java code (aka most reflection based libraries) is excluded from understanding and invoking functions that *take* inline class variables:

> Since inline classes are compiled to their underlying type, it may lead to various obscure errors [...]. To mitigate such issues, functions using inline classes are mangled by adding some stable hashcode to the function name. [...]
>
> Note that `-` is an invalid symbol in Java, *meaning that it's impossible to call functions which accept inline classes from Java.*

So, going back to our "Time" class, I said it "almost" matches the primitive data class counterpart above, and then hinted that is a lie. The reason is that Kotlin aggressively tries to *stop* use from Java callsites. Kotlin rewrites any function that *receives* an inline class type to be mangled and compile-time-invisible through the use of an illegal method name. This prevents a variety of uses with "Java" compatibility in the current form. Specifically, consider if we had vars instead of vals on our data type (meaning that the generated Java class had both getters and setters):

```kotlin
data class Time(var hours: Hours, var minutes: Minutes, var seconds: Seconds)
```

The generated byte-code, if written in Java, might look like this (the actual hash values would vary in the real generated code, of course):

```java
public class Time {
  private int hours;
  private int minutes;
  private int seconds;
  public Time(int hours, int minutes, int seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public int getHours() { return hours; }
  public int getMinutes() { return minutes; }
  public int getSeconds() { return seconds; }

  public void setHours-1x9gxwl6(int value) { this.hours = value; }
  public void setMinutes-axdjxx6m(int value) { this.minutes = values; }
  public void setSeconds-dooczpta(int value) { this.seconds = values; }

   // ...
}
```

Those setters sure look scary. Kotlin code will totally understand this and will be fine with these hashes appended; Kotlin understands inline classes and will compile to the fancy "hashcode-appended-setter". However, this mangling of the "setters" will confuse many (all?) Java bean libraries that depend on setters, making this largely a "Kotlin only" feature for the current iteration if you rely on setter method use. Further, Java code cannot even *compile* against this due to the use of dashes. This is by design; they don't want you interop'ing Java code with inline classes, because the underlying structure is something they fully expect to change. This restriction may change or be improved as the feature evolves from experimental, but, for now, this is a real factor to consider.

You *could* potentially use this if you were careful (and maybe foolhardy) -- for example, this would likely work fine in JSON because of the nature of constructors, and the lack of dependency on setter methods:

```kotlin
data class Time @JsonCreator constructor(
  @JsonProperty("hours") val hours: Hours,
  @JsonProperty("minutes") val minutes: Minutes,
  @JsonProperty("seconds") val seconds: Seconds)
```

Arguably, using `@JsonCreator` is preferable with Jackson anyway, since it avoids having to make types mutable just for the JSON tooling. However, this relies on a couple things not changing in the inline class definition by Kotlin:

* Constructors cannot be name-mangled and Kotlin will keep using vanilla constructors for inline classes
* Inline classes used in constructors for generated types by Kotlin will continue to take the underlying value type as the arguments

If you are willing to react to the experimental nature of Kotlin in your use case, then by all means - go for it! But go into it knowing that the runtime representation of inline classes is treated as "obscured" and blocked for Java interoperability purposes.

## Comparing to Java Value Types

The name "inline" specifically implies that these classes are, in fact, a compiler trick performed via inlining. The generated runtime code is attempted to be represented as the underlying primitive value and any methods on those classes are actually compiled into static helper methods. As a result, the runtime generally has no more knowledge about these types than before, and that limits the power the runtime has to influence the way these values behave.

As a result, it should be cautioned that Kotlin inline classes are named such because they are *not* a value type in an actual sense. The plans for [value types in the JVM](https://www.infoq.com/news/2018/06/JavaValuesJun18) would allow for types at runtime that provide a superset of benefits over the current inline class model beyond what is worth covering here in their entirety. The *runtime* can handle the types as being aligned primtives, beyond what the compiler attempts to provide, including:

* Type-safe "objects" comprised of a chain of primitives
* Zero overhead memory alignment
* True polymorphism and inheritance
* Proper parametric type and generics support

## What About Other Languages?

The problem being solved by inline classes and this general approach to solving them with (mostly) compiler tricks is not a new concept in programming languages. For example, [Haskell](https://wiki.haskell.org/Haskell), has [`newtype`](https://wiki.haskell.org/Newtype). Similarly, [Scala](https://www.scala-lang.org/) developed [Value Classes (SIP-15)](https://docs.scala-lang.org/sips/value-classes.html) and more recently [Opaque Types (SIP-35)](https://docs.scala-lang.org/sips/opaque-types.html) as ways to build primitive indirections on the JVM.

In all cases the goal is to bring some of the expressivity and type-safety of classes to value types without performance problems. This issue is not unique to the JVM (as evidenced by the reference to Haskell) -- primitive types in Java are simply a representation of "stack values" -- values that don't have to be referred to via pointer, but can instead be moved around in registers and memory close to the program's primary execution pipeline. This lack of indirection and lack of slow memory lookups is the key to what makes primitives so powerful in Java and so fundamental to systems programming. Hence why multiple languages try to provide the power of primitive types to programmers, while still trying to maintain some expressivity.

Consequently, it has been noted in multiple places that Kotlin inline classes are quite similar to [SIP-15 Value Classes](https://docs.scala-lang.org/sips/value-classes.html) in Scala. Many of the same compiler tricks and restrictions exist - notably, value classes:

* Can only have a single member
* Cannot have secondary constructors or initializer blocks
* Cannot override `equals` or `hashcode`
* Are implicitly final
* A runtime "boxed" type is generated to act as a proxy in `object/heap` use-cases

It should be noted that Scala is transitioning away from value classes (a 2012 feature of the language) into the more recent opaque types (a 2017 proposal), and this is a result of many developers running into some of the problems and edges with the original implementation. The differences between the implementations of value classes and opaque types, however, probably merits an entirely separate blog post, so hopefully... stay tuned!
