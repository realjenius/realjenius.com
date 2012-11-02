---
title: Check Out the Play! Framework
summary: 'The Play! Framework is a new Java web-framework that actually manages to merge minimalism and Java together (somewhat paradoxically).' 
tags:
- java
- play framework
- scala
category: journal
legacydate: 3/1/2010 10:54:00
legacyId: 454
layout: post
---

{% assign align='right' %}
{% assign src='/play/play.png' %}
{% include image.html %}

I've spent a lot of time recently investigating a variety of languages other than Java, such as [JRuby](http://www.jruby.org) and [Scala](http://scala-lang.org), and truly believe from these experiences that traditional Java MVC web frameworks are inherently flawed in design and implementation. The effort involved in implementing on a framework like Struts or Spring MVC is astronomical, especially if you are going to implement things "the right way".

It's amazing to me how much these platforms push "hello, world" examples that are simply not realistic web applications. After trying these short examples, developers turn around and start trying to implement a complete application, and this simple example balloons into a mess of code, and that's without any real functionality yet in the application. A co-worker of mine is a fan of saying "\[These frameworks\] make the simple things trivial, and the hard things impossible".

Historically, I've been known to say "If you are doing web-development in Java, use [Wicket](http://wicket.apache.org)"; this was based on the fact that to my experience Wicket took the most advantages from the strongly-typed, and strongly IDE-supported, Java language, as opposed to trying to hide them behind anemic and broken templating languages that have horrid editors and basically trade one problem for another.

Recently, however, I spent some time doing some significant development with the [Play Framework](http://playframework.org). I have to say that I think the Play Framework has eclipsed my Wicket fever. That's not to say that I don't still think Wicket is very powerful, but I have been particularly impressed with the feedback loop provided by Play. It has, without a doubt, the most direct code-test-cycle I have seen in any platform for Java (it approaches the instant feedback of Rails), and also has the distinct advantage of being stateless out-of-the-box (something Wicket is definitely not).

Play manages this feedback loop problem in a rather novel way - embedded in the framework is the [Eclipse](http://www.eclipse.org) compiler for Java (ECJ). This means that when you're coding for the play framework, you're not sending it your class files, but rather your source files. This allows Play to recompile code in a running instance on the fly - I literally only restarted my application a handful of times while I was coding over *the course of several days*. It also integrates seamlessly with IDEs, and ships with an embedded HTTP runtime (no deployment is necessary during development).

There are a number of other benefits Play can provide by working with source files instead of class files. Much like Rails ability to add functionality to your application at runtime, Play can (and does) pre-process certain Java classes to add functionality.

I was further heartened to see that the next release of Play is meant to fully support Scala, which would allow for other modern language features to be used with this highly interactive framework.

It's hard to describe all of the neat features Play provides in a few hundred words, but I would highly recommend [you check it out](http://www.playframework.org) - they have a 10 minute screencast they sells it better than I can. While I'm still convinced Java (as a language) will be surpassed for an overwhelming majority of the web-development as the language continues to stagnate, this is a compelling framework for the Java platform as a whole, even if Java isn't your language of choice.