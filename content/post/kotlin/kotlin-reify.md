---
title: 'Kotlin: Reified Type parameters'
date: 2017-09-08
tags: ["kotlin","java"]
---

As most Java programmers know, Java generics are _erased_ at compile-time. This has trade-offs, but the two main reasons for this are:

* Compatibility - Java 1.4 and earlier dealt exclusively in raw types at the VM level. Generics were able to compile without introducing significant new typing to the bytecode and classfile format spec.
* Simplicity - By erasing to raw types, the JVM doesn't have to understand specialization; something that has its own complexities and downsides. For example, specialized types are much more challenging to optimize with a just-in-time compiler.

Of course, because the classfile format and runtime do not understand type parameterizers, that means that languages like Kotlin and Scala which must eventually target the JVM classfile format *also* must consider the consequences of erasure.

Kotlin's approaches these topics with care.

<!--more-->

Languages can choose to do things like generate specialized classes at compile time or create metadata payloads within classes and instances, but generally, Kotlin and Scala will erase their types as well to boil down to raw classes because they want to maximize opportunity for Java language interoperability. Kotlin's specification is generally pretty clear about how it translates to Java classes (aka how it "interops"), and most of it is fairly unsurprising.

However, sometimes having "reified" type information (resolved type parameter metadata) at runtime is extremely valuable; this is something that Java simply doesn't have, and consequently you will see methods in Java that look like this:

```java

public class MyThing<T> { // compile time type.

  // Factory method *must* take the runtime type if it is to be used at runtime.
  public static MyThing<T> create(Class<T> type) {
    return new MyThing<T>(type);
  }

  private Class<T> type; // pair-matched runtime type

  public MyThing(Class<T> type) {
    this.type = type;
  }

  public void printType() {
    System.out.println(type);
  }
}

// ... use:

MyThing<String> result = MyThing.create(String.class);
result.printType();
```

In Kotlin we can model this same class and same factory method, effectively just porting the Java implementation:

```java
class MyThing<T>(private val type: Class<T>) {
  fun printType() = println(type)

  companion object {
    fun create(Class<T> type) = MyThing<T>(type)
  }
}

// ... use:
val result = MyThing.create(String::class.java)
result.printType()

```

However, Kotlin also supports a limited form of reification, meaning that it can carry and track the type parameter for you in code.

Here is an update of this class using reified parameters:

```java
class MyThing<T>(private val type: Class<T>) {
  fun printType() = println(type)

  companion object {
    inline fun <reified T> create(Class<T> type) = MyThing<T>(T::class.java)
  }
}

// ... use:
val result = MyThing.create<String>()
result.printType()
```

Note that we can refer to `T` in the code as if it was a fully resolved type literal. Specifically you can use:

* `is` comparators (`x is T`, `x !is T`)
* `as` casts (`x as T`, `x as? T`)
* Reflection literals like `::class`

This functionality has some limitations, however:

* It only can be used as parameters to inline functions
* The type information is reduced to runtime-available reification (meaning that there is no encoding of nested type parameters ala `List<String>` as opposed to `List<*>`)
* Primitive types are translated into their boxed forms

### Behind the Scenes

These restrictions may seem odd, but it boils down to the complexity of type information and the desire to avoid creating hidden/synthetic carrier types and fields.

Since this is restricted to inline functions, the Kotlin compiler can simply compile the function such that every place where `T` is referenced the literal type from the use-site is put in its place in the code. In this way, inline functions provide a way to do "use-site type specialization" in the code because every place the code is "inlined" the type information is simply encoded in the inlined code, instead of trying to build a type parameterization solution.

So in the code above, the raw Java bytecode translation would look like this were it in Java source (or was decompiled):

```java
// ... use:
val result = MyThing.create<String>()
result.printType()
// --> in Java code:
MyThing<String> result = new MyThing<String>(String.class);
result.printType();
```

Because it is inline, the `create` method is entirely invisible to the actual bytecode; it's purely there as a code organization tool.

The other major advantage to forcing this only on inline functions is it effectively punts any question of Java type interoperability. An area where Scala often creates unpleasant edges when it comes to Java interop is with respect to the sheer number of synthetics and hidden encoded details the actual Java classes it generates has.

Kotlin goes to great lengths, on the other hand, to avoid significant bytecode magic, and this is another exmaple of that.

### References:

* [Inline and Reified Functions](https://kotlinlang.org/docs/reference/inline-functions.html)
* [Reified Types Spec](https://github.com/JetBrains/kotlin/blob/master/spec-docs/reified-type-parameters.md)
