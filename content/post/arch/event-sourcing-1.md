---
title: Event sourcing
date: 2017-08-22
draft: true
---

When it comes to distributed systems, the challenges of managing state grow exponentially. In fact, past a certain scale, some things are simply not possible. as a result, the goal of most distributed systems is to make the most out of what is possible when you consider the physical limitations and realities of the world, and make those limitations easy to reason about and easy to manage as an engineer.

Event sourcing as a concept has shown up in a variety of spaces in distributed computing in the past few years, and for good reason: it provides a model that can actually be reasoned about in a distributed world.
Consider, for example, all of these platforms:

* Cassandra, Riak, Mongo
* Kafka
* Akka
*
*
* Axon

All of these frameworks use the concepts of event-sourcing at some level, where the changes that have been applied to the system can be reconciled by all nodes in the same fashion without a tremendous amount of coordination.

Event sourcing operates on the idea that log of changes can be produced by all changes made by all systems, and that change-log can, in turn, be eventually realized to a consistent state.

Consider a relational database. Two users might rapidly execute updates like this (even at the same micro-second):

```sql
update some_schema.some_table set some_value = "my value" where some_key=123;
update some_schema.some_table set some_value = "my other value" where some_key=123;
```
These two competing statements executing in quick succession is the root of the distributed programming problem. Who is right? Whose write should be lost in the history and whose write should be the source of truth for subsequent reads? All of this boils down to questions of "now".

In fact, understanding that there actually is no "now" in distributed systems is the first step to understanding most distributed algorithms that actually work. All nodes in the cluster will have a different understanding of the "truth"", and none of them are necessarily wrong, they are just talking to different actors at that moment in time. As a result, most distributed algorithms are all about minimizing how often you have to ask the question "am I right, or are they right?".

This very challenge that there is no single source of truth for a particular record is *specifically why* most distributed systems require that you distribute work by some sort of "partitioning" key. This key allows the system to provide some degree of atomic ordering within a specific constraint.

Different solutions have different goals in reaching consistency, but all of them eventually try to answer the question of what is actually "current" at any point in time by trying to agree to a consistent order of events.

# A Deeper understanding

Recently there was an *excellent* post that visualized how consensus protocols function when multiple nodes are trying to agree on something. It's an excellent use of a few minutes to understand how a cluster of nodes in a system might try to agree on some data.

TODO picture of visualization

If you've watched this visualization or spent any time trying to digest the mechanics of Paxos or Raft, then you will immediately

# Cassandra

Cassandra uses log aggregation as a fundamental foundation for the storage of `SSTables`, which are the fundamental data storage that drives Cassandra.

Consider that

# Kafka

At its core, all Kafka does is append-only logs, so it seems to be a pretty obvious event sourced concept. It wasn't until the introduction of `KTables` with Kafka streams that Kafka got the idea of calculating state off of a series of events organized by key.

What makes the Kafka approach effective is organizing data into partitions based on a partition key. By partitioning data with the same key in an ordered partition, you can always reproduce the state of the record by simply replaying through the partition.



# Akka

TODO akka event streams and message passing durability


# Axon

Event stream repositories and replay
