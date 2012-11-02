---
title: 'JRuby 1.4 Plan Details Discussed'
summary: 'The JRuby guys have some details on what they are planning for JRuby 1.4'
tags: java jruby ruby
category: journal
legacydate: 9/21/2009 14:52
legacyId: 253
layout: post
---

{% assign align='right' %}
{% assign src='/jruby/logo.png' %}
{% include image.html %}

The JRuby team has [posted some key JRuby 1.4 details](http://www.engineyard.com/blog/2009/5-things-to-look-for-in-jruby-1-4/) over at [the Engine Yard blog](http://www.engineyard.com/blog).

Of high-level note:

* **Ruby 1.8.7 is the new Baseline** - This promises a number of library changes and backports from 1.9. To see the 1.8.7 changes you can [look here](http://svn.ruby-lang.org/repos/ruby/tags/v1_8_7/NEWS).
* **Ruby 1.9 Compatibility Improvements** - As mentioned in the blog entry, Ruby 1.9 is a moving target, however they are moving a lot closer to having major libraries working as desired. Some high-visibility features (like Fibers) are still on the pending list, however.
* **New YAML Parser** - Ola Bini re-visited the YAML parser in Ruby recently (he has [blogged about the creation of the new parser](http://olabini.com/blog/2009/07/new-jruby-yaml-support-with-yecht/) and [how he ported it](http://olabini.com/blog/2009/07/porting-syck-a-story-of-c-parser-generators-and-general-ugliness/)). The new parser, Yecht, is said to be completely compatible with Syck (the Ruby parser), warts and all.
* **Java Method Dispatch Improvements** - One of the major promised features of 1.4 for quite a while has been improvements to the Java integration; they have been wanting to formalize this portion of the language for sometime. This involves calling Java methods, calling overloaded Java methods, the coersion of types (Java string <--> Ruby string), etc. Charles Nutter has posted a very detailed, developer-centric breakdown of the plans in this integration [on the Developer mailing list](http://archive.codehaus.org/lists/org.codehaus.jruby.dev/msg/f04d2210909210937h3d1be610x8366a70d94cac42e@mail.gmail.com) (which goes beyond what's available in the blog entry). Certainly an interesting read.
* **Java Class Generation via JRuby** - JRuby will finally be able to construct runtime-available Java classes on the Java classpath. This will be available on-demand only, however that could be quite handy in a number of difficult integration scenarios.

This set of features is exciting to me, particularly for the Java integration features. The current Java integration functions, but can be somewhat tempermental, and can end with surprising results at times (I have learned this the hard way via hand-coding Swing and SWT apps).

Additionally, one of the killer features of Scala, in my opinion, is its ability to interact seamlessly with Java libraries at compile-time (create a class in Scala, and code against it in Java). While the semantics defined for JRuby are more runtime-centric for now, it does provide an ability to construct something that can be fed into Java APIs at runtime that expect certain "inferred" contracts to exist (such as the Jersey example they used in the original post).