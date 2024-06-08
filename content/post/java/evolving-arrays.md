---
title: "Evolving Arrays in Java"
draft: true
---

Java has long had a duality with both arrays and lists. Array have existed in Java since 1.0, and are in many ways the logical evolution of a `std::array` from C++. They:

* Define the size up-front
* Define the type with the array
* Build language constructs to navigate the array safely

At the time Java arrays were a distillation of all the improvements in prior-art: they force homogeneity in the carried values, they completely prevent overruns, there is full reflective language support for understanding their nature, and they largely preserve memory space efficiency (aside from object header space). But, compared to the power of the collections API, arrays are tough to code with for a variety of reasons:

1. They can't grow
2. They are not inherently thread-safe
3. There is no concept of encapsulation
4. They have no natural object API; anything you want to do to array requires utility functions like `Arrays.toString()`

## From the Beginning

It's important to keep in mind that Java arrays were in `1.0` - and as such, they pre-date the Java collections API by a two full years. Vectors, the first growable alternative to arrays, came out with Java 1.0, but they were more expensive wrappers around arrays that could only hold heap objects, and also brought along threading penalties; they were often used to help with coding convenience, but there were definite tradeoffs.

With Java 1.2, the proper `java.util` collections objects arrived, including `List`, `Map`, and `Set` - and with them, a variety of default implementations. `Vector` was now the less-favorable counterpart to `ArrayList` (or even `LinkedList`), and collection API interface adoption quickly grew.

Unfortunately, while they had a number of benefits, there were still gaps with lists compared to arrays. Lists:

1. ...are untyped boxes of objects - you couldn't tell what was in them, and your code was littered with casts
2. ...can't hold primitive values - due to their heap-based nature, a List could only hold objects allocated by the Java heap
3. ...don't organize their values contiguously in memory like arrays - due to their heap-based nature, the only thing in memory contiguously are references (pointers) to values on the heap; so by being bound purely to heap objects you lose this benefit immediately.

{{< alert "lightbulb" >}}
For years in the embedded and small-system space, the lack of memory conciseness in Java arrays and general coarseness with which Java memory could be managed was a significant hinderance to adoption. Interestingly, in more recent years this has renewed itself as a challenge in spaces where Java is being tuned to best use every bit of processing power. Struggling with CPU cache coherence and optimizing for deep CPU pipelines with vector opcodes, where keeping values close together can have a super-linear impact on performance
{{< /alert >}}

So, with Java 1.2 you had these two choices:

```java
int[] values = { 1, 2, 3 };

List valuesList = new ArrayList();
valuesList.add(Integer.valueOf(1));
valuesList.add(Integer.valueOf(2));
valuesList.add(Integer.valueOf(3));
```

It's no surprise that, when primitives came up, lists were often not the first choice.

With Java 1.5 and generics, we finally were given typed collections. All arguments about type-erasure aside (I still argue it was the right choice, even if it has had growing pains), this solved the "untyped" argument for most developers, even if it took years to get people to use it in their production code. We also got boxing and unboxing which made things a lot less verbose when dealing with primitive types. These days, we can now at least get to this:

```java
int[] values = { 1, 2, 3 };

List<Integer> valuesList = List.of(1, 2, 3);
```

Much better syntax! Of course, there is still a big difference in how these are stored in memory. For critical sections in some systems, arrays (or other unsafe memory trickery) still reign supreme.

