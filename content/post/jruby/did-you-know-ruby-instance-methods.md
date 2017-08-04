---
title: 'Did You Know: Ruby Instance Methods'
summary: 'I show off a lesser known feature of Ruby objects: Instance Methods'
tags: ["ruby","meta-programming"]
date: 2009-09-14 12:47:00
---

Did you know that in Ruby you can add new methods to a particular object, as opposed to adding them to the entire class?

Ruby makes it easy to re-open any class to add new methods - that's almost always covered early on in Ruby books that talk through using the IRB; it's a great tool for being able to evolve code incrementally as you are scripting the solution. Less common, however is discussion over how to add methods to individual objects; something that feels foreign to a lot of Java developers like myself. It turns out, it's just as easy as adding methods to the class.

For example, let's say you have created a class to represent a dog:

```ruby
class Dog
 def bark
  puts 'Woof!'
 end
end

pug = Dog.new
basset = Dog.new

pug.bark
>> Woof!

basset.bark
>> Woof!
```

Adding a method to all objects of the class dog is very simple. Simply re-declare the class, defining the new methods in place:

```ruby
class Dog
 def growl
  puts 'Grrr!'
 end
end
```

To show that this new method works on all instances of that class, we can now call it on both of our existing dog objects:

```ruby
pug.growl
>> Grrr!

basset.growl
>> Grrr!
```

There can be cases where a particular object should be given special functionality without manipulating all objects of that class; generally speaking this is a case where the object needs to be [adapted](http://en.wikipedia.org/wiki/Adapter_pattern). There are different ways to achieve this, such as using a proxy object wrapping the original; however one way to solve this involves adding the method to just one object. This isn't always the "right" solution for the job, but there are definitely cases where it can streamline a block of code, by simply being able to adapt an existing object to fit a different API.

In Ruby this is done simply by prefacing the method name with the variable name of the object you want to modify (obviously, that object has to be in scope at the time you make the modification). Beyond that, the syntax is the same as any method definition:

```ruby
def pug.snort
 puts 'SNGRRHT!'
end
```

Note the 'pug.' prefix. The best way to read this is "define the method snort on the object pug, and assign this code block to it". Simple testing will show that our pug now has "snort" behavior, but our basset, on the other hand, does not:

```ruby
pug.snort
>> SNGRRHT!

basset.snort
>> NoMethodError: undefined method `snort' for <Dog:0x1de8aa8>
```
