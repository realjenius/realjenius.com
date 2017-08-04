---
title: 'Distilling Mirah: Type Inference'
summary: 'I walk through the type inferencing engine in the new JVM language: Mirah'
tags: ["java","ruby","jruby","mirah","distilling-mirah"]
date: "2010-10-05T22:38:00-06:00"
series: distilling-mirah
---

Recently, I've been watching the work of a handful of developers on a new programming language: [Mirah](http://www.mirah.org). As a fan of the [Ruby programming language](http://www.ruby-lang.org) and a slave to the Java overlords, Mirah offers instant appeal to me, as it borrows the core Ruby syntax almost verbatim, but creates true Java class files runnable on any Java VM. This decision makes perfect sense, considering the originator of Mirah is [Charlie Nutter](http://blog.headius.com), one of the key developers of JRuby, a language heavily invested in both Ruby and Java. (Mirah actually reuses the JRuby parser at the time of this writing, if that gives you any indicator how similar the syntax is).

Because of my interest in the development of Mirah, I've decided to begin spelunking into the implementation as it stands today, sharing with you what is going on internally. Many of you are probably familiar with my "[Distilling JRuby](/tags/distilling-jruby/)" series, and while these articles will likely read similarly, I suspect they will be more brief and hand-wavy. This is partially out of a desire to cover more topics over a short period of time, but also because the implementation for Mirah is very fluid, and is likely to change, rendering these articles invalid or at least out-dated.

Without further ado - let's kick this pig. On to Mirah's type-inferencing!

{{< series distilling-mirah >}}

## Mirah Overview

There are a few key concepts that need to be discussed regarding Mirah before we get started:

* Mirah is not Ruby! Mirah looks like Ruby at first glance, but that is only superficial in nature. We will see why over the next series of topics.
* Unlike JRuby, Mirah is not implemented in Java (well, mostly not). It is actually implemented in Ruby - this is going to make the way we traverse the code in these articles very different than the JRuby series.
* While I say that Mirah borrows the Ruby syntax, it has to modify and add certain structures to fit the mold which has been carved for it. So while it is possible to write some programs that are almost true Ruby syntax, most Mirah programs will have a few extra sprinkles.
* Mirah is statically typed, and compiles to standard Java bytecode. This is one of the key reasons that Mirah is not 100% Ruby-grammar compatible.
* Mirah is designed from the ground up to be a language specification that can be implemented on several platforms (.NET is a perfect example). This introduces indirection in the code that may, at first, seem confusing.
* One of the key principals of Mirah is to avoid runtime encumbrances if at all possible. What this means is that all features in Mirah as it currently stands are implemented by either a compiler plug-in, or by using existing core libraries of the underlying platform (or a combination, of course). This goal is to hopefully avoid the 3-5 MB ball-and-chain that many languages (i.e. Scala, Clojure, JRuby) hang around your neck to run deployed code. The idea being that, if you want runtime features, you can bring Mirah modules in per your own decision, but if you want to fit a Mirah program on a micro-controller that can run Java bytecode (or Dalvik *cough*), you should be able to by forgoing some of those features that require large runtime libraries.