With [Project Valhalla](https://openjdk.org/projects/valhalla/) we are finally getting deep runtime and language solutions to solve the ability to treat primitives like objects. This has been an incredibly complex undertaking, now taking over **ten full years** of iteration. The hope is, with the right syntax and libraries, a `List<int>` will finally be possible in Java right next to an `int[]` - with all the benefits of the List API.

Similarly, [Project Panama](https://openjdk.org/projects/panama/) is vastly improving the native bridges from Java to the underlying infrastrudcture. For some very specific memory management scenarios, this is another tool that is coming. 

So thankfully, we are seeing better ways to organize data contiguously in memory that doesn't require the hard choice between arrays, lists, and `Unsafe`. But, for those paying attention, it's been **28 years** since Java 1.0. Arrays have a bit of a head-start on all of these projects that are still in-flight. Also, it's worth remembering that while `List` is a more expressive API, most collections in Java use arrays under the covers **somewhere**, so even if we finally are able to mostly use the collections API arrays are and will always be a fundamental part of Java.

## The Nature of Java Arrays

Up to this point it almost sounds like I'm describing arrays as a less successful equivalent to the collections API. And admittedly, there are a lot of features of the List API that are more convenient than arrays. However, it's not really fair to judge arrays by this measure.

More fundamentally, the power of List is the API abstraction that comes with it being an object. Arrays, due to their intrinstic nature as a Java type, really behave like a Java primitive type; similar to `int`. They don't have the ability to change their implementation for `get` (i.e. `var x = array[i]`) and `set` (i.e. `array[i] = x`) for individual positions, they don't have methods, and they have unique quirks like the immutable property `.length`.

The unique nature of Java arrays has been revisited by many languages, which have made different choices with array representations (and primitive-like types) since; for example both Kotlin and Scala leverage their compiler and language differences to make the distinction far less difficult to deal with.

This historical perspective is important when we talk about the future of arrays in Java, as

## Into the Future

Accepting that arrays 

# Freezing Arrays

While "the ability to grow capacity" is a favorite feature to harp on with `Lists`, it's easily solved with the abstraction. A messier challenge is the ability to protect memory. Arrays, in many ways, represent raw memory space. And if an array reference is leaked somehow (or reflection is used to get to it maliciously), things can go wrong very quickly under the covers.

Keep in mind that arrays are internal to the most important types in Java, and these arrays are not supposed to be manipulated. For example:

* An `ArrayList` contains an `Object[]` - If that array was manipulated outside of the list itself, the internal state of the object is now wrong, and a variety of things could go wrong
* A `String` contains a `byte[]` (or `char[]` in older implementations) - Strings are inherently immutable in Java; their state is not supposed to change. If the internal array was changed, it could wreak havoc on a runtime
* Enumerations return a `T[]` from their `values()` method - to avoid malicious or accidental manipulation, this method returns a new copy every single time it is called, which in the wrong loop could be a huge penalty

Two JEPs exist to help solve this problem. Both have run into the challenges of doing this to a living breathing ecosystem like Java.

* [Draft JEP #8261007 - Frozen Arrays](https://openjdk.org/jeps/8261007) - The idea here is to introduce public APIs to freeze arrays (e.g. a strawman example of `someArray.freeze()` or `Arrays.freeze(someArray)`)
* [Draft JEP #8261099 - Internal Frozen Arrays](https://openjdk.org/jeps/8261099) - Like #8261007, but only internal to OpenJDK internal APIs

In both cases, the concept of freezing an array is the same - it makes `array[x] = ...` fail in some way. The array becomes "shallowly immutable", meaning that the values in the array itself cannot be changed (but if the array contained objects, those are still mutable).

As it pertains to the above two JEPs, the latter exists as incremental step to reach the first; solving many of the platform problems and fixing the most common JDK challenges with arrays, while still leaving room to explore freezing for the broader API that developers might consume.

There are a variety of benefits to frozen arrays:
1. Defensive copies can be basically removed by the runtime - a frozen array doesn't need to be copied
2. Similarly, creating a sliced subset copy of array can often be simplified drastically as the underlying memory values cannot change
3. Constant folding is possible in a variety of ways over frozen arrays due to their immutable nature, which is handy for cases where arrays play into classfiles and early object initialization
4. System safety increases significantly as it is no longer just an API design feature that protects the array, but also an intrinsic runtime feature that is far less likely to be defeated (on purpose or not)

# Sorting Intrinsics
https://openjdk.org/jeps/8044082

# Arrays 2.0
https://cr.openjdk.org/~jrose/pres/201207-Arrays-2.pdf


