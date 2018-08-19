---
title: "Typescript for Java Developers: Index Types"
date: 2018-08-16
tags: ["typescript","javascript"]
---

A fairly recent addition to Typescript is index types and the `keyof` operator. For a Java developer this is an interesting thing to learn about, as Java doesn't have this feature, specifically due to type system inflexibility.

<!-- more -->

Academically speaking, an index type is a small facet of [dependent type systems](https://en.wikipedia.org/wiki/Dependent_type) (where one type in use is dependent upon the value of another input). This is also, in effect, a way to get many of the benefits of a [heterogenous map](https://wiki.haskell.org/Heterogenous_collections). The abilities of index types and the `keyof` operator are rooted in two features:

1. The ability to declare a sealed enum of all possible property names on a type
1. The ability to look up the type of any property using an `indexed access operator` lookup based on values at runtime.

Without getting too much deeper into the academia of the feature, let's consider this Java type:

```java
public class Person {
  int age;
  String name;
  // getters, setters, etc.
}
```

It would be nice in theory to be able to create a generic builder API that works like this:

```java
public class PersonBuilder {
  private Map<String, Object> fieldValues = new HashMap<>();
  public PersonBuilder set(String field, Object value) {
    fieldValues.put(field, value);
    return this;
  }

  public Person build() { /* build from the map of values */ }
}
```

Ideally the usage would be clean:

```java
Person person = personBuilder
  .set("age", 15)
  .set("name", "Bobby Tables")
  .build();
```

However, this has a huge glaring type issue (and this is why we don't do this with builders in Java). Notably:

```java
personBuilder.set("age", "oops"); // age has to be a number!
personBuilder.set("gender", "Male"); // gender is a not a field of person!
```

This, of course, can be enforced with reflection and/or generated code, but that is a mess and not even remotely ideal (magic functionality, performance implications, etc etc etc).

This magic type dependency is what index types do: they give us a dependent type on which we can perform contextual lookups the compiler will respect. Here is a concrete example in TypeScript:

```javascript
interface Person {
  age: number
  name: string
}

type PersonOption = keyof Person; // == 'age' | 'number'
```

As you can see, keyof is nothing more than compiler supported sugar for defining a fixed type of possible string values. However, since it automatically reflects changes to the type, it is generally superior to defining this enum type manually.

This limits variable inputs to real properties on `Person`. Already this allows us to write a function that limits the string inputs to fields on the source type:

```javascript
function someFunction(input: PersonOption) { /* ... */ }

someFunction('age'); // valid
someFunction('name'); // valid
someFunction('gender'); // compiler error! not a field of Person
```

This already is a feature that Java doesn't have: the ability to restrict a dynamic string to the properties on a compiled type. However, where the real power of `keyof` arrives is the ability to do a dependent type lookup on the `Person` interface using the input value. This power comes from the other feature: the `indexed access operator`.

As a contrived example to understand indexed access operators, I could write a function like this:

```javascript
function setAge(ageValue: Person['age']) {
  /* ... */
}
```

What the `ageValue: Person['age']` type declaration says is this: "The type of `ageValue` should be whatever the type of the 'age' property of `Person` is". Of course, this is silly, we already know the type of age, so why write the function this way and not: `setAge(ageValue: number)`.

The answer to that is that `keyof` types can be used dependently within the same function so that one parameter is enforced by the other. Specifically:

```javascript
function set<T extends PersonOption>(field: T, value: Person[T]) {
  // ...
}
```

This ensures that, based on the name provided for `field`, the value parameter must match the given type of that field on the underlying object. Now we get two benefits in one: the field must be a valid field of the type, and the value must match the type of that field:

```javascript
set('age', 15); // allowed
set('age', 'test'); // compiler error in typescript: age is a number
set('gender', 'male'); // compiler error in typescript: gender is not a field of Person
```

We now get enforcement of both the allowed parameter types as well as the types of the provided value of that parameter name.

Finally, it should be noted that `keyof` can be used inline to a function, and in fact that is probably the more common usage pattern:

```javascript
function set<T,K extends keyof T>(name: K, value: T[K]) {
  // ...
}
```

This function still works as expected, but can in fact work for any type `T`, based on the parameterization in the code.
