---
title: "Typescript for Java Developers: De-Structuring of Variables"
date: 2018-08-15
tags: ["typescript","javascript"]
---

ECMAScript is all about wrangling loosely typed variables. In a compiled and strongly typed language like Java, classes have a fixed shape (aka schema) and every object follows that pattern strictly. With ECMAScript, objects are more "ad-hoc" in that any single object be comprised of any combination of properties. Prototypes help with pre-defining that combination, but you can muck around with it all you want.

Consequently, there is an opportunity for language features around lifting, shifting, filtering, and moving around properties from objects. In modern ECMAScript and Typescript, a lot of benefits come from the magic of de-structuring, which allows for grabbing exactly what you need from an object, and nothing more.

<!-- more -->

De-structuring is, generally speaking, a foreign concept to Java developers. Java, as a language has avoided anything that adds ambiguity or read-time interpretation in favor of write verbosity and explicit behaviorial understanding; in other words, Java is easy to read, not easy to write. This balance has started to shift in the wake of so many expressive languages putting pressure on Java (including Scala and Kotlin), but I digress.

Though de-structuring is a foreign concept, it isn't fundamentally hard to understand. The idea of a de-structuring declaration is to simply pull out multiple components of an object into local variables, with a single expression. For example, consider this Java object:

```java
public class Person {
  int age;
  String name;
  // ... other properties here...

  // Getters, setters, etc.
}
// In a for loop we can do this:

List<Person> people = /*...*/;
for(Person person : people) {
  int age = person.getAge();
  String name = person.getName();
  System.out.printf("Person: %s with age %s%n", name, age);
}
```
Wouldn't it be nice to be able to do this instead?

```java
List<Person> people = /*...*/;
for((age, name) : people) {
  System.out.printf("Person: %s with age %s%n", name, age);
}
```

That is de-structuring: Lifting a couple variables by convention (or order, or name) out of the target object without having to explicitly declare variables and walk through a bunch of convention and assignments.

Kotlin actually has (what I will call) ["typed" de-structuring-declarations](https://kotlinlang.org/docs/reference/multi-declarations.html), but that's a subject for another day.

TypeScript (and the underlying modern ECMAScript) has two forms of de-structuring: iterable (positional) de-structuring and object de-structuring. Let's take a look at both.

# Iterable De-structuring

There are a lot of forms of iterable de-structuring in Typescript, all of which help with being able to grab the most likely position candidates. For example:

```javascript
let array = [1, 2];
let [first, second] = array;
// first = 1, second = 2
```

There are multiple ways in which this can be used - here is a short sample of the major approaches:

```javascript
let array = [1,2,3,4,5,6];
// Get the third element, ignoring the first two
let [,,third] = array; // third = 3

// Get the first element, and then get the remaining elements as a sub-array
let [first,...rest] = array; // first = 1, rest = [2,3,4,5,6]

// Perform nested iterable lookups
let nestedArray = ["test", [["deep"], "nesting"]];
let [alpha, [[beta]]] = nestedArray; // alpha = "test", beta = "deep"
```

Whenever a positional de-structuring falls out of bounds, the variable will be undefined. This allows you to define de-structuring without worry about index bounds due to variable input types.

# Object De-structuring

Object de-structuring takes properties of a TypeScript object and binds them to local variables, but the binding is done by name, rather than by position.

Consider this example similar to the Java case above:

```javascript
let person = { age: 13, name: "Bobby Tables" };

let { age, name } = person;
// age = 13, name = "Bobby Tables"
```

As with the iterable de-structuring, object de-structuring supports all kinds of variances for individual use-cases. For example:

```javascript
// assign different local variable names to the properties
let { age: someAge, name: someName } = person;
// someAge = 13, someName = Bobby Tables

// Define default values for properties that might not exist
let { age, name, gender = "Male" } = person;
// age = 13, name = Bobby Tables, gender = Male
```

Object de-structuring also works when declaring functions. In plain ECMAScript this has the interesting effect of letting you define a bag of desired named properties for options style declarations. For example:

```javascript
function connect({ ttl = 300, host = "localhost", port = 8080 } = {}) {
  /* ... */
}

connect(); // connects to localhost:8080 with a TTL of 300
connect({ ttl: 6000, port: 9000 }); // connects to localhost:9000 with a TTL of 6000
```

There is a couple of things to unpack here:

1. First, the function variable itself is defaulted via the use of `= {}`. This ensures that in the event the caller doesn't pass anything, this function will default to an empty object.
1. Second, once the object question has been resolved, every field that is de-structured (ttl, host, and port) has a default in case that value is not passed by the caller.

In TypeScript we also have type declarations as a benefit, which allows for more clarity to API users in particular. For example:

```javascript
interface ConnectConfig {
  ttl?: number,
  port?: number,
  host?: string
}

function connect({ ttl = 300, host = "localhost", port = 8080}: ConnectConfig = {}) {
  /* ... */
}
```

As with plain JS, the same rules apply, but now TypeScript can help us know if the shape of the object matches.

# Spread

Spreading is, in a sense, the opposite of de-structuring. The idea of a spread is to take multiple values out of one object or array and assign them to a single target.

We've already seen this in the array example above where we took the "rest" of the positions as a sub-array with a given name. This can also be used in other cases, such as creating an object with an override for a specific value. For example, consider a case where you wish to accept a ConnectConfig but always want the connection on port 443:

```javascript
function ttlConnect(config: ConnectConfig) {
  let ttlConfig = {...config, port = 443 };
  /* ... */
}
```

In this case, all properties of `config` will be copied to `ttlConfig`, but no matter what is in the "config" object for `port`, the value assigned manually as 443 will be used, as processing is done left-to-right.
