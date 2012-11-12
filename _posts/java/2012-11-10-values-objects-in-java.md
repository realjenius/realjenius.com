---
title: Value Objects Proposal in Java with JEP 169
summary: 'A new enhancement proposal is in for Java that describes creating value objects, allowing the JVM to treat complex objects as primitives.'
category: journal
tags: jeps jep-169 primitives java
layout: post
---

There have been a lot of [interesting JEPs](http://openjdk.java.net/jeps/0) proposed recently for Java. With the pending onslaught of [Lambdas](http://openjdk.java.net/projects/lambda/), and with them, an entirely new programming model, there is a whole new set of enhancements and opportunities for both the language and the JVM to expand.

One of the recently announced JEPs is [JEP 169 - Value Objects](http://openjdk.java.net/jeps/169). Value objects are a number of tools for expicitly declaring that a Java object represents a raw set of vectorize-able values, rather than a composite set of memory values linked via pointers, and dynamically allocated on the heap.

As Java programmers, most of us are comfortable with (even if not entirely happy with) the distinction between primitives and objects. It's one of those ugly, very un-academic pragmatics that you deal with because of the benefits it brings. Unfortunately, as the aforementioned JEP mentions, it's also an ugly dividing line. You find yourself choosing between very low-level and hard-to-manage data structures and value types, or choosing more manageable high-level types, sacrificing the performance gains you would see otherwise:

> In modern JVMs, object allocation is inexpensive, with a cost comparable to out-of-line procedure calling. But even this cost is often a painful overhead when compared to individual operations on primitive values. Thus, Java programmers face a binary choice between existing primitive types (which avoid allocation) and other types (which allow data abstraction and other benefits of classes). When they need to define small composite values such as complex numbers, pixels, or pairs of return values, neither approach serves. This dilemma often has no good solution, and the workarounds distort Java programs and APIs. Consider, for example, the lack of a good complex number type for those who program numeric algorithms in Java.

The general idea is allow programmers to give the JVM more knowledge about objects that are already designed to be immutable and simply represent a value, so that it can be treated in a more efficient way; much like primitives.

The example that is used repeatedly in the JEP is a class that represents a Complex number; call it `java.lang.Complex`. As you may or may not know, Java today doesn't have a complex number value type. There are a number of open-source and academic examples of creating one, with the commonality between them being that they are written as a Java object. Usually, they are represented as a couple `double` values internally, and they have a numbef of methods for doing things like adding, subtracting, dividing, calculating cosine, and so forth. [Apache Commons Math](http://commons.apache.org/math/userguide/complex.html) has a perfect example.

However, if you think about it, there is really no reason this Complex value has a full heap allocated object. It's really just a couple primitives that are bound together inextricably. But the only way to do this with pure primitives would be to juggle multiple primitive values manually, and that's not exactly a pretty API: hard to explain to implementors, and very hard to add functionality around. Sadly, a lot of APIs that have to deal with quickly shuffling numeric values (such as rendering APIs) fall into the trap of having to deal with buckets o' numbers.

This JEP is an attempt at tackling this problem. Once an object has been declared a value object, it can now be scalarized by the VM into a compact binary representation that can be propagated on the stack, and can even be interned in memory. In other words, the VM can treat the Complex class exactly like two `double` values held in a single memory vector; but the developer code can still handle it like an object, with all of the encapsulation and behavioral binding that implies.

Because the value object is being treated like a primitive, it now has no surrogate identity (no pointer location), and as such, it can't be used for operations like synchronization and reference equality checks.

## Boxing Benefits

One of the interesting notes about this proposal is the fact that it changes the cost and complexity of boxing and unboxing of values from primitive types and back. Consider this contrived example:

{% highlight java %}
public void someMethod() {
	int val1 = 5;
	int val2 = 10;
	int result = sumofsquares(a, b));
	// ...
}

public Integer sumofsquares(Integer a, Integer b) {
	return square(a) + square(b);
}

public Integer square(Integer val) {
	int val = val.intValue();
	return val * val;
}
{% endhighlight %}

In this case, we have two primitive values in `someMethod()`, but immediately pass them into a method that expects boxed types. As such, the compiler is going to secretly inject code like this: `sumofsquares(Integer.valueOf(val1), Integer.valueOf(val2))`.

What this new value objects support would allow the compiler and runtime to do is defer any of boxing all the way until some complex behavior is invoked on the type - in this case that would be the `intValue()` invocation within the `square` method. However, since the compiler can determine it's simply calling a method that returns the value component contained there-in, it can actually completely eliminate the boxing altogether.

The method tree then turns around and returns a boxed value again, but the compiler and runtime can again eliminate any boxing, as the method tree never deals with them in a complex way.

Therefore, somewhat surprisingly, the boxing to a reference style `java.lang.Integer` above can in fact be eliminated completely. The other exciting benefit is that your own value types can be treated this same way, even though (unlike `int` and `Integer`) there is no native primitive value directly representing your type. This opens a whole new opportunity for expressive and clean APIs that have to deal with primitives, closing many of the difficult "performance vs. readable" problems Java developers have faced.

## Class vs Object

Several months ago I watched a presentation from Brian Goetz (can't find the specific talk) where he discussed the potential for value objects in Java's future, and at the time the thought was that it could be a new keyword or annotation on the type that indicated it was a value type. This proposal, however, discusses using a per-instance locking mechanism similar to:

{% highlight java %}
Integer x = new Integer(123456);
// Not yet a value object...
x = x.lockPermanently();
// Now a value object...
{% endhighlight %}

There are some benefits to this manual locking:
* You can use some objects as reference types; if you're synchronizing on Integer somewhere in your code (Why?) this would allow you to continue to do so.
* Initialization of objects that will eventually be value types can be done in multiple steps.
* Even if your code (or some library you use) doesn't mark the object as a value type explicitly, the JIT can still analyze the usage of the object, and transliterate it to a value type internally as a form of runtime optimization.

This idea of value objects becomes a huge benefit when exploring expressive sophisticated functional programming models. Since one of the main tenants of functional programming is immutable types - if Java can in turn represent those immutable types as primitive values, it helps ensure that functional programming models don't pay a penalty for their expressivity and immutability.