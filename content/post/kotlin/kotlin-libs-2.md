---
title: 'Kotlin Libraries: Comparables and Comparators'
date: 2017-08-23
series: kotlin-libraries
tags: ["kotlin","java","kotlin-libraries"]
---
This is the second article in a series around Kotlin standard library additions. This article is all about the Kotlin comparator factory functions.

<!--more-->

{{< series >}}

# Comparator Composition

For developers familiar with Java 8, you are likely also familiar with the lambda-accepting factory methods on the `Comparator` class itself:

* `Comparator::comparing(Function<? super T, ? extends U> extractor)`
* `Comparator::comparing(Function<? super T, ? extends U> extractor, Comparator<? super U> keyComparator)`
* `Comparator::comparingDouble(ToDoubleFunction<? super T>)`
* `Comparator::comparingInt(ToIntFunction<? super T>)`
* `Comparator::comparingLong(ToLongFunction<? super T>)`
* `Comparator::naturalOrder()`
* `Comparator::nullsFirst(Comparator<? super T> comparator)`
* `Comparator::nullsLast(Comparator<? super T> comparator)`
* `Comparator::reversed()`
* `Comparator::reverseOrder()`
* `Comparator::thenComparing(Comparator<? super T> comparator)`
* `Comparator::thenComparing(Function<? super T, ? extends U> extractor)`
* `Comparator::thenComparing(Function<? super T, ? extends U> extractor, Comparator<? super U> comparator)`
* `Comparator::thenComparingDouble(ToDoubleFunction<? super T> extractor)`
* `Comparator::thenComparingInt(ToIntFunction<? super T> extractor)`
* `Comparator::thenComparingLong(ToLongFunction<? super T> extractor)`

This is obviously a pretty long list, but the idea is that you can compose comparators via a series of calls, particularly if you are using static imports.

For example, consider this arbitrarily complex example:

```java
import static java.util.Comparator.*;

public class Person {
  private String name;
  private Integer age;

  public String getName() {
    return name;
  }

  public Integer getAge() {
    return age;
  }
}

// ...

// By age, reversed naturally.
Comparator<Person> byAge = comparing(Person::age, reverseOrder());

// OR (Same idea, as natural is the default order):
Comparator<Person> byAgeAlt = comparing(Person::age).reversed();

// By first name, with nulls last
Comparator<Person> byName = comparing(Person::firstName, nullsLast());

// Combine the above two:
Comparator<Person> byNameThenAge = byName.thenComparing(byAge);

```

That's just a few examples of how you can use Java 8's comparator generating methods. Kotlin has a similar set of functions, but obviously has the advantage of running on older JVMs (at least for now), and has a little more expressivity.

Assuming the given data class (which is canonically similar to the above Java class), we can propose similar solutions:

```java
data class Person(val firstName: String?, val age: Int?)

// ...

val byAge = compareByDescending(nullsFirst<Int>(), Person::age)
val byName = compareBy(nullsLast<String>(), Person::firstName)
val byNameThenAge = byName.then(byAge)

// OR with Kotlin 1.1.4, we can nuke the type declarations as the inferencer is mo-betta.
val byAge = compareByDescending(nullsFirst(), Person::age)
val byName = compareBy(nullsLast(), Person::firstName)
val byNameThenAge = byName.then(byAge)
```

Looking at this, much of this is the same idea, but just organized a little differently. A few notes:

* `nullsFirst` and `nullsLast` are required in this context. Because I (sneakily) included a subtle null bug in the Java version of the code, it shows that Kotlin *requires* you resolve your null issues at compile time. I cannot create a comparator via `compareBy` without first sorting out that the type itself is not nullable when it gets to that function. As a result, the `nulls` functions will deal with the null variant themselves, and resolve to a non-nullable comparable.
* You specify the type parameter when constructing the `nulls` operators because they come before the method references. When constructing the actual stack of execution, the evaluation happens from the "inside" to the "outside". The inside would be the right-most argument to `compareBy` (or `compareByDescending`), and everything prior to that can be considered a "decorator" to the prior.
* `then` as a function on `Comparator` itself is largely interchangable with Java 8's `thenComparing` function (though the typing is more precise for Kotlin). However, Kotlin adds more variants for more edge cases (see below).
* Reversing the order starts earlier in the context of Kotlin. In Java the idea of reversing the order comes after the `Comparator` has been constructed (though that is possible with Kotlin as well)

Some other notes in code form:

```java
// you can reverse any comparator you create, so this:
val byAgeDesc1 = compareByDescending(nullsFirst(), Person::age)
// can be done like this too (note the flip to nulls last so the whole thing reversed):
val byAge2 = compareBy(nullsLast(), Person::age).reversed()

// Simple chains of property checks can be composed varargs style:
val byNameThenAgeSimple = compareBy(Person::age, Person::firstName)

// You have the ability to chain and compose comparables too if you don't want pure simple chaining:
val byAgeReverseThenName = compareBy(Person::age).reversed().thenBy(Person::firstName)
```

# Writing compareTo Methods

Kotlin also enables you to create your own comparable methods just like you would compose comparator methods, which to this point is something that Java still leaves to you (or you end up using [Guava](https://github.com/google/guava) or similar).

This is done by the complementary `compareValues` methods, which are focused on accepting actual types; allowing you to compose the behavior when faced with real data types.

```java
data class Person(val firstName: String?, val age: Int?) : Comparable<Person> {
    override fun compareTo(other:Person) = compareValues(this.age, other.age)
}
```

This first example is pretty weak overall - this is hardly a difficult comparison to do by hand; the only advantage here is the underlying null checks it does so you don't have to. However, we can also use a selector function to find the comparison value via `compareValuesBy`:

```java
data class Person(val firstName: String?, val age: Int?) : Comparable<Person> {
    override fun compareTo(other:Person) =
        compareValuesBy(this, other, Person::age)
}
```

And we can chain those:

```java
data class Person(val firstName: String?, val age: Int?) : Comparable<Person> {
    override fun compareTo(other:Person) =
        compareValuesBy(this, other, Person::age, Person::firstName)
}
```

Finally, we can also add a comparator to drive the comparison behavior of the underlying data type, like controlling how nulls are handled, and reversing:

```java
data class Person(val firstName: String?, val age: Int?) : Comparable<Person> {
    override fun compareTo(other:Person) =
          compareValuesBy(this, other, nullsLast<Int>().reversed(), Person::age)
}
```

Note that you can even chain comparisons if you do it in the comparator chain, rather than in the selector. To do this the selector needs to return the identity of the comparison data type:

```java
data class Person(val firstName: String?, val age: Int?) : Comparable<Person> {
    override fun compareTo(other:Person) =
          compareValuesBy(
            this,
            other,
            compareBy(nullsFirst(), Person::age).thenBy(Person::firstName),
            { it }
          )
}
```

What's happening here is:

* `compareValuesBy` is accepting a selector for the value type to select as a function that returns self. `{ it }` is shorthand in this case for a function with this signature: `(Person) -> Person`; in other words just return the object you were given.
* The comparator provided expects person objects.
* When invoked, the comparator does a nulls-first age comparison, then a firstName natural order comparison.

# Helpful Utilities

The last thing I wanted to point out is the addition of the `maxOf` and `minOf` functions in 1.1. These are simple utilities that often come in handy and work for anything comparable; having them in the functional namespace is generally a handy thing.

```java
val a = 1234;
val b = 2345;

val max = maxOf(a,b)
val min = minOf(a,b)
```
