---
title: 'Harnessing Java varArgs on Non-VarArgs Methods'
summary: 'A technique for cleaning up array method parameter syntax from the outside.'
tags: java varargs
categories: article
legacydate: 9/11/2009 22:47
legacyId: 164
layout: post
---

Var-Args in Java can often make a particular algorithm appear much cleaner, simply by allowing the developer to omit array-box syntax altogether. So, for example - here is a method call for setting up a fictional date-input validator with acceptable inputs:

{% highlight java %}
public boolean validateDateField(String field, DateFormat[] formats) {
boolean validates = false;
// Do something here.
return validates;
}
{% endhighlight %}

... and here is how you could call it:

{% highlight java %}
if(!validateDateField("myField", new DateFormat[] { new SimpleDateFormat("MM/dd/yyyy") })) {
// Didn't validate!
}
{% endhighlight %}

Quite a mouthful. In Java 5 this can be simplified quite a bit by simply turning the method into a var-args receiver:

{% highlight java %}
public boolean validateDateField(String field, DateFormat... formats) {
boolean validates = false;
// Do something here.
return validates;
}
{% endhighlight %}

... which allows the caller to do this:

{% highlight java %}
if(!validateDateField("myField", new SimpleDateFormat("MM/dd/yyyy"))) {
// Didn't validate!
}
{% endhighlight %}

But what if you are working with a library that wasn't converted post-Java-5 to support varargs-style parameters, you don't have the source to be able to change it, and you'd rather not tack on a bunch of array construction syntax? Or, what if the portion of a method that accepts an array is not the last parameter (or there are multiple array parameters)?

In these cases, we can still leverage varargs and type-inference (as a caller) to make our syntax shorter and a little more expressive. With the method-level type-inference of Java 5 along with varargs, you can create a method like this:

{% highlight java %}
public <T> T[] array(T... types) {
    return types;
}
{% endhighlight %}

This method is just a convenience array-construction method, but it is very reusable because it can construct an array of any type via varargs syntax. Even though generic types are erased by runtime, because Java constructs an array on-behalf of you for var-args (the compiler does, specifically), you don't need to know the type at runtime. Normally this would be a problem, because unlike a generics-list (which only has type information at compile-time), arrays have type information at runtime, and as such can't be constructed with generics paramters (in other words, you couldn't say new T\[\] - the compiler no-likey).

This method can now be used to shorten the calling of the non-varargs variant of this method quite a bit:

{% highlight java %}
public void testMethod() {
  if(!validateDateField("myField", array(new SimpleDateFormat("MM/dd/yyyy")))) {
    // Didn't validate!
  }
}
{% endhighlight %}

Not quite as nice as the varargs version, but at least it's very clear what is happening - and it sure beats the `new DateFormat[] {...}` line. Additionally, if we change the original method signature slightly (let's say it's a method for validating multiple fields):

{% highlight java %}
public boolean validateDateFields(String[] fields, DateFormat[] formats) {
    boolean validates = false;
    // Do something here.
    return validates;
}
{% endhighlight %}

We can still use our varargs tool on both method parameters:<

{% highlight java %}
public void testMethod() {
    if(!validateDateFields(array("myField1", "myField2"), array(new SimpleDateFormat("MM/dd/yyyy")))) {
    // Didn't validate!
    }
}
{% endhighlight %}