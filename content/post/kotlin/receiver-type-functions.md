---
title: Kotlin Receiver Types on Functions
date: 2018-01-22
tags: ["kotlin","java","functions"]
draft: true
---
One of the neat features of Kotlin that is not immediately obvious is the ability to change the receiver type of functions.

For example, consider that you want to do something to every element in an array using a lambda. You may initially want to do it this way:

```java
data class Person(var firstName: String, var middleName: String, var lastName: String) {
  fun fullName() = "$firstName $middleName $lastName"
}

val things: List<MyThing> = // ... (from somewhere)...

things.forEach { person ->
  person.firstName = person.firstName.toUpperCase()
  person.middleName = person.middleName.toUpperCase()
  person.lastName = person.lastName.toUpperCase()
  println(person.fullName())
}
```

Of course, with Kotlin we don't need to define our input variables for single-argument lambdas; we can shorten this by using the shortcut parameter for a lambda: `it`:

```java
things.forEach {
  it.firstName = it.firstName.toUpperCase()
  it.middleName = it.middleName.toUpperCase()
  it.lastName = it.lastName.toUpperCase()
  println(it.fullName())
}
```

Of course, we can also use receiver types. All objects are automatically given an `apply` function as part of the Kotlin core libraries, which treats "this" in the function as the object on which it was called, which allows us to try something like this as well:

```java
things.forEach {
  println(it.apply {
    firstName = firstName.toUpperCase()
    middleName = middleName.toUpperCase()
    lastName = lastName.toUpperCase()
  }.fullName())
}
```

So, what is happening here? This apply function appears to make it so we can work on `it` as part of the class itself, and we can work on the object after changing things on it. This is using receiver types to make the focus of "this" on a lambda be an object of our choosing, instead of the surrounding code of the lambda.

The definition of this function is bizarre to look at initially:

```java
public inline fun <T> T.apply(block: T.() -> Unit): T { block(); return this }
```

This line contains quite a few details in it:

# I'm an inline function (`public inline fun`)
# I work against any parameterized type "T" (`<T>`)
# I'm bound as an extension function on that parameterized type (`T.apply`) so I can be called as if I belong to that type -- This is the same way you can add "methods" to String, for example
# I take a function (named `block`) that is bound to the receiver type T (`T.()`) and returns nothing `-> Unit`)
# I return `T` (`: T`) - This allows apply to be used fluently.
# As an implementation, I invoke the passed in function on my "this" value, and then I return "this" (`block(); return this`)

So, a lot of disparate parts are working together here. Let's focus in particular on the part that says `T.()`.

Normally with any function, the "this" type is treated as a reference to the enclosing object that constructed the lambda. So, for example, in this case, you can see that "this" refers to the service object that is iterating over the list:

```java
class MyService(private val stringToAppend) {
  fun appendText(words: List<String>) =
    words.map { "$it--${this.stringToAppend}" }
}

val myService = MyService("test")
val list = myService.appendText(arrayListOf("A", "B", "C"))
// list = ["A--test", "B--test", "C--test"]

```

The lambda we passed into `words` is allowed to refer to variables in our instance of `MyService`.

In this usage model, `map` in the Kotlin collections API is simply taking a standard lambda of the form `(T) -> R` where the function takes an argument of a particular type, returns a different type, and is allowed to keep the choice of what it considers "this". Consequently, Kotlin makes the instance of `MyService` for which the lambda was constructed "this", and as a result, the map function can find `this.stringToAppend`. Here is a rough representation of the `map` method declaration:

```
public inline fun <T,R> List<T>.map(transform: (T) -> R): List<R> { ... }
```

**For what it's worth, the real `map` function is bound to iterable (rather than List) and uses a lot more intelligence internally than I did here; this is a simplified illustration.**

This is simply saying:
# Add an extension function called `map` to the `List` class
# The map function takes a lambda which transforms from `T` to `R`
# After completing, it returns a `List<R>` (which we know is a copy of the list with the elements all transformed).


We can create our own function called `pureMap`, and in it adjust the definition very slightly to instead bind the function to the parameterized type T:

```
public inline fun <T,R> List<T>.pureMap(transform: T.() -> R): List<R> {
  // iterate over List
  // for each element:
  val elem: T = // ...
  val mapped: R = transform(elem)
  // create alt list of same size as original
}
```

Note that we call it *exactly* the same way. That is because, in reality, receiver types are really just syntactic sugar to be able to say that "this" is allowed to be an alias for the first parameter of the function. As a result our `pureMap` function can actually be a straight delegate to `map` itself:

```
public inline fun <T,R> List<T>.pureMap(transform: T.() -> R) = map(transform)
```
