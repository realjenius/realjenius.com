---
title: 'Spark: Sinatra Goes Verbose'
summary: Spark is a new micro-application-server for Java that models itself after Ruby's Sinatra library.
  While it's still Java, it shows that minimalism is possible with this crufty old language.
tags: java sinatra ruby spark web
categories: journal
legacydate: 8/2/2011 12:51
layout: post
---

It would seem that the prevailing wisdom in the upper echelon of alpha-geeks is that Java, as a language, is no longer generally viable for effective coding of... well frankly, anything. Where-as five years ago, everyone working on the JVM was looking for the next-best-Java-framework, it seems the focus has switched to finding the next-best-language instead. Scala, JRuby, Clojure, Groovy, and a whole host of other JVM-based languages (there are several new contenders like Kotlin, Ceylon, Stab, Gosu, and Mirah) have begun making news for their vastly superior language features that promise to dramatically improve the coding experience, and ideally, improve the code itself.

It's no surprise Java developers are trying to branch out: Java 7, which was nearly five years in the making, barely evolves the language features at all; most of the promised game-changers for the language (lambdas, extension methods, modules) were deferred to Java 8 due to lack of consensus and time. Java has become stagnant, too verbose, and too crufty for developers that have seen the greener grass on the other side.

Unfortunately, most JVM developers in the wild are still using the Java language in some capacity, and it seems to be fairly common in companies that there is a distinct reason that an alternative VM language hasn't been adopted in its place. The reasons may not satiate the idealists of the JVM-based community, but they do exist. Just to name a few:

* Corporate restrictions
* Concerns about code maintainability
* Team skill-sets and strengths
* Stability and performance of the various language platform
* Lack of top-grade IDE support

Frameworks, on the other hand, often seem to be an easier pill to swallow. The barrier to entry for developers is often lower, the scope of impact on your application can be less pervasive, and your tools and team skill-sets are not stretched nearly so far. In reality, some frameworks (*cough*Spring*cough*) actually add more complexity than any language shift would, but this is unfortunately a game of perception. As it pertains to frameworks in the post-Java JVM world, those of you that follow my blog or Twitter account know that I'm a big fan of the [Play! Framework](http://www.playframework.org), as it re-imagined what it means to write a Java web application, and it also provides an easy gateway into Scala. It shows that while Java the language is falling behind, it isn't a complete wasteland for developers craving more.

A co-worker recently pointed out another intriguing Java framework that, while not being as full-featured or targeted for large applications as Play!, has a lot to offer to this neo-Java world: enter [Spark](http://www.sparkjava.com/).

Those of you who have worked with (or at least seen) Ruby's [Sinatra framework](http://www.sinatrarb.com/) will instantly feel home (and perhaps vaguely disgruntled) with Spark. Spark is effectively the Sinatra-style of web-binding, using Java syntax. Here is an example from their home-page:

{% highlight java %}
import static spark.Spark.*;
import spark.*;

public class HelloWorld {

   public static void main(String[] args) {
      
      get(new Route("/hello") {
         @Override
         public Object handle(Request request, Response response) {
            return "Hello World!";
         }
      });

   }

}
{% endhighlight %}

The general idea behind Spark is to make the binding between a URL to the actual code being run as thin as possible - allowing you to focus on servicing the request. When compared to many frameworks, the list of features it *doesn't* have may be disconcerting; but, there is a certain power and portability in the simplicity. The self-coined term "micro-web-framework" is really only true due to the sheer volume of complexity and features that Java web frameworks have decided to provide (or impose) in the last few years.

Like Play!, Spark focuses on using very human-readable API design. The central component of Spark is the callback which handles the request. As seen in the above example, this is provided by the developer via a subclass of "Route". What happens inside the callback to build the result is entirely up to you.

While Spark doesn't get involved in the "manipulating data" part, there are a handful of features and utilities available to help with the control of HTTP-level web-flow. Some of these include:

* Filters - These are callbacks just like the routes that can be run based on certain URL patterns, allowing for functionality to be applied orthogonal to a set of requests.
* Request/Response Wrappers - The servlet request and response classes are well known (and often loathed) for their design. Spark, like many frameworks, wraps these to help conceal the suck.
* Halt Commands - This is an increasingly popular API design in web frameworks: methods that set an HTTP status code, and fail with an exception immediately.
* Redirects - Browser redirects are made particularly simple.

All of these features are shown in more detail on the [Spark Readme Page](http://www.sparkjava.com/readme.html).

Spark's main facility for running is to start up an embedded Jetty server to automatically handle requests. This is right in-line with how Sinatra functions by default, and provides a quick and easy process for developers to get their application going to test and do development. While not documented on the site, Spark does support a deployed mode where it can be run inside of an already-deployed application server as a WAR with a web.xml file. This is done via the spark.servlet.SparkFilter class, which is a servlet filter that can route requests to your application.

In summary: it's unlikely you would want to implement your entire enterprise on Spark, but that's not really its goal. Spark is really targeted for quick-to-live "scrapplications"; getting something together in a short amount of time, and in the hands of users, without the pomp and circumstance of importing 900 JARs, and creating 35 configuration files. It explicitly avoids imposing a particular application model, data model, or really any dependencies at all on the developer, instead offering a small expressive Java API (as expressive as Java gets anyway), that allows you to quickly map blocks of Java code to RESTful HTTP routes. Overall - definitely worth checking out.