---
title: Kotlin Logging Without the Fuss
date: 2017-08-31
tags: ["kotlin","java","logging"]
---
One of Kotlin's strengths is that generally speaking, the code you might write in Java is generally more compact in Kotlin without losing any of the readability, functionality, or performance.

An odd case where that doesn't prove to be true is declaring loggers as Java developers.

<!--more-->

# A Background on Kotlin and statics
Java makes static a first-class feature of any particular class, where-as Kotlin rejects the idea of statics altogether, and instead gives you the concept of a singleton companion in [Companion Objects](https://kotlinlang.org/docs/reference/object-declarations.html).

Companion objects do *behave* like singletons in the language - for example (borrowing from the Kotlin docs), you can write this:

```java
class MyClass {
    companion object Factory {
        fun create(): MyClass = MyClass()
    }
}

fun main(args: Array<String>) {
  // reference companion methods in a static way
  val myClass = MyClass.create()
}
```

As you can see this is visually analogous to this Java code:

```java
public class MyClass {
  // static method
  public static MyClass create() {
    return new MyClass();
  }
}

public class SomeClass {
  public static void main(String[] args) {
    MyClass myClass = MyClass.create();
  }
}
```

However, under the covers this is not precisely what Kotlin does. Instead it does this:

```java
public class MyClass {
  public static final Factory COMPANION = new Factory();

  public static final class Factory {
    public MyClass create() {
      return new MyClass();
    }
  }
}

public class SomeClass {
  public static void main(String[] args) {
    MyClass myClass = MyClass.COMPANION.create();
  }
}
```

(*Note*: You can use annotations to expose companion methods as static Java methods, but that's outside the scope of this article).

This means that the companion object can in fact have state.

# So, What's Wrong with That?

Generally speaking, nothing - it's a great way to isolate the limitations and problems of statics without losing the convenience. However, declaring a logger as Java developers are used to is arguably *more* syntax than Java; a rare thing for Kotlin:

```java
class MyClass {
  companion object {
    private val logger = LoggerFactory.getLogger(javaClassName<MyClass>())
  }

  fun someMethod() {
    logger.debug("Hello")
  }
}
```

Compare this to the Java version:

```java
public class MyClass {
  private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

  public void someMethod() {
    logger.debug("Hello");
  }
}
```

Now, you can of course do this instead:

```java
class MyClass {
  private val logger = LoggerFactory.getLogger(MyClass.class)  
}
```

However, that is actually creating a logger *per instance* of the class, which, however optimized SLF4J might be, has an impact. Especially since SLF4J Logger handles are designed to be thread-safe and reused.

So, what is the solution?

# Kotlin Logging

The [Kotlin Logging](https://github.com/MicroUtils/kotlin-logging) framework has created a tidy package that uses the best features of Kotlin to make logging concise and tidy. Here's a walkthrough:

## Logger Declaration

The first major benefit is making logging clean. Here's the shortest form:

```java
class MyClass {
  companion object: KLogging()
}
```

This is creating a companion object of type `KLogging`. This is one of the interesting things that Kotlin companion objexcs support: the ability to inherit or be implemented by an entirely outside type.

KLogging has a `logger` property which allows you to do this in your code:

```java
class MyClass {
  companion object: KLogging()

  fun someMethod() {
    logger.debug("Hello")
  }
}
```

Also note that while there is no declaration of type or name, `KLogging` automatically infers the logger name based on the parent context in which it is used.

Alternatively if you really want to specify your own name you can with:

```
companion object: NamedKLogging("my-logger")
```

Now, this may seem wasteful: pulling in an external dependency just to save a couple lines and braces. However, that's not all that this library offers.

# Kotlin-Friendly Log Methods

In Java, SLF4J created the idea of a var-args based replacement strategy for loggers. This resulted in something like this:

```java
logger.debug("param 1 is {} and param 2 is {}", "first", "second");
```

This was done because when the logger is *off*, this is far more efficient than string concatenation (despite having the var-args array construction). This is because the string concatenation would happen before the method invocation, and that is a notoriously expensive memory juggling act.

However, this is actually *worse* than string concatenation when the logger is on, because it has to:

* Parse the input string and tokenize it into parts
* For each position replace an element out of the array of inputs
* Do bounds checking and under/overflow handling in the input array
* Reconstruct and emit a new string to the log handler of choice under the covers

SLF4J Does a lot to optimize this, but it's still a bit of a hack to workaround the limitations of the language while still making logging easy. Without this utility, developers always wrote this code:

```java
if(logger.isDebugEnabled()) {
  logger.debug("..." + "..." + " ...");
}
```

This is tedious, and if you forget to do it, you will pay the cost of the string construction.

Kotlin logging adds the concept of inline string construction. Combine this with Kotlin's ability to perform string interpolation with in-scope variables and you get a very effective syntax:

```java
logger.debug { "param 1 is $first and param 2 is $second" }
```

This is a function which boils down to this:

```java
if(logger.isDebugEnabled()) {
  logger.debug(new StringBuilder("param 1 is ").append(first).append(" and param 2 is ").append(second));  
}
```

This has the best of all worlds:

* It uses language features from string construction, which is ideal
* When logging is *off* this is a simple boolean check, as cheap as they come.
* When logging is *on*, this is simply string concatenation. There is no parsing, tokenization, or anything else.
