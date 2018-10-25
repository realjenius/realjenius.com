---
draft: true
---

For any Java developer, a familiar feature exists in TypeScript with a curious name: "Rest" parameters. This article will go over what these are, as well as how they can be used for maximal effectiveness.

<!--more-->

In Java (or really most compiled languages) the relationship between function/method parameters that are expected and those that are provided is strict: you must provide the same number of parameters and each must be of the correct type. For example, in Java this method declaration may exist:

```java
public void myMethod(String arg1, int arg2, List<Long> arg3) { ... }
```

This must be called with the exact arguments as specified:

```java
myMethod("test", 123, asList(1L, 2L, 3L)); // ok
myMethod(null, 0, null); // ok
myMethod("test", null, null); // compiler error! 2nd argument cannot be null
myMethod("test"); // compiler error! 2nd and 3rd arguments missing
myMethod("test", "test", "test"); // compiler error! 2nd and 3rd arguments wrong types!
```

With Java 1.5, the "varargs" feature was added. This created the ability to have a variable number of arguments at the end of the parameter list for a method, so long as those arguments were all the same type. For example:

```java
public void myVarArgsMethod(String arg0, long... args) { ... }
```

This can be called with 0-many longs after providing the required string value:

```java
myVarArgsMethod("test"); // ok
myVarArgsMethod(null); // ok
myVarArgsMethod("test", 1L); // ok
myVarArgsMethod("test", 1L, 2L, 3L, 4L, 5L); // ok
myVarArgsMethod(1L); // Compiler error: 1st argument (String) required
```

Under the covers, "varargs" in Java are really just compiler trickery for building an array. The above example, under the covers, actually looks like this at the bytecode level:

```java
public void myVarArgsMethod(String arg0, long[] args) { ... }

// Calls that look like this:
myVarArgsMethod("test", 1L, 2L, 3L);

// are actually compiled into this:
myVarArgsMethod("test", new long[] { 1, 2, 3});
```

TypeScript rest parameters are very similar to varargs, hence the name "rest" (as in the rest of the parameters provided). In TypeScript we can write a function like this:

```typescript
function myFunction(first, second, ...more) { /* ... */ }
```

This function explicitly declares the first two arguments, but then accepts the rest as an array:

```typescript
myFunction('first', 'second'); // more = []
myFunction('first', 'second', 'third', 'fourth', 'fifth'); // more = ['third', 'fourth', 'fifth']
```

Rest parameters in typescript can be typed, just like varargs in Java:

```typescript
function myFunction(first: string, second: string, ...third: string[]) { ... }
```
