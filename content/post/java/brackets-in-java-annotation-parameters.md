---
title: 'Brackets in Java Annotation Parameters'
summary: 'A tip for reducing the noise in annotation parameter values.'
tags: ["java","annotations"]
date: 2009-09-10
---

Just a quick note for anyone using annotations - I was going through a bunch of code today, where I saw someone using the `@SuppressWarnings` annotation (which you are probably using if you have generics in your code), and it quickly became clear that the person wasn't aware of the single-argument support in annotations. They had this in the code everywhere:

```java
@SuppressWarnings({"unchecked"})
public void genericsSuck() {
	// etc.
}
```

Even if the parameter in particular for the annotation takes an array, annotations don't require brackets if you only have one parameter. In those cases you can simply provide the single value, which I think helps the readability quite a bit:

```java
@SuppressWarnings("unchecked")
public void genericsSuck() {
	// etc.
}
```

This also works when you are dealing with named parameters. Here is an example from a TestNG `@Test` annotation - notice that I have even used multiple parameters in this case:

```java
@Test(dependsOnMethods="otherMethod",
      dependsOnGroups="unit-tests")
public void thisMethod() {
	// Do some testing
}
```

If you *do* have multiple parameters, brackets are then necessary:

```java
@Test(dependsOnMethods={"otherMethod","mrMethod"},
      dependsOnGroups="unit-tests")
public void thisMethod() {
	// Do some testing
}
```
