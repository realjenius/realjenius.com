---
title: 'Taking Advantage of Java in JRuby'
summary: 'I show how you can use Java libraries in your Ruby application for the betterment of your sanity.'
tags: ["java", "ruby", "jruby"]
date: 2009-09-09 23:32:00
---

<p><a href="http://blog.enebo.com/">Tom Enebo</a> and <a href="http://blog.headius.com/">Charles Nutter</a> have posted a couple of entries over at the <a href="http://www.engineyard.com/blog/">EngineYard Blog</a> on accessing Java libraries from JRuby.</p>

* [Part 1: Scripting Java Libraries with JRuby](http://www.engineyard.com/blog/2009/scripting-java-libraries-with-jruby/)
* [Part 2: You've Got Java in My Ruby](http://www.engineyard.com/blog/2009/youve-got-java-in-my-ruby/)

*For those who may not be familiar, Tom Enebo and Charles Nutter were full-time Sun employees until late July [when they switched companies](http://www.eweek.com/c/a/Application-Development/JRuby-Team-Leaving-Sun-for-Engine-Yard-382884/) to Engine Yard. As many others have said, it's a different experience (and in my opinion, a very good one) seeing JRuby posts show up on the EngineYard blog.*

You have to make a decision at some point when developing a JRuby application as to whether or not you are going to hide the 'Java' in your JRuby application from the main bulk of your code. Obviously, if you are developing a Swing GUI, it's pretty much a foregone conclusion that it's a JRuby app, and JRuby alone. On the other hand, it's very possible now-a-days to code a Rails app that can run concurrently on JRuby, MRI, and any other compliant platform.

No answer is 'right'. Each approach has its own virtues, and it really depends on your goals. A Java developer who simply wants to make coding Java apps easier could easily switch to JRuby and sprinkle Java references throughout their app. On the flip-side, you may just choose to deploy an existing Ruby app on JRuby for it's compelling performance characteristics and its alternative deployment/scalability options; or perhaps your company would rather manage a Glassfish cluster than a Mongrel cluster - there are a variety of possible reasons.

Tom's article (part 2) delves a little deeper into the influence of Java from an API perspective (as opposed to the basic Java-integration language constructs). His comments on delegation are compelling:

> Delegation as a concept is not specific to Ruby, but is worth bringing up. Why decorate a Java class and get all of that Java class’s methods exposed when you can hide them behind another Ruby class that only exposes what you want? This is especially powerful when you want to write a common API that works across multiple Ruby implementations (e.g. JRuby, MRI, and Rubinius), but has different native backends.

In practice, even if you have committed to JRuby as your platform and have Java libraries referenced all over the place, it can still be very valuable to abstract APIs around the original Java implementation - if for no other reason than Java APIs needing some TLC to feel natural in Ruby. Likewise, there are cases where using Java libraries as the driver for a particular component of your application may give you a competitive advantage on JRuby.

In that vein, I was recently working on a high-concurrency component of a JRuby application. In my opinion, the Java libraries for concurrency are much further evolved than those in Ruby; particularly after the advent of `java.util.concurrent` in Java 5. One of those areas where Ruby is lacking is the existence of a read-write lock. Jonah Burke previously blogged about [implementing a read-write lock](http://blog.jonahb.com/?p=10) using the standard Ruby mutex objects. His implementation is simple, well-implemented, and should be quite reliable. However, Java's `ReentrantReadWriteLock` is fully integrated into the underlying Java platform, including in the instrumentation libraries ([detailed briefly here](http://www.ibm.com/developerworks/java/library/j-java6perfmon/)). To me, if you plan to run on a Java platform, being able to independently monitor locks held by the Ruby runtime is a Good Thing.

Here is a simple JRuby implementation of a ReadWriteLock that uses a Java lock via delegation:

```ruby
require 'java'

class ReadWriteLock
    include_package 'java.util.concurrent.locks'
    def initialize
        @java_lock = ReentrantReadWriteLock.new
    end

    def read(&block)
        read_lock = @java_lock.read_lock
        read_lock.lock
        begin
            block.call
        ensure
            read_lock.unlock
        end
    end

    def write(&block)
        write_lock = @java_lock.write_lock
        write_lock.lock
        begin
            block.call
        ensure
            write_lock.unlock
        end
    end
end
```

As you can see, JRuby makes using Java objects and extending them very straightforward.

This particular implementation is not 100% API-compatible with the one provided by Jonah, however with a little manipulation it could be easily (I'll leave it as an exercise to the reader). This is really more of an example of how Java can provide the underlying engine for Ruby libraries when running on JRuby (this is how the majority of JRuby is implemented, after all) - and if this were API compatible with Jonah's, it'd simply be a matter of constructing this in an isolated factory method, and switching implementations then becomes simply a matter of flipping a flag.

Note that instead of delegation, you could also simply extend the existing Java object with more goodies. Here is an alternate implementation of these same block methods that simply appends these methods to the Java class itself:

```ruby
require 'java'

class java::util::concurrent::locks::ReentrantReadWriteLock
    def read(&block)
        read_lock = self.read_lock
        read_lock.lock
        begin
            block.call
        ensure
            read_lock.unlock
        end
    end

    def write(&block)
        write_lock = self.write_lock
        write_lock.lock
        begin
            block.call
        ensure
            write_lock.unlock
        end
    end
end
```

In practice this works almost identically to the first example. This implementation has some API-leakage (the Java methods on ReentrantReadWriteLock are available to call by the client code), but also has one less object involved, as the methods are added to the Java lock class itself, rather than provided by a proxy object. Which approach you would use for your particular use-case is really dependent upon the scenario in question, and your goals for the API.

Either way for this example, the core usage of this library is identical irrespective of which implementation you choose:

```ruby
lock = ReadWriteLock.new
# Alternatively, the second example would be created this way:
# require 'java'
# import java.util.concurrent.locks.ReentrantReadWriteLock
# lock = ReentrantReadWriteLock.new

# Usage is identical
lock.read do
    puts 'Executing with read lock'
end

lock.write do
    puts 'Executing with write lock'
end
```