The Mirah site can be found at [http://www.mirah.org](http://www.mirah.org), and the official Mirah 'master' repo is available at github: [http://github.com/mirah/mirah](http://github.com/mirah/mirah). Feel free to checkout and follow along, although one last disclaimer - the code is changing *quickly*, so my articles are bound to fall out of the times.

I'd suggest before proceeding you familiarize yourself with the language syntax - I don't plan to stop along the way.

## A Background on Type Inference

Most JVM language replacements that are garnering attention right now in one way or another avoid explicit typing in the syntax to some degree. Languages that are compiled to normal byte-code with some degree of implicit typing, must involve some form of type inference. This is the process of statically analyzing code to determine the runtime types the code is using by *inferring* from the use of variables and parameters. Statically compiled languages on the VM must do this, because Java bytecode (and the VM) expects to work with types - and if the compiler can't figure it out, it can't compile the bytecode.

Consider this Java statement:

```java
HashMap<String,String> myMap = new HashMap<String,String>();
```

There is really no reason you need to define the left-hand side (the declaration) so explicitly, considering that the right-hand side (the assignment) has already told you exactly what the variable is. Surely this should be sufficient:

```java
var myMap = new HashMap<String,String>();
```

Anyone familiar with C# will likely recognize this syntax convenience. Of course, this is simple example, because you only have to examine this one line to infer the variable type. Things get much more complex when there are control structures, methods, and other language features in the way.

That being said, type inferencing is a well-tread path - it's certainly not unique to JVM languages; far from it. There are different levels of type inference, with the most complete often using something [like Hindley-Milner](http://en.wikipedia.org/wiki/Type_inference) to deduce types recursively (excellent description [of Hindley-Milner by Daniel Spiewak on his blog](http://www.codecommit.com/blog/scala/what-is-hindley-milner-and-why-is-it-cool)).

## Mirah's Type Inferencing

As it stands today, Mirah currently implements a variant of type inference somewhere between true "local" type inference, and fully recursive type inference like Hindley-Milner. Mirah's inference uses a multi-pass infer process, where the first phase does simple local inference (or line-by-line inference), and then subsequent passes are made, looking for new type resolutions from those higher constructs. For example, consider these two Mirah methods:

```ruby
def method_a()
  return method_b(5) + method_b(6)
end

def method_b(x:int)
  return x * -1
end
```

In this case, 'method_a' is obviously dependent upon 'method_b' - but if 'method_a' is inferred first, it will have no way to know what it's return type is, because method_b hasn't been inferred yet. In this case, 'method_a' is 'deferred' for a later inference pass. Shortly thereafter, 'method_b' will be processed, and since it can be completely analyzed through local inference, it will resolve to return an int. At that point, method_a can look at the two invocations that are involved in the return statement, and can in turn determine that it should also return an int.

## The Algorithm

From an implementation standpoint, Mirah does this inference by utilizing the ASTs generated from the source. Each AST knows individually how to infer itself based on its recursive contents - this is something we'll investigate in more detail shortly.

Mirah defines a namespace and class called Typer that is used to orchestrate this entire process. The Typer is asked to analyze each AST tree parsed by Mirah individually, and then to iteratively resolve:

```ruby
typer = Typer.new
asts.each { |ast| typer.infer(ast) }
typer.resolve
```

The infer method for an individual AST node is pretty straightforward:

```ruby
class Typer
  def infer(node)
    node.infer(self)
    # error handling business
  end
end
```

Notice that the typer passes itself into the node - this allows the nodes to callback into the typer for a variety of reasons. For example, each node has to decide for itself whether or not it has enough information to infer. If it doesn't, it will tell the typer that it needs to be 'deferred', meaning it doesn't yet have enough information. All this effectively does is record the node for later:

```ruby
class Typer
  def defer(node)
    @deferred_nodes << node
  end
end
```

So the typer calls infer on the top level AST node, at which point the AST hierarchy will recurse, inferring and deferring nodes as appropriate. After the first recursive inference pass, the typer is then asked to resolve AST nodes iteratively until all nodes are inferred, or until no progress is made:

```ruby
class Typer
  def resolve
    old_len = @deferred_nodes.length
    while true
      @deferred_nodes.each do |node|
        type = infer(node)
        if type != nil
          @deferred_nodes.remove(node)
        end
      end

      if @deferred_nodes.length == 0
        break
      elsif old_len == @deferred_nodes.length
        raise # can't infer error!
      end
    end
  end
end
```

## AST Working Together

Understanding the concept of the AST recursively inferring is the key component to understanding the typer. Consider, for example, the statement `x = method_b(5)` - this is represented by a tree of AST nodes. For those of you with experience in parsers, or experience with my previous JRuby articles, it probably won't be too hard to derive the types of nodes involved - it's basically this:

```
LocalDeclaration
|
.-- LocalAssignment (type_node)
    |
    .-- FunctionalCall (value)
        |
        .-- Fixnum (parameters)
            |
            .-- "5" (literal)
```

The idea is that the declaration will ask the assignment, which will in turn ask the call being made with the parameter types in play, and will then return the type of the call return type. Here is a sketch of the various infer methods for these nodes:

```ruby
class LocalDeclaration
  def infer(typer)
    type = @type_node.infer(typer)  #type_node is the local assignment
    if(!type)
      typer.defer(self)
    end
    return type
  end
end

class LocalAssignment
  def infer(typer)
    type = @value.infer(typer) #value is the "functional" call.
    if(!type)
      typer.defer(self)
    end
    return type
  end
end

class FunctionalCall
  def infer(typer)
    @parameters.each { |param| param.infer(typer) }
    if #all parameters inferred, and method with params and scope is known
      return typer.method_type(@method_name, method_scope, @parameters)
    else
       typer.defer(self)
       return nil
    end
  end
end

class FixNum
  def infer(typer)
    return typer.fixnum_type(@literal) #literal is '5'
  end
end
```

A few things to note here:

* This is totally pseudo code - the actual code has all kinds of branches for caching and other good bits.</li>
* The one literal we have, Fixnum, calls back into the typer to get the actual fixnum type - we'll see this come in to play momentarily.
* The typer has the ability to look up a method type by a signature - when methods are scanned during type inference, they record themselves in the typer for other nodes, like this one, to use when inferring since they are one case of node "hopping", where one AST can be linked to another by reference.
* We're dodging how the functional call determines things like 'method scope' for now.

## Resolving Literals

As noted above, the Fixnum node is asking the typer to give it back a fixnum type. This is done for all of the literal types. It's done this way so that the platform implementation (in this particular case, Java) can plug in a particular type. So in this particular case, the Java implementation, in the JVM::Types module, provides a FixnumLiteral that looks at the provided value, and determines where in the hierarchy it belongs (for you Java folks, that's byte, short, int, long, etc). When asked to actually compile, these AST nodes actually know how to generate the ultra-fast JVM bytecode-ops for primitives.

