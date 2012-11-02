---
title: 'Review: "Apache Wicket Cookbook"'
summary: 'A review of the new "Apache Wicket Cookbook": A collection of code solutions for a variety of problems in Apache Wicket.'
tags: java wicket ebook
category: article
legacydate: 5/7/2011 18:54
layout: post
---

{% assign align='right' %}
{% assign src='/misc/Apache-Wicket.png' %}
{% include image.html %}

Earlier this week, the folks over at [Packt Publishing](http://www.packtpub.com/) were kind enough to forward me a copy of the **[Apache Wicket Cookbook](http://link.packtpub.com/D58AdD)** for review, and given my previous positive experiences with [Wicket](http://wicket.apache.org/), I was excited to dive in, and see what the book has to offer.

## Overview

[Apache Wicket Cookbook](http://link.packtpub.com/D58AdD) is a dense collection of recipes for working with the various Wicket features, and is written by Igor Vaynberg, one of the main Wicket committers, who is also a contributing author to [Wicket in Action](http://wicketinaction.com/). While "Wicket in Action" is targeted at understanding the "what and why" of Wicket, this new cookbook is all about the "how". To re-summarize from the preface of the book itself:

> A straightforward Cookbook with over 70 highly focused practical recipes to make your web application development easier with the Wicket web framework.

The book is composed in a repeating format, showing different solutions to problems using the same rough outline for each recipe:

* Overview - To start each recipe, a short introductory set of paragraphs are provided that explain the problem at hand, and why it might be important to have a solution.
* Getting Ready - This section sets up the example for the recipe, providing the necessary code snippets to try the recipe out for yourself.
* How to do it - After prepping the example, this section jumps right in with highlighted code changes, showing how to solve this particular problem with Wicket.
* How it works - Now that the reader has seen what to do to fix the problem, this section explains how Wicket uses the code provided to handle the particular problem, and provides more detail for people who like to understand what's going on under the covers.
* There's more - Because these examples are so focused, they don't touch on all facets of a particular API. This section provides highlights of other features to explore on your own.
* See also - A lot of the recipes are inter-related, solving similar, but different problems. This section points you to other recipes in the book that compliment the one you just read.

## Review

Overall, I've been quite impressed with the "Apache Wicket Cookbook". Having been quite familiar with Wicket over the years from writing a number of tutorial articles about it, and applications with it, I found the previously released "Wicket in Action" to be a bit of a redundant read; most of the code and APIs were familiar enough to me in concept, that I didn't feel that book was the right target for me. This book, on the other hand, is very no-nonsense, and hits on a lot of detailed problems that even a seasoned "Wicketeer" would probably have to really dig to solve in a novel and clean way.

This book touches on many of the "hard" problems, including:

* The nuances of safe, attractive, and error-free form handling - the web is all about this, of course, but it seems like most people don't do it right. Wicket has the tools, but you've got to know how to apply them - and there are enough recipes here to train your brain!
* Internationalization - Who hasn't struggled with this in one fashion or another?
* Data Tables - Paging, Sorting, Filtering tables are another "hard to implement" cornerstone of the web, and an area where Wicket really shines.
* Security - Wicket's security model is intense; that's both good and bad, and this section really hits on some sticky (get it? Sticky wicket?) topics, like cross-site request forgery protection.

Other topics touched on by the book include AJAX and Rich UIs (another Wicket "wow" feature), middleware integration (e.g. Spring), and, interestingly enough, charting and graphing.

Being very familiar with Wicket, I didn't feel the need to try most of these examples for myself, and so despite the book being over 300 pages in ebook form, I found myself finishing my read-through in short order. I wouldn't say the book is short, but for me, it was over quickly.

Some of the sections, like the aforementioned chapter on charting, were new to me, and gave me a better feel of how this book would be received by someone less familiar. Others covered problems I have previously solved myself with Wicket, and I was happy to see solutions that I felt were both cleaner and more complete than the solutions I had "cooked up" myself.

Most of the examples are based in reality; not having the overly contrived feel of an author searching for a problem to solve; and many of the chapters focus on a particular example, and build upon it in several stages - providing a good picture on how to take a bare implementation and layer on features until it is "complete".

If you are already familiar with Wicket, but would like to better understand how to use it effectively to build robust, feature-rich, and pretty applications, then this book is for you.

On the other hand, if you've never used Wicket before, you will be lost the moment you hit the first chapter. It just so happens, that [Wicket in Action](http://wicketinaction.com/book/) is already available for novices, as are a [number of online resources](http://www.google.com/search?sourceid=chrome&ie=UTF-8&q=wicket+tutorial) (some by yours truly; albeit a little out of date).

For those of you that would like to try this new book out, I have been given access to a free chapter that you can download in PDF format: **[Chapter 5: Displaying Data Using DataTable](http://www.packtpub.com/sites/default/files/1605OS-Chapter-5-Displaying-Data-Using-DataTable.pdf?utm_source=packtpub&utm_medium=free&utm_campaign=pdf)**.