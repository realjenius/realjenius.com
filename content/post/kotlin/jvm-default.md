---
title: "What's the Deal with @JvmDefault?"
date: 2018-06-29
tags: ["kotlin","java","jvm"]
---

Kotlin has an annotation called `@JvmDefault` which, like most of the "Jvm" prefixed annotations, exists to help massage the Kotlin compiler output to match Java classes in a certain way. This annotation was added in 1.2.40, and has now seen some experimental enhancements in 1.2.50, so it seems worth exploring what this is all about.

<!--more-->

Out of the box, Kotlin supports adding non-abstract methods to interfaces. In other words, you can write this in Kotlin:

```java
interface Dog {
  fun speak() = println("Woof!")
}

class Collie : Dog

fun main(args: Array<String>) {
  Collie().speak()
}
```

This works even when running on Java 7, a version of Java before default interface methods.

Kotlin achieves this by generating a sidecar class to the interface to help facilitate these magic interface methods. To illustrate this, if you were to write what Kotlin generates for you using Java code, it would look something like this:

```java
public interface Dog {
  public void speak();

  public static class DefaultImpls {
    public static void speak(Dog instance) {
      System.out.println("Woof!");
    }
  }
}

public class Collie extends Dog {
  public void speak() {
    Dog.DefaultImpls.speak(this);
  }
}
```

To summarize, Kotlin creates a static inner class called `DefaultImpls` that exists to store the default implementations of methods, and those methods are all static, and take "Self" receiver types to emulate the methods belonging to objects. Then, for every type extending that interface, if the type doesn't implement the method itself, upon compiling, Kotlin will wire the method up to the default implementation via invocation.

This is great, because it gives the power of concrete methods on interfaces even on JVMs prior to Java 8. However, it has two major downsides:

1. It is incompatible with the modern Java way of doing things, so interoperability is messy. You can manually write Java classes that invoke the method (as I have done here) but it's a magic implementation detail.
1. One of the main reasons for default methods existence in Java 8 was to be able to add methods to an interface without having to touch every subclass (such as the addition of `Collection.stream()`). The Kotlin implementation doesn't support this under the covers, because the default invocation must be generated on every concrete type. Adding a new method to the interface results in having to recompile every implementor.

*Side Note*: Incidentally, if you are building Kotlin APIs, this is a good reason not to use these methods to grow your APIs unless you explicitly plan to target Java 8 and use the JvmDefault annotation.

If you are running Java 8 and would rather Kotlin use default interface methods ala Java, you can now with the `@JvmDefault` annotation. Note that (as of Kotlin 1.2.50) if you decide to use this annotation you must also specify the `-Xjvm-default` flag to the compiler - this is disabled by default and is still experimental. The annotation will be a compiler-time-error without the flag.

To understand what this does, consider if you had this interface and you also specified `-Xjvm-default=enable`:

```java
interface Dog {
  @JvmDefault
  fun speak() = println("Woof!")
}
```

Kotlin will now generate classes effectively like this Java source:

```java
public interface Dog {
  default public void speak() {
    System.out.println("Woof!");
  }
}

public class Collie extends Dog {
  // No method here.
}
```

This now allows you to gain all the benefits of Java 8 default methods from within Kotlin.

Note that, [in addition to changing the compiler flag, with Kotlin 1.2.50 they added a compatibility mode](https://blog.jetbrains.com/kotlin/2018/06/kotlin-1-2-50-is-out/). The compatibility flag (`-Xjvm-default=compatibility`) is specifically for retaining binary compatibility with existing Kotlin classes, while still being able to move to Java 8 style default methods. This flag is particularly useful when considering other projects that were generated to point to the static bridge method.

To achieve this, the Kotlin compiler uses a classfile trick with `invokespecial` to invoke the default interface method while still retaining the DefaultImpls bridge class. Here is *kind of* what this looks like:

```java
public interface Dog {
  default public void speak() {
    System.out.println("Woof!");
  }

  public static class DefaultImpls {
    public static void speak(Dog instance) {
      Dog.instance.speak();
    }
  }
}

public class Collie extends Dog {

}

// In some other project this already exists in compiled form:
public class Labrador extends Dog {
  public void speak() {
    Dog.DefaultImpls.speak(this);
  }
}
```

There is a good bit to unpack here, especially since this isn't valid Java syntax. Here are some notes:

* The default method is generated on the interface, as it was when we just used `enable`
* Newly compiled classes, like Collie, will use the default interface directly Java 8 style.
* Existing compiled code like Labrador will still work at the binary level, because it points to the shim'ed `DefaultImpls` class.
* The `DefaultImpls` method implementation cannot be expressed in true Java source, as it is similar to invoking `<init>` or `<super>`; the method must call the speak method on the Dog interface on the instance provided. If it simply invoked "speak()" it would result in a stack-overflow on old types (`Labrador.speak() -> DefaultImpls.speak() -> Labrador.speak()`). Instead, it must invoke the interface directly: `Labrador.speak() -> DefaultImpls.speak() -> Dog.speak()`, and that can only be done with an interface method `invokespecial` invocation.

Presumably, `@JvmDefault` will become a standard mechanism in upcoming Kotlin releases, and it is certainly an important flag to understand if you are building libraries in Kotlin today, as default methods on interfaces are a key tool for maintaining binary compatibility and growing an API.
