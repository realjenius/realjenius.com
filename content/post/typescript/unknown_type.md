---
title: "TypeScript for Java Developers: The 'unknown' Type"
date: 2018-09-12
tags: ["typescript","javascript"]
---

TypeScript 3.0 introduced the `unknown` type, which is described as being: "Like `any`, but type-safe". This is an exploration of what that means in the Javascript and TypeScript ecosystems. and how that can be compared to strongly-typed languages like Java.

<!--more-->

In Java, since the initial release, there has been a "top" type (at least, for non-primitive values...), called `Object`. All classes in Java directly or indirectly extend from Object, and therefore, all types may be referred to as an Object (String, List, BankAccount, etc.), and there is some degree of method calls available for those types (such as `toString()`, `hashcode()`, `equals()`, etc.) Similarly, Kotlin has the `Any` type which also represents anything. Kotlin also makes a point to support primitives as children of `Any` as well, though that is largely a compiler trick (the runtime for Kotlin still treats primitives and objects separately, since it is still a JVM language, and [Value Types are not a thing yet](https://www.infoq.com/news/2018/06/JavaValuesJun18)).

# But Typescript Already Has `any`

TypeScript, on the surface, has a similar concept with `any`. As with Java, `any` may be anything. However, unlike Java, the `any` type in Typescript means "a value we don't know because it comes from a dynamic source". As a result, with `any` you can treat the variable like any Javascript value, and, as a result, call arbitrary methods on them and request arbitrary fields.

```typescript
let aPerson: any = { age: 15, name: "Bob" }
// Even though it's an 'any', the field can be accessed:
let name = aPerson.name
```

In effect, `any` reverts to JavaScript weak typing for a specific variable. Consequently, the problem with `any` is that it is too permissive and defeats the strictness of TypeScript. An `any` variable is, by its very definition treated as "anything" - you can invoke any functions or access any fields.

The goal with `unknown` is to have a type in its "least capable form", meaning the top-level and most useless type, and requiring some degree of clarification or refinement before it can be used.

# OK, but What About `object`

With TypeScript 2.2+, the `object` variable type was introduced, and defines some value that is not a Javascript primitive (so, very similar to Java's `Object`). Namely, not: `boolean`, `number`, `string`, `symbol`, `null`, or `undefined`; in other words, all `object` fields must be something that could be expressed via `{}`.

So, this ensures the value is not a primitive value (though JS primitives are different in nature). While this is interesting and useful, it is not the same role that a top-type plays in a proper type system (though admittedly, it is very similar to `Object` in Java!)

# Back to `unknown`

And so, we come back to TypeScript 3.0's `unknown` type. Like `any`, a variable of this type may be numbers, strings, booleans, null, undefined, arrays, and any other object type. However, unlike `any`, an unknown variable can't be acted on until it has been clarified and narrowed into a more specific type. As a result, `unknown` is much like `Object` in Java or `Any` in Kotlin: it is known to be "something", but without a refinement (aka cast) to a specific or narrowed type, it is not of much use (and, as a result, the program is more type-safe). Revisiting our previous example, but with the `unknown` type:

```typescript
let aPerson: unknown = { age: 15, name: "Bob" }
// Compiler Error: error TS2571: Object is of type 'unknown'.
let name = aPerson.name
```

To resolve this, the compiler must have a preceding constraint check (similar to `instanceof` in Java or `is` in Kotlin) that checks the minimal required shape of the object. For example:

```typescript
let aPerson: unknown = { age: 15, name: "Bob" }
if(aPerson is { name: String })
  let name = aPerson.name
  // do something with name
}
```

Note that, unlike Java or Kotlin, the "type check" is based on the existence of fields and methods on the underlying instance (duck typing), rather than some actual runtime type (remember: in TypeScript types are conventional and not actually encoding into the underlying object).

# Summary

Hopefully this describes the benefits of `unknown`. Ideally, most programs can likely transition use of `any` to `unknown` seamlessly; for most TypeScript programs `any` has been treated as `unknown`. However, occasionally there have been edges where the benefit of an `any` "dynamic invocation" have been too beneficial to resist due to the neatness of a programming model it provides.

Thankfully, `any` as a type still exists and migration can be gradual and targeted.
