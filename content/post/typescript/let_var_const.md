---
title: "Typescript for Java Developers: Variable Scoping"
date: 2018-07-30
tags: ["typescript","javascript"]
---

When learning a language built on another existing platform, like [TypeScript](https://www.typescriptlang.org/) (or, for that matter [Kotlin](https://realjenius.com/tags/kotlin/)), one of the challenges you face is understanding the underlying platform or language so that you can, in turn, understand how the higher-level language is applied to meet the restrictions and features of the lower-level language. Inevitably, no amount of documentation for a language built upon another language or platform is complete without some degree of knowledge of the underlying platform.

With TypeScript, this is true in a lot of ways as TypeScript tries hard to be an augmentation of ECMAScript (aka Javascript), rather than a total replacement (in the spirit of [Coffeescript](https://coffeescript.org/) for example). TypeScript tries to use the same approaches and merits of ECMAScript, but simply advance the language to a more expressive and controllable place. For example, TypeScript:

* Uses the [same approach for namespaces and modules as JS](https://www.typescriptlang.org/docs/handbook/modules.html)
* Supports checking JS types with [--checkJs](https://www.typescriptlang.org/docs/handbook/type-checking-javascript-files.html)
* Uses [Shape/Duck-Typing for interfaces](https://www.typescriptlang.org/docs/handbook/interfaces.html) so JS types can flow naturally from non-TS-libraries

Another area where TypeScript closely follows ECMAScript is variable declarations and how they behave, and without the context and history of Javascript, it can be really confusing. Let's walk through variable declarations in JS and TS!

<!-- more -->

# A Recap of Scoping in ECMAScript

For as long as is relevant, Javascript (now ECMAScript) has had the `var` keyword for declaring variables. Unfortunately, traditional Javascript variables had really bizarre/unconventional scoping. For developers familiar with most [BNF languages](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form), variables are generally scoped by blocks, and breaking out of that block was either (a) not possible or (b) required special syntax (such as pointer refs and de-refs in C++). However, in JS, variables regularly leak and bleed in ways you might not expect.

In general for most modern languages, variable scoping generally works such that a variable declared in a particular block (or frame) of the language leaves scope once that block closes. For example:

```java
var x = "..."
someBlock {
  var y = "..."

  // x and y are accessible here:
  println(x + " -- " + y)
}
// only x is accessible here. y is "out of scope"
println(x)
```

However, `var` in ECMAScript has never behaved that way; instead, `vars` are accessible in the entire containing function. Meaning this is possible:

```javascript
var x = "..."
someBlock {
  var y = "..."
}
// x and y are accessible here:
println(x + " -- " + y)
```

Ruby programmers [may find this familiar](http://ruby-for-beginners.rubymonstas.org/writing_methods/scopes.html), for what it's worth. Another way to understand this is to understand that, prior to EMCAScript 6, JS never supported block-level scoping as a feature. Instead, scoping was either "global" (meaning the entire script could see it) or local (meaning only the function execution could see it).

Further, since everything in Javascript is often `pass by reference`, this only makes it even more confusing or surprising in some cases, as many simple values can be mutated in one scope only to change in another. For example:

```javascript
function getAdder() {
  var x = 10;

  var func = function adder() {
    var y = x + 1;
    return x;
  }

  x = x+10;

  return func;
}

var adder = getAdder();
adder();
```

For many, it might be surprising that "adder" returns 21 here rather than 11. This is a side-effect of the fact that in this case Javascript captures the reference of the variable X in the nested function declaration, and shares that reference with all other uses, including the outer function as well as other nested functions that are created.

For Java developers the concept of "effectively final" comes in to play here. In Java a variable captured by an anonymous inner class or a lambda must be either declared final, or effectively final due to scoping rules. If it is not, you get a compiler error. The above oddity is one of the key reasons why.

The Typescript language shares this concept of the "var" declaration, though generally "var" is discouraged from use in TypeScript. The [typescript documentation](https://www.typescriptlang.org/docs/handbook/variable-declarations.html) has an *excellent* example that shows this bizarre behavior by using delayed evaluation and `setTimeout`:

```javascript
for (var i = 0; i < 10; i++) setTimeout(function() { console.log(i); }, 100 * i);
```

A seasoned developer in `Java` or similar may expect this to print `0,1,2,3,4,5,6,7,8,9` (or not compile) but in fact this prints `10,10,10,10,10,10,10,10,10,10` because the variable `i` is captured as a reference by every timeout function, rather than copied into every timeout function as a value. By the time the first timeout can be run cooperatively by the runtime, the for loop has already incremented `i` to its terminal value (which is `10`).

This pass-by-ref behavior is challenging and workarounds are not always simple. Javascript does force a pass-by-value when invoking functions, and so consequently, many people will force a function boundary to avoid leaking a variable reference. The article above on the Typescript site shows that for many cases, programs will use the `[IIFE](https://developer.mozilla.org/en-US/docs/Glossary/IIFE)` pattern (immediately invoked function expression) to force the capturing of `i` at each invocation so that it is not a moving referential target. It's an ugly pattern, if effective:

```javascript
for (var i = 0; i < 10; i++) {
    // capture the current state of 'i'
    // by invoking a function with its current value
    (function(i) {
        setTimeout(function() { console.log(i); }, 100 * i);
    })(i);
}
```

In other words, this wraps the invocation in another function which shadows the value of `i` into a fixed and immediate resolved value (the executed function captures the value of `i` at invocation time, rather than a reference to the original `i` value).

The last behavior that makes `var` declarations unpleasant is their ability to be redeclared. Surprisingly for many developers, a `var` redeclaration is no problem in Javascript:

```javascript
var x = 5;
var x = 10; // this_is_fine.gif
```

This pass by reference behavior tied with the leaky scoping of traditional `var` variables and silent re-declaration has caused a ton of issues over the years.

*Note*: I'm going to avoid talking about variables declared *without* the `var` keyword (global scope variables), but needless to say [they make things even more confusing](https://stackoverflow.com/questions/1470488/what-is-the-purpose-of-the-var-keyword-and-when-should-i-use-it-or-omit-it).

# Enter `Let`

All of these headaches resulted in the introduction of the keyword `let` in ECMAScript 6. This keyword, as you might expect, introduces ["lexical scoping"](https://en.wikipedia.org/wiki/Scope_(computer_science)) to ECMAScript variables. This form of variables has a key series of benefits:

* They are scoped to the block in which they are declared (inside the `if/try/catch/while/for` block, for example)
* They cannot be set or accessed before they are declared
* They cannot be re-declared within the same scope
* Variable shadowing (inner blocks declaring variables with the same name) is allowed, but it requires distinct block boundaries

This is what "let" does in TypeScript as well. For most programmers that means that `let` is much more intuitive and understandable than `var` ever was. For a Java programmer, this is basically what all Java local variables and method parameters do by default. One exception is the ability to shadow variables within the same functional scope. Shadowing does exist in Java, but it has to involve higher-level lexical scopes, like classes.

```java
// MyThing.java
public class MyThing {
  private String name;

  public void myMethod() {
    String name = "test"; // shadows MyThing.name
    if(somethingIsTrue()) {
      String name = "test2"; // compiler error, but would work in TS
    }
  }
}
```

Within Kotlin (or for that matter, [Java 10](https://developer.oracle.com/java/jdk-10-local-variable-type-inference)), a `let` is analogous to a type-inferenced `var`. It can be set and re-set, and respects block boundaries for scoping.

# Tighten Mutability with `const`

Also in ECMAScript 6, `const` was introduced. `const` behaves just like `var`, but once assigned it cannot be re-assigned:

```javascript
let someLet = "test-let"
someLet = "test-let2"
const someConst = "test-const"
someConst = "test-const-2" // compiler error
```

Again, this is how TypeScript `const` works, as well. For Java developers this is analogous to using `final` on a variable:

```java
final String someString = "test-string";
someString = "test-string-2"; // compiler error
```

Kotlin has the `val` keyword which also behaves this way (and compiles to a type-inference final variable in Java):

```java
val someString = "test-val"
someString = "test-val-2"; // compiler error
```

As with Java and Kotlin, a variable in Javascript/TypeScript declared as a const prevents reassignment, but does not prevent mutation of the underlying object. So while the above is a compiler error, this example will work totally fine:

```javascript
const myObj = { a: "test", b: "something" }
myObj.a = "test-2"
myObj.b = "test-something-else"
```

There is a lot more to variables in TypeScript, but this covers the basic scoping rules, and provides foundational knowledge of modern JavaScript as well.
