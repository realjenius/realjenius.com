---
title: Currying and Partial Application in Kotlin
date: 2017-08-24
tags: ["kotlin", "lambdas", "currying", "functional"]
---
Many moons ago, I wrote an [article on the feasibility of currying and partial application in Java 8]({{< ref "/post/java/curry-in-java8.md" >}}), and asserted that, while it was technically feasible, explicit and thorough type syntax in the language, as well as inflexible operator notation prevented it from being approachable. In comparison, is Kotlin up to the challenge?

<!--more-->

Kotlin provides a much more addressable function syntax, and also has a powerful type inferencer. As a result, currying and partial application might just be manage-able.

In the previous article I showed this example of currying using Javascript:

```javascript
// JS
function add(a) {
    return function(b) {
        return a + b;
    }
}

add(1)(2); // returns 3.
```

Here is the same thing in Kotlin:

```java
fun add(a:Int) = { b:Int -> a+b }

add(1)(2) // returns 3.
```

This is quite terse, but harbors a significant amount of information. We can look at it in a longer form so it's a little easier to grok the first time:

```java
fun add(a:Int) : (Int) -> Int {
    return { b: Int -> a + b }
}
```

So `add` is a function that returns a function that takes an int, and returns an int. Internally, that function is defined as `a + b`.

And, as with the Javascript example where we could do function specialization:

```javascript
// JS
var addOne = add(1);
var val1 = addOne(2); // returns 3
var val2 = addOne(3); // returns 4
```

So can we do this in Kotlin as well:

```java
val addOne = add(1)
val val1 = addOne(2) // returns 3
val val2 = addOne(3) // returns 4
```

I also showed partial application with Javascript and Java, which is the idea of wrapping a multi-parameter function in such a way that you codify or fix a subset of the functions to create a simpler form. Here it is in Javascript:

```javascript
function add(a, b) {
    return a + b;
}

// Partially apply this function in a new function.
var addOne = function(b) {
    return add(1, b);
};

var val1 = addOne(2); // returns 3.
```

And again, here it is in Kotlin:

```java
val addOne = { b:Int -> add(1, b) }
val val1 = addOne(2) // returns 3
```

Here, the basic "add" function is decorated with another function that fixes the first paramter to `1`, but still allows the second function to be passed through. This could easily be done as a proper function rather than an inline "lambda" in this context:

```java
fun addOne = { b:Int -> add(1,b) }
```

While this by itself does not make Kotlin a true functional language, it does open the doors to more functional patterns and techniques, as evidenced by many of Kotlins function-oriented libraries.
