---
title: 'Is There Curry In This Dish?'
summary: 'Java 8 now has some functional concepts. How functional can it really get, though?'
layout: post
category: journal
tags: java java8 lambdas currying
---

Now that Java is dipping its toes into the waters of supporting functional programming styles, many of the tenets of functional programming come to mind, and it's worth exploring what is possible in this brave new world. Two of those features are currying and partial application. So the question is, can Java be functional?

### Currying and Partial Application - A Refresher

Currying (named after Haskell Curry) is the process of generating function invocations with multiple parameters via single-parameter functions that return follow-up functions. In a language that is purely functional, currying is the only way to handle multiple parameters, as a *pure* function only accepts one parameter, and always returns one value.

Most languages in practice have convenience support for multiple parameters (sometimes implemented via currying, like Haskell), or tuples, or both, so currying isn't a requirement, but it is a very powerful tool.

As a concrete example, we can use a language most folks understand that has some functional bits: Javascript (you may not love it, but you can probably read it!)

Javascript does not require curried functions, and doesn't have language-level knowledge of currying, but can be forced to do it since functions are first-class (it's just not nearly as pretty or "always-on" as in Haskell, for example). In Javascript we can have this function:

{% highlight javascript %}
function add(a, b) {
	return a + b;
}

add(1,2); // returns 4.
{% endhighlight %}

We can revisit this implementation as a curried version instead, and it could look like this:

{% highlight javascript %}
function add(a) {
	return function(b) {
		return a + b;
	}
}

add(1)(2); // returns 4.
{% endhighlight %}

This allows us to define, and then carry around a halfway completed function (it allows you to "specialize" the function).

{% highlight javascript %}
var addOne = add(1);
var val1 = addOne(2); // returns 3
var val2 = addOne(3); // returns 4.
{% endhighlight %}

What the `add(1)` invocation has done is partially apply parameters to the underlying operation of adding `a` and `b`.

This leads us to the idea of partial application, which is the process of taking a function that involves multiple parameters, and generating a new function where some of those parameters are fixed or built-in.

Note that you can have partial application without currying. Where currying is the process of composing a multi-parameter function chain out of many small single parameter functions (increasing the number of parameters), partial application is the process of producing a simpler and more specific function out of a more general function (reducing the number of parameters).

Let's flip our previous example on its head, and use partial application on our initial add function:

{% highlight javascript %}
function add(a, b) {
	return a + b;
}

// Partially apply this function in a new function.
var addOne = function(b) {
	return add(1, b);
};

var val1 = addOne(2); // returns 3.
{% endhighlight %}

Now, we've taken our add function, applied one of the parameters and passed through the other parameter, converting a two-parameter function into a one-parameter function that is more specialized for the task.

### So What About Java 8?

There are many arguments that can be made in this space regarding Java's ability for partial application and currying, and much of it depends on where you are coming from to make your arguments.

Academically speaking, Java unequivocally does not have currying or partial application, simply because Java 8 still does not have first class functions. What Java 8 has are compiler-synthetic lambdas that are generated (via fancy `invokedynamic` static method lambda meta factory generation trickery) into functional interface objects.

So really, when you get down to the core of things, you're still dealing with objects, and anything you can do with objects hasn't really changed, other than a lot of syntactic sugar and some performance benefits as compared to spinning a bunch of anonymous inner classes.

**However**, from a practical standpoint, the answer is more like "maybe" or "kind-of".

Keep in mind that at the core of Java's lambda support is the translation of a lambda into a functional interface. Assuming you know the functional interfaces you're dealing with (such as the standard `java.util.function` interfaces), then you can create some reusable utilities that can help. For example, consider partial application using a `BiFunction` example. We can partially apply parameters by hand:

{% highlight java %}
BiFunction<Integer,Integer,Integer> adder = (a,b) -> a + b;
Function<Integer,Integer> addOne = (b) -> adder.apply(1, b);
{% endhighlight %}

All we've effectively done here is create an adapter object around the initial functional object; but in practice it is indeed partial application. You could also make this partial application a utility:

{% highlight java %}
public static <A,B,R> Function<B,R> partial(BiFunction<A,B,R> func, A aVal) {
	return (b) -> return func.apply(aVal, b);
}

BiFunction<Integer,Integer,Integer> adder = (a,b) -> a + b;
Function<Integer,Integer> addOne = partial(adder, 1);
{% endhighlight %}

From this viewpoint, currying is also "practically" possible, although the verbose type signatures can make it less than ideal, as the curried nature of the value is quite explicit in the typing. Consider our curried add:

{% highlight java %}
Function<Integer,Function<Integer,Integer>> add = (a) -> (b) -> a + b;
{% endhighlight %}