## Type Annotations

As seen in one of the snippets above, Mirah supports type definitions for places where typing is either required (due to a lack of inference) or desired (widening a type, for example). Forgoing the fact this is a contrived implementation for a moment, consider this method:

```ruby
import java.util.Map
import java.util.HashMap
class SomeClass
  def singleton_map(a:string, b:string):Map
    map = HashMap.new
    map.put(a,b)
    return map  
  end
end
```

Here we are declaring both variable types so we can control inputs, and then we are declaring the return type. The reason you might want to declare a return type like this is so that the compiled method doesn't expose too narrow of an implementation. Remember, we're compiling to Java class files here - so if the compiled type inferred that the method returned a HashMap, that is a contraint we may never be able to change in the future. By changing it to 'Map', we can adjust the API like we would in the Java world to avoid tying ourselves to an implementation. To see this in action, here's the output from mirahc when asked to generate Java code for this with and without the return type:

```java
// With:
public class SomeClass extends java.lang.Object {
  public java.util.Map singleton_map(java.lang.String a, java.lang.String b) {
    java.util.HashMap map = new java.util.HashMap();
    map.put(a, b);
    return map;
  }
}

// Without:
public class SomeClass extends java.lang.Object {
  public java.util.HashMap singleton_map(java.lang.String a, java.lang.String b) {
    java.util.HashMap map = new java.util.HashMap();
    map.put(a, b);
    return map;
  }
}
```

Individual AST nodes know about these definitions (sometimes known as forced types), and will respect those over the corresponding inferred types. That's not to say that it will just take them for granted; the type inference still occurs. In the example above, the method body is still inferred to ensure it returns a type that can be widened to 'java.util.Map' - otherwise the code will cause runtime errors in the VM. Here's a snippet of the method definition AST analysis:

```ruby
class MethodDefinition
  def infer(typer)
    forced_type = @return_type
    inferred_type = @body.infer(typer)
    actual_type = if forced_type.nil?
      inferred_type
    else
      forced_type
    end

    if !actual_type.is_parent(inferred_type)
      raise "inference error"
    end
    return actual_type
  end
end
```

The return_type field will be set by the parser if provided, and takes precedent so long as it's still able to be used in place of the actual inferred type of the method body.

## Uncovered Topics

So this was a quick spin through Mirah-land, but even for the inference engine, a lot was left on the table if you'd like to explore from here:

* Finding "native" types (in this case, calls into and returning Java types)
* Tracking class/method scope when inferring
* Inferring against intrinsics (such as '+', and '||')
* Dealing with multi-node inference - several nodes, like 'MethodDefinition' are expected to infer several parts, including arguments, return type, throws types, etc. This increases the complexity of the implementation, but doesn't have much impact on concept.
* Superclasses, invocation of 'super', overriding, overloading, etc.
* Framework vs. Implementation (i.e. JVM) Responsibilities

Stay tuned as the Mirah story unfolds!
