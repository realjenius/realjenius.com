---
title: 'Patterns of Distributed Storage'
summary: 'TODO'
tags: nosql data
category: article
layout: post
published: false
---

When dealing with the majority of traditional relational databases, you're given a number of advantages in one area in particular: the flexibility and depth of the questions you can ask about your data in an ad-hoc fashion. Ignoring some of the more philosophical discussions about relational algebra and database design, the underlying reason for this flexibility is quite simple: most relational databases generally know the whole data-set when you ask. Because of this, they can crunch on the data in a variety of ways to provide certain guarantees of complexity in queries. However, for most of the large-scale distributed storage solutions that we're seeing today (NoSQL), this just isn't the case. A particular node servicing a request will often only have a segment of the total data. (*In this case I'm speaking about the traits of most key-oriented databases: Cassandra, Mongo, Google Datastore, Dynamo, Riak, Couchbase, etc.*) 

The reason behind this "nodes with partial data" design choice is that, presumably, your data-set simply cannot be serviced by a single node. There are a number of reasons this could be the case, but the two major overarching causes are that either (a) the volume of the total data-set is just too big for any single node to handle at once, or (b) the frequency of writes to the data storage is just too high for a single node to keep up (or at least for some synchronous replication between nodes to keep up). Note that I say writes here because reads are generally significantly easier to scale in most storage situations. Inevitably, you hit a wall with writes first.

The "clever" bit with most NoSQL solutions comes with being able to consistently route to the data in a distributed structure based on keys. When it comes to basic reads and writes, the idea is pretty simple at its core (the devil is in the details): by assigning globally unique keys, the database can make smart decisions about storage, like picking a node based on hashing the key or otherwise cheaply determining where the key exists. Since the database can use this same calculation on both writes by key and reads by key, the distribution part is pretty transparent.

However, once you get past basic reading and writing (which all non-trivial applications do), building applications on NoSQL "correctly" gets hard, and fast. Relational databases have entire sections of computer science dedicated to understanding how to effectively store and read data. With the current state of non-relational storage, things are much, much more in flux. When faced with understanding how to store data without being punished later, developers will often throw out phrases like "you have to denormalize the data", or "you have to partition the storage of that value", but for folks coming from a relational background, understanding these patterns in this brave new world can be daunting. This is only made worse by the fact that NoSQL databases all operate a little differently, and therefore often have *very* different ways to tackle the same problem effectively.

What I hope to start to tackle in this article is an overview of some of the more common things that you have to understand when dealing with most popular NoSQL databases. While I will be speaking in the general sense, I hope to show examples from some of the more popular variants where it helps.

##No Monotonically Increasing Keys

This is the first thing that takes folks a while to get comfortable with. With relational databases, it is bashed into your head that you need to use surrogate keys on data so that there is no risk of a key changing because the key doesn't mean anything (it's effectively a pointer). This makes sense in relational databases as generally the only downside is that the table has an extra 64-bits on the leading edge. Nearly all RDBs have the concept of a incrementing integer value (MySQL's autoincrement, Oracle's sequences, etc).

However, to properly implement a monotonic integer on a NoSQL database with 15 nodes distributed across multiple data-centers, those nodes would have to coordinate, a lot. Simply asking "what is the next unique integer key for this record" cannot be answered easily by any particular node, as while it is answering, other nodes may be answering too, and you now have a collision issue. Therefore they have to synchronize with each other. Even clever solutions like grabbing batches of values per node at a time still requires coordination periodically (for the initiated, you probably know there is another solution to incrementing value problem, but I will pretend it doesn't exist until later, as it's generally not recommended for keys anyway).

So what is the answer? If the database can't generate a unique key, how can you get one?

Well, to be clear, I didn't say the database can't generate a unique key, just that it can't easily provide a *monotonically increasing unique integer* for a key.

Different NoSQL databases solve this problem in slightly different ways. Let's look at some of the more common cases:

* Cassandra requires you provide the key. That may sound like they are just passing the buck, but it's really a matter of flexibility. They do have support for an ideal key type: UUIDs. The most common way to generate a unique value (that has no meaning) on an isolated machine is to use a UUID of some variant. UUIDs can be considered globally unique for your dataset - even if the "global" only implies uniqueness for your total connected cluster. (*Some UUIDs are truly globally unique, as they are generated using the MAC address of the device as a foundational value. Since those are issued out uniquely when the hardware is constructed, the values generated on top only have to be unique for that MAC address, which is a much easier problem to solve, as it can be done against a single client. Some Cassandra client libraries can't generate this type of UUID, so they involve other cluster-unique values like a node id that is unique to that connected client).
* Riak is similar to Cassandra in that it allows you to provide keys, but it also has the ability to generate a "random" key, which is effectively guaranteed to be unique. Internally it is probably using a UUID-like model (although that is just speculation on my part).
* Google Datastore is opaque in how it does what it does, but it does actually generate 64-bit numbers on your behalf (they're actually integers in the 64-bit float range to be compatible with Javascript/Actionscript-like languages). Recently, they moved to a ["scattered" generation model for performance reasons](http://googlecloudplatform.blogspot.com/2013/05/update-on-datastore-auto-ids.html) (likely due to some of the reasons I described above). What they are likely doing internally is giving different clients different safe id ranges in the total address space unique to them. Note that, like the other two examples, with Google you can provide either integer or string keys.

Hopefully this talk about the difficulty of incrementing integers has lead you to another realization: implementing your own counters in a database using a simple field you increment in your code is just as unsustainable if not more so in heavily contended cases. I'll discuss the answer to this more below!

##It's OK If Keys Mean Something
As I mentioned above, most folks that have built their careers around relational databases have it baked into their head that "primary keys" for records should not be natural, but should be surrogate, and that any natural keys should be represented, at best, via auxiliary enhancements to their schema. This is *by far* the most common place I see developers struggle with NoSQL. In NoSQL it is much more acceptable for keys to mean something, and to actually contain real data. In fact, in many cases, it is significantly more efficient to do so than the alternatives.

A lot mention this little detail, but don't walk through the consequences. If you just use a natural key, it's not like NoSQL somehow magically removes the "changing key" problem - in fact, it's even harder in NoSQL. This problem manifests in two ways:
* Changing the key of a record in a NoSQL database generally can't just be done in-place - it's a remove and an add instead. In relational databases you can often change the primary key in place (although it's not often recommended).
* Changing a key in a NoSQL database doesn't fix any references to that key in other records in the same database. In relational databases you can often be protected in this case via foreign key constraints, which NoSQL databases don't have.

So why would you use meaningful keys then? Well, in many cases you actually wouldn't. It's certainly OK to use the UUID-like keys I discussed in the previous section.

However, part of the issue people run into is that every table in a normalized relational database either represents a meaningful entity with scalar properties, or it's a link table between multiple entities. In a NoSQL database, there are often data types that exist solely to help facilitate lookups later. Because you can't just rely on relational algebra to query, this is often the case.

Because keys are one thing we know we can look up, and we also know they carry a special performance significance, it's often a way of solving problems and acheiving goals that would otherwise be rather ugly.



##Indexing Yourself

Partitioning Counters

Trees of Keys


