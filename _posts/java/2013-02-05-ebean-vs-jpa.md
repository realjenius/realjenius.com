---
title: Thoughts on EBean vs. JPA
summary: 'Doug Lea has proposed to promote memory fencing mechanics into implementation APIs in Java. I cover what this means.'
layout: post
category: journal
published: false
tags:
- java
- play framework
- scala
- ebean
- jpa
---
A little while ago I posted on Twitter [that I was enjoying working with EBean](TODO). This happened as a natural progression of me exploring [Play! 2.0](http://www.playframework.org), since they suggest [EBean](TODO) as their default data-source backend for the entity model of Play applications when written in Java (when using Scala they [promote Anorm instead](http://www.playframework.org/documentation/2.0.4/ScalaAnorm), and abandoning ORMs altogether).

