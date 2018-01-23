---
title: 'Kotlin: Reified Type Function Parameters'
date: 2017-09-08
tags: ["kotlin","java"]
---

As most Java programmers know, Java generics are _erased_ at compile-time. This has trade-offs, but the two main reasons for this are:

* Compatibility - Java 1.4 and earlier dealt exclusively in raw types at the VM level. Generics were able to compile without introducing significant new typing to the bytecode and classfile format spec, and having to deal with older classes generated without that typing.
* Simplicity - By erasing to raw types, the JVM doesn't have to understand specialization; something that has its own complexities and downsides. For example, specialized types are much more challenging to optimize with a just-in-time compiler.

However, knowing the type parameters used at runtime can have real value, and it's something Java simply doesn't offer. Kotlin would like to help in this area, but there are many challenges in doing so.

<!--more-->

Because the classfile format and runtime do not understand type parameterizers, that means that languages like Kotlin and Scala (which must eventually target the JVM classfile format) *also* must consider the consequences of erasure.

JVM Languages can choose to do things like generate specialized classes at compile time or create metadata payloads within classes and instances, but generally, Kotlin and Scala will erase their types as well to boil down to raw classes because they want to maximize opportunity for Java language interoperability. Kotlin's specification is generally pretty clear about how it translates to Java classes (aka how it "interops"), and most of it is fairly unsurprising. Having a bunch of hidden variables in the code to reflect the types used at a particular point in time would make Java interop awful.

However, sometimes having "reified" type information (resolved type parameter metadata) at runtime is extremely valuable; this is something that Java simply doesn't have, and consequently you will see methods in Java that look like this:

```java

// Some type that is parameterized but also needs to know the type at runtime.
public class MyThing<T> { // compile time type.

  // Factory method *must* take the runtime type if it is to be used at runtime.
  public static MyThing<T> create(Class<T> type) {
    return new MyThing<T>(type);
  }

  private Class<T> type; // Capture of type at runtime.

  // Required type via constructor.
  public MyThing(Class<T> type) {
    this.type = type;
  }

  public void printType() {
    // Runtime use of that type.
    System.out.println(type);
  }
}

// ... use:

MyThing<String> result = MyThing.create(String.class);
result.printType();
```

You often see this sort of pattern in factories, database libraries, and serializer/deserializer frameworks; places where the framework uses parameters for organizing instances of a tool, but the underlying tool must also *know* the type at runtime to be able to walk the fields or similar RTTI operations (e.g. `Dao<Person>` vs `Dao<BankAccount>`).

In Kotlin we can model this same class and same factory method, effectively just porting the Java implementation naively:

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

Note that we refer to `T` in the `create` method, as if it was a fully resolved type literal. Specifically you can use:

* `is` comparators (`x is T`, `x !is T`)
* `as` casts (`x as T`, `x as? T`)
* Reflection literals like `::class`

This functionality has some limitations, however:

* It only can be used as parameters to inline functions
* The type information is reduced to runtime-available reification (meaning that there is no encoding of nested type parameters ala `List<String>` as opposed to `List<*>`)
* Primitive types are translated into their boxed forms

### Behind the Scenes

These restrictions may seem odd, but the reason is quite clever.

By restricting this feature to inline functions, the Kotlin compiler can simply compile the function such that every place where `T` is referenced the literal type from the use-site is put in its place in the code. In this way, inline functions provide a way to do "use-site type specialization" in the code because every place the code is "inlined" the type information is simply encoded in the inlined code, instead of trying to build a type parameterization solution.

So in the code above, the raw Java bytecode translation would look like this were it in Java source (or was decompiled):

```java
// Kotlin:
val result = MyThing.create<String>()
result.printType()
// --> in Java code:
MyThing<String> result = new MyThing<String>(String.class);
result.printType();
```

Because it is inline, this function is entirely invisible to the actual bytecode; it's purely there as a code organization tool.

The other major advantage to forcing this only on inline functions is it effectively punts any question of Java type interoperability. An area where Scala often must create unpleasant edges when it comes to the generated Java classes (and in turn impacting Java interop) is with respect to the sheer number of synthetics and hidden encoded details hidden in the actual Java classes it can generate. Kotlin is able to avoid this by not diverging as far as Scala when it comes to the power of the type system (and other areas).

You can imagine if you had to do this with some sort of non-inline function it would require hidden type parameters in the code. Something like this:

```java
public static MyThing create(Class __superSecretHiddenType) {
  return new MyThing(__superSecretHiddenType);
}
```

And, now you can also see why Java interop would suffer, as this method would exist, but it has unexpected type parameters. Instead, inline functions simply don't exist.

For another example, let's look at this Kotlin sample:

```java
fun main(args: Array<String>) {
    myFun<String>("test")
    myFun<String>(123)
}

inline fun <reified T> myFun(any: Any) {
    if(any is T) {
        println("$any matches ${T::class}")
    } else {
        println("$any does not match ${T::class}")
    }
}
```

If you were to run this, it would print this:

```java
test matches class kotlin.String
123 does not match class kotlin.String
```

By looking at the corresponding bytecode, we can see the result of this function were it written in Java is effectively this:

```java
// I've taken some liberties by naming some ldcs for readability.
public static void main(String[] args) {
  String c1 = "test";
    String str = new StringBuilder(c1)
      .append(" matches ")
      .append(kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(java.lang.String));
    System.out.println(str);


  Integer c2 = Integer.valueOf(123);

  if(c2 instanceof java.lang.String) {
    String str = new StringBuilder(c2)
      .append(" matches ")
      .append(kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(java.lang.String));
    System.out.println(str);
  } else {
    String str = new StringBuilder(c2)
      .append(" does not match ")
      .append(kotlin.jvm.internal.Reflection.getOrCreateClass(java.lang.Integer.Class));
    System.out.println(str);
  }
}
```

Wow! What happened here? Well it turns out that the compilation process was able to tell that the first condition (is a String a String) would always result in true, so it eliminated the branch entirely before emitting the code. This is another case where the compiler can take liberties when emitting the inline function code repeatedly, as the scope of the logic is isolated to that particular invocation. Were this an *actual* function then you would have to rely on the JIT to perform this sort of optimization at runtime after warming up.

### References:

* [Inline and Reified Functions](https://kotlinlang.org/docs/reference/inline-functions.html)
* [Reified Types Spec](https://github.com/JetBrains/kotlin/blob/master/spec-docs/reified-type-parameters.md)