Here we've defined a curried add function. We can partially apply as before:

{% highlight java %}
Function<Integer,Integer> addOne = add.apply(1);
addOne.apply(2); // returns 3
addOne.apply(3); // returns 4
{% endhighlight %}

### The Problems with Generalizing

One of the strengths of true functional currying and partial application is that it can be done in a very general way. Since functions are a core "currency" of the language, you can effectively take any function and curry it into a decomposed chain of functions, allowing you to pass those parts around. Ideally in Java, you could take a three parameter function and turn it into a 3-deep single parameter function chain, but, in practice it's a lot more sketchy.

It's worth considering that in Java a "function" is really defined via the implementation of a particular interface (with Java 8 shipping specific "standard" implementations), and despite having a standard library, it's also meant to bridge the compatibility gap with existing libraries that existed before Java 8, so you can have a lambda become an implementation of any SAM interface (single abstract method).

Once you bake a lambda into one of these interface implementations, you kind of lose the "general" nature of it, and it becomes a specific (and opaque) thing; even if that thing is semantically identical to another thing.

This is quite relevant looking at Google Guava. Guava ships a small suite of functional programming tools, and is very common in many Java projects active today. Most of these functional APIs are superceded by Java 8, but replacing them will be a gradual process for a lot of teams.

For example, Guava has a `Function` interface. While there are some extra bits in Java 8, at its core, the Google and Java `Function` interfaces are of parity:

{% highlight java %}
// Guava
public interface Function<F,T> {
	T apply(F param);
}

// Java 8
public interface Function<F,T> {
	T apply(F param);
}
{% endhighlight %}

Now - consider this Guava-interfacing code on Java 8 (using Guava's Iterables utility class):

{% highlight java %}
List<Integer> ints = Arrays.asList(1,2,3);
Iterable<String> strings = Iterables.transform(ints, Integer::toString);
{% endhighlight %}

Internally, what Java has actually done here (Java-7-style) can be thought of like this:

{% highlight java %}
List<Integer> ints = Arrays.asList(1,2,3);
Iterable<String> strings = Iterables.transform(ints, new com.google.common.base.Function<Integer,String>() {
  public String apply(Integer x) {
    return x.toString();
  }
});
{% endhighlight %}

You'll get a compile time error if you try and use a standard type intermediately (I've left explicit packages in the code for clarity):

{% highlight java %}
java.util.function.Function<Integer,String> func = Integer::toString;
com.google.common.base.Function<Integer,String> googFunc = Integer::toString;

// OK:
Iterable<String> strings = Iterables.transform(ints, googFunc);
// Compile-time error:
strings = Iterables.transform(ints, func);
{% endhighlight %}

Thankfully, this mismatch can easily be resolved in code by declaring the lambda as the correct type as I illustrated, or by adapting it through using something like a method reference:

{% highlight java %}
// Construct (or receive) the wrong type
java.util.function.Function<Integer,String> func = Integer::toString;
// Use a method reference to adapt the wrong type into the right type via a lambda.
Iterable<String> strings = Iterables.transform(ints, func::apply);
{% endhighlight %}

Here we're actually creating a new functional wrapper of the "google" type around our core Java function using method references as the short-hand to do so.

However, because functions and their accepted parameters and type declarations are not a "universal" type in the language (at the same ubiquitous level as `Object`), there will unfortunately always be impedence mismatches of the sort seen above. This sort of friction that exists is practically easily to resolve in code as shown above, but will have a tendency to cause library log-jams as you try to use techniques like partial application and currying.

Concretely speaking, what happens when you want a three-parameter function? You could of course create your own interface for it:

{% highlight java %}
public interface TriFunction<A,B,C,D> {
	public D apply(A a, B b, C c);
}

TriFunction<String,Integer,String,String> wat = (a,b,c) -> a + String.valueOf(b) + c;
String result = wat.apply("test",5,"testagain"); // test5testagain
{% endhighlight %}

But this will not be recognized by other libraries. Or you could get fancy with currying:

{% highlight java %}
Function<String, Function<Integer, Function<String,String>>> spicy = 
	(a) -> (b) -> (c) -> a + String.valueOf(b) + c;
String result = spicy.apply("test").apply(5).apply("testagain");
{% endhighlight %}

Unfortunately, this isn't what I'd call idiomatic or friendly to most developers (Java or no), and still runs the issue that adapting through libraries will be bumpy depending on what they chose.

### Summary

In-short, Java has the ability to generate, compose, and transform functional objects into different shapes in a far more concise syntax than before, but it's still far more verbose and type-y than true functional counterparts (even Javascript as I illustrated here).

That doesn't mean that Java isn't orders of magnitude better of than it was; simply that it is still a good distance from (and probably never will be) a bastion of functional programming.