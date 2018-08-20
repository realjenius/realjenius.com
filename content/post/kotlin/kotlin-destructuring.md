---
title: De-Structuring in Kotlin
date: 2018-08-18
tags: ["kotlin","java"]
---
Recently I wrote an article about [de-structuring in TypeScript]({{< ref "/post/typescript/destructuring_in_typescript.md" >}}), and hinted that Kotlin has a similar feature, but in more of a "strongly typed" language style. Today I want to discuss that feature.

<!-- more -->

To re-cap from the previous article, de-structuring is, at the core, a syntactic sugar to easily "lift" parameters out of an object or array and declare local all in one shot.

With ECMAScript and TypeScript, this comes in two forms: positional (for arrays and iterables) and by name (for objects). One of the things that enables this ability in ECMAScript is the fact that all objects are, in effect, a map data-structure. Every property is accessible on an object by name; e.g: `person['name']`. Kotlin, being both a strongly-typed language as well as being built on Java, which has objects with full encapsulation rules that notably *do not act like 'maps'* has no such ability.

Consequently, to support de-structuring, Kotlin has to come up with a different solution: the `component` convention.

Given an arbitrary object, in Kotlin you can try to do this:

```java
class Person {
  var name: String = ""
  var age: Int = 0
}

fun main(args: Array<String>) {
  val people: List<Person> = /* ... */
  for((name, age) in people) { // compiler error!
    println("Name: $name age: $age")
  }
}
```

In this case, the compiler will fail with an error like: `Destructuring declaration initializer of type Person must have a 'component1()' function` (and will also repeat for `component2`).

To make it support de-structuring, we can follow the advice and define the indexed component functions. For example:

```java
class Person {
  var name: String = ""
  var age: Int = 0

  fun component1() = name
  fun component2() = age
}
```

Once these functions are defined, the code above will work as expected. It's effectively the same as this code:

```java
fun main(args: Array<String>) {
  val people: List<Person> = /* ... */
  for(person in people) {
    val name = person.component1()
    val age = person.component2()
    println("Name: $name age: $age")
  }
}
```

The name you choose for the de-structuring variables doesn't matter, it's strictly positional.

In practice, the primary benefit of de-structuring in Kotlin shows up with data classes, in which it's a free feature. Data classes provide all sorts of convention benefits including: equality and hashcode definition, toString declaration, copy functions, and de-structuring components. That means we can convert our `Person` class to a data class and get the benefits of de-structuring for free:

```java
data class Person(var name: String, var age: Int)

fun main(args: Array<String>) {
  val people: List<Person> = // ...
  for((name, age) in people) {
    println("Name: $name age: $age")
  }
}
```

Of course, built-in Kotlin types also have de-structuring components pre-declared, including:

* `Pair`
* `Triple`
* `Map.Entry`
* `List`

This means that there are a lot of "syntax shortcuts" when working with data-structures. For example, consider lists and maps:

```java
// Get the first 3 people out of the list.
val peopleList: List<Person> = /*...*/
val (person1, person2, person3) = peopleList

// Get the Key and Value of a mpa in a loop
val map: Map<UUID,Person> = // ..
for((id,person) in map) {
  // id = entry.getKey()
  // person = entry.getValue()
}
```

Of this set of examples, `List` is the one that requires some care in use.

* Unlike TypeScript (which falls back to `undefined`), if you exceed the list boundaries, the de-structuring will still trigger an `IndexOutOfBoundsException`. Therefore, de-structuring must be preceded by bounds checking in this case.
* List has a limit in the number of components available. As of Kotlin 1.2.x, List only supports `component1()` to `component5()`. Meaning if you declared the following destructuring variable, it would fail to compile: `val (p1, p2, p3, p4, p5, p6) = peopleList`

Another pattern that is common with de-sructuring is the "dual return type" pattern, where a function can return two values. Often times this is used for cases where a function returns either a success result or an error, rather than using exceptions for error flow. In these cases, a `Pair` type (or similar) is used to hold both possible result types. Anyone that has used `Go` as a language or asynchronous message passing is familiar with this approach:

```java
fun doSomething(): Pair<SomeResponse?, Error?> {
  // ...
}
```

The advantage of using a componentized type like `Pair` is that it can be de-structured at the call-site:

```java
val (response, error) = doSomething()
if(error != null)
  // handle error
else
  // do something with the response
```

De-structuring can also be used in lambda declarations. Consider a lambda that takes a Person object. That lambda can de-structure in the variable declaration:

```java
val people: List<Person> = /* ... */
people.foreach { (name, age) -> println("Name: $name age: $age") }
```

Finally, it's worth noting that when de-structuring you can ignore fields that you don't care about using underscores:

```java
val people: List<Person> = /* ... */
people.foreach { (_, age) -> println("Age: $age") }
```

Of course, being attached to a strongly typed language, de-structuring in Kotlin has some limitations compared to the feature in ECMAScript:

* There is no partitioning or spreading supported (like `(first, ...rest)`) - this is at least partially a byproduct of the fact that spreading to produce "the rest of" results in the definition of a new "type". In TypeScript this is no big deal; an object minus a few properties is still an object. But in a JVM based language, what do you call a `Person` object that doesn't have a `name` property anymore? While TypeScript can change `{ 'name': 'test', 'age': 15 }` to `{'age':15}` when excluding `name`, Kotlin needs a strict type to use for all return values, and no such type exists.
* Everything in Kotlin de-structuring is order-based; therefore ordering is part of the API contract! if you change your data class (or whatever class is providing the component functions) in a way that changes the order of properties, it will break de-structuring usages. This may seem obvious (constructor arg reordering is also contract breaking) but it may seem surprising if you always choose to use matching names in your de-structuring sites; remember: the names at the call-site are unrelated to the names of the components!
* There is no variable defaulting available - in other words you can't currently say: `(name, age = 13)` in case `age` is null.
