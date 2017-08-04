---
title: 'Understanding Method References'
summary: 'Java 8 introduces lambdas, but it also introduces method references which are a very handy syntax for producing lambdas off of objects and classes. I walk through the various types here.'
tags: ["java", "java8", "lambdas", "method-references"]
date: 2013-11-26
---

Java 8 introduces four types of method references:

* Static - Converts any static method into a lambda with matching formal parameters.
* Instance-Capturing - Converts a method call on a specific object into a lambda.
* Constructor - Converts constructors into factory lambdas for a specific type.
* Arbitrary Instance - Converts a method on *all* instances of a type into a lambda that accepts instances of that type.

Since method references apply more magic behind the scenes than straight lambdas, I thought it might be good to walk through each with examples from the pre-lamdba Java world on each:

### Static

Static method references are probably the easiest to understand. For example, the method `Integer.valueOf(String)` can be converted into a `Function<String,Integer>`. Intuitively, that's exactly what it is already - a context-less method that takes a String and returns an Integer.

Using the functional interfaces in `java.util.function`, we can illustrate the various ways to build a string to integer conversion function:

```java
// The Java 7 way - wish this syntax a fond farewell!
Function<String,Integer> f1 = new Function<String,Integer>() {
  public Integer apply(String x) {
    return Integer.valueOf(x);
  }
};

// The naive (and overly verbose) Java 8 way.
Function<String,Integer> f2 = (String str) -> {
  return Integer.valueOf(str);
};

// The inference-y short-hand naive Java 8 way.
Function<String,Integer> f3 = (str) -> Integer.valueOf(str);

// The method reference Java 8 way.
Function<String,Integer> f4 = Integer::valueOf
```

### Instance-Capturing

Instance-capturing method references are, as the name implies, a way to capture instances in the enclosing type. Consider `Integer.toString`, which converts an integer instance back into a `String`:

```java
Integer x = new Integer(123);
// Create a string supplier off of the instance Java 7 style.
Supplier<String> s1 = new Supplier<String>() {
  public String get() {
    return x.toString();
  }
}

// Short-hand lambda version.
Supplier<String> s2 = () -> x.toString();

// Method reference version.
Supplier<String> f2 = x::toString
```

In effect, the lambda closes over the variable and uses it later when evaluated, and that's what the method reference is doing internally. We use a supplier in this case because we have no method parameters to `toString()`, and the instance is already known, so we can simply call the no-arg method to get the resultant value.

But what about an instance method that takes a parameter? That's no problem, so long as the underlying type that the lambda is being coerced into accepts matching parameters.

So what does that mean? Let's look at an example. `String.indexOf(String)`. is an instance method on String that accepts a string, and returns an `int`. We can translate it into a `Function` like so:

```java
String str = "abcdefg";
Function<String,Integer> indexOfFunc = str::indexOf
Integer x = indexOfFunc.apply("abc"); // returns 0.
x = indexOfFunc.apply("def"); // returns 3.
// etc.
```

However, it's worth noting that we can also go directly to primitives with Java 8, as there are primitive-friendly functional interfaces in the `java.util.function` package as well:

```java
ToIntFunction<String> indexOfFuncPrim = str::indexOf
int x = indexOfFunc.applyAsInt("abc");
```

*This avoids the boxing and type indirection, at the expense that the API is less generally reusable. There will be some interesting learning curves for API designers regarding whether to and how to properly flow primitive variants through their APIs.*

It's important to note here that the type inferencing implied by the assignment of the method reference is how Java will choose between overloaded methods as well. This is quite important for our next type of method reference.

### Constructors

Constructor references are effectively shorthand for factories of Objects. These will go along way towards getting rid of the factory-hell in Java land that has been incurred in the name of making things "pluggable" and "flexible".

```java
Supplier<List<String>> listMaker = ArrayList::new;
// Calls new ArrayList<String>();
List<String> newList = listMaker.get();

IntFunction<List<String>> sizedListMaker = ArrayList::new;
// Calls new ArrayList<String>(int) -- setting initial capacity.
List<String> newList2 = sizedListMaker.apply(5);

// Same with boxed params using standard function interface:
Function<Integer, List<String>> sizedMaker2 = ArrayList::new;
List<String> newList3 = sizedMaker2.apply(10); // boxes to Integer.valueOf(10) to call.
```

### Arbitrary-Instance

These types of method references are perhaps the most mind-bending when you first work with them, as they imply there is a magic instance somewhere they work against, however, once you understand the conversion into a lambda, they are quite straightforward.

The goal of an arbitrary instance method reference is to allow you to refer to a method on an instance that will be encountered at execution time. In the Java implementation, this means the instance will be the first argument of the lambda invocation (aka, passing in "self"), and the remainder of the parameters will be as encountered on the method itself.

Let's walk through an example using `Integer.toString()` once again. This time the method reference produces a lambda that operates on the integer instance being passed in instead of capturing one in the current context:

```java
// Java 7 implementation.
Function<Integer,String> f1 = new Function<Integer,String>() {
  public String apply(Integer x) {
    return x.toString();
  }
};
// Standard lambda.
Function<Integer,String> f2 = (x) -> x.toString();

// Method reference.
Function<Integer,String> f3 = Integer::toString;
```

So, where-as our "instance-capturing" method reference produced a method signature with no parameters (aka `Supplier's` `String get()` method), our "arbitrary instance" method reference produces a method that still returns a `String`, but also accepts the instance to operate against; in this case as a `Function` with the method signature: `String apply(Integer self)`.

Where this gets more interesting (and a little more confusing initially) is when you have methods that accept parameters. Consider our `String.indexOf(int)` case again. What does a `String::indexOf` method reference produce? If you remember the rule, the first parameter of the method signature will be our type, and the second-through-N will be the standard parameters of the method. While Java doesn't ship a whole suite of arbitrarily tupled functional interfaces, it does have a `BiFunction` that accepts two parameters:

```java
// Java 7 form.
BiFunction<String,String,Integer> bf1 = new BiFunction<String,String,Integer>() {
  public Integer apply(String self, String arg) {
    return self.indexOf(arg);
  }
};

// Java 8 standard lambda.
BiFunction<String,String,Integer> bf2 = (str,i) -> str.indexOf(i);

// Method reference:
BiFunction<String,String,Integer> bf3 = String::indexOf;
```

Admittedly, these typed signatures are long-winded, but keep in mind that generally speaking you won't see them directly; lambdas are most often declared directly at the call site. So instead you'd have something that would accept a `BiFunction` (or similar), making the syntax flow like this:

```java
factory.setIndexFinder(String::indexOf);

// Signature of this method might look like this:
public class SomethingFactory<T> {
  public void setIndexFinder(BiFunction<T,String,Integer> finder) { ... }
}
```

We can also go so far as to apply primitives in this case as well; Java does have a `ToIntBiFunction` that allows us to eliminate the boxing return value:

```java
ToIntBiFunction<String,String> bf = String::indexOf
int x = bf.applyAsInt("abc", "c"); // returns 2.
```
