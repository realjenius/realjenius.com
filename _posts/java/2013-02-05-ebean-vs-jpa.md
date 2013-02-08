---
title: Thoughts on EBean and JPA
summary: 'EBean is Play! 2.0''s storage model of choice for Java. Why?'
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

A little while ago I posted on Twitter [that I was enjoying working with EBean](TODO). This happened as a natural progression of me exploring [Play! 2.0](http://www.playframework.org), since they recommend [EBean](TODO) as their default data-source backend for the entity model of Play applications when written in Java (when using Scala they [promote Anorm instead](http://www.playframework.org/documentation/2.0.4/ScalaAnorm), and abandoning ORMs altogether).

I had actually never heard of EBean prior to that moment, and was curious and somewhat taken-aback that they had relegated JPA to a secondary option, since it's kind of been the drug of choice (good or bad) in the Java world. So I took a dive, and what I found was interesting enough I figured it merited some documenting here.



