---
title: 'Reclaiming the Underscore'
summary: 'Java 8 will start complaining about the use of an underscore by itself as a variable name.'
layout: post
category: journal
tags: java java8 jdk8
---

Java, as a language, has historically been quite careful to avoid changes that are forwards-incompatible. This is quite obvious to anyone who has spent any time coding against the JVM. Very few changes come in that don't allow for applications migrate forward naturally. There seem to be different tiers of protection here, with the first being binary compatibility. Ideally, applications compiled with Java 1.4 will still run on JVMs today, which says a lot.

Library compatibility has also been a big effort over the years. Deprecations run amok in the core libraries, and there are duplicate classes for many core pieces of functionality.

Finally, we have source compatibility. Again, most changes to the syntax of Java have been forward-compatible, and in turn the syntax of the language has seen some odd choices to meet this need (the use of `:` for the "foreach" construct, for example).

That's why I found it interesting that Java 8 will see the reservation of the `_` character [for all variable identifiers](http://mail.openjdk.java.net/pipermail/lambda-dev/2013-August/010673.html). Before you grab your pitchfork, note that this is only when using identifier _alone_ (not as part of a larger identifier name), and for all existing cases it's only a warning about it being possibly unusable in future releases. Here are some examples:

{% highlight java %}
// This is totally fine.
String _test = "test";

// This will produce a compiler warning.
String _ = "test";

// this will produce an error
Consumer<String> op = (String _) -> { /* ... */ }
{% endhighlight %}

Note that in the lambda case, they are immediately failing on the keyword for the "lambda formal", as this is a position you can't possibly have in your existing code prior to Java 8.

When pressed for some details on this on the JDK-8 mailing list, this is what Brian Goetz said about the future of "underscore" as a variable name:

> Yes, we are "reclaiming" the syntactic real estate of "_" from the space
> of identifiers for use in future language features.  However, because there
> are existing programs that might use it, it is a warning for identifiers
> that occur in existing syntactic positions for 8, and an error for lambda
> formals (since there is no existing code with lambdas.)
>
> Your suspicion is mostly right, except that we are certainly NOT going to
> do Scala's "wunderbar".  However, things it might be used for include
> things like "I don't want to give this variable a name" (such as catch
> parameters that are never used.)