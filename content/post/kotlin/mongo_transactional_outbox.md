---
title: Microservices, Mongo, Events and the Outbox
date:
tags: [ ]
draft: true
---

Mongo and microservices is a very popular recipe these days. And for good reason. Mongo makes it easy to evolve your documents without downtime, and can easily store new and varied collections of data without any significant operational overhead.

Another very important component to most microservice architectures is an event-stream. Whether you are exploring CQRS, event storming, event sourcing, or simply a published event API for your services, there is a lot of merit in having a proper event stream.

However, microservices that dispatch events have a fundamental problem: they must ensure the events are actually dispatched. And in all but the most trivial examples, this is actually kind of hard -- harder than it might seem at first. This article will explore one solution to this problem with Mongo in particular.

<!--more-->

To understand why "at least once" event dispatching is a challenge, we can look at a fairly simple code block that summarizes the problem using generic "database" and "event publisher" APIs (written in [Kotlin](https://kotlinlang.org) here):

```kotlin
fun saveChange(someEntity: SomeEntity) {
  val txn = db.beginTransaction()
  try {
    db.save(someEntity)
    eventPublisher.dispatch(EntityUpdatedEvent(someEntity))
    txn.commit()
  } finally {
    if(!txn.committed()) txn.rollback()
  }
}
```

In this example the database could be anything that has transactions, and the event publisher could be a JMS bus, or Kafka, or something else entirely; for this example it actually does not matter.

This shows what is typically referred to as the [Two-Phase Commit](https://www.techopedia.com/definition/1252/two-phase-commit) problem (or the "dual write" problem). No amount of clever coding in this function will actually remove the issue: in this example two independent systems with indepenent transactional models can both fail and one of them will always be in the wrong state:

* The database may fail to save the record, in which case the transaction will rollback and everything will be fine (given this ordering anyway)
* The event publisher may fail to receive the event (and throw an exception), at which point the database transaction will rollback and everything will be fine (again, given this ordering)
* The transaction may fail to commit -- *now we have a problem* -- at this point the database and the event publisher are out of sync. We have already dispatched the event and cannot roll it back.

We can try reordering things (moving the dispatch after the commit, for example), but there is actually no solution to this fundamentally, it is a reality of this architecture. No matter which way we organize the dual writes, one of them can always happen without the other knowing it happened, and we are left with an out-of-sync situation. One side is always left wondering if the other side is correct. This is, at its core, a manifestation of the [Two Generals' Problem](https://en.wikipedia.org/wiki/Two_Generals%27_Problem) as eloquently summarized by Tom Scott here):

{{< youtube IP-rGJKSZ3s >}}

So, if we can't actually solve this, what do we do? Just hope things work and blindly trust our distributed systems? That sounds bad (and it is).

## From Parallel to Serial

The answer is to rely on only one of the systems, and make the accuracy of the second system a durable side-effect of the first. There are, broadly speaking, two ways to handle this:

* Rely on the event system and use event-sourcing to incrementally build the database model. This presents race-condition challenges for [CQRS command services](https://microservices.io/patterns/data/cqrs.html) so I will not be disucssing that here, but there are many systems that try to solve this model and are built "event first".
* Rely on the database and use the [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) pattern to ensure events are eventually distributed after they are stored.

The solution in both of these models is to accept the term **"eventually"** as a truth in distributed systems, and rely on that as our crutch. While we cannot guarantee in the moment of the initial database write that we can ensure dispatching of the event, we can assume that *eventually* systems will heal and we will be able to get the event dispatched; especially if we are in a place to retry durably (instead of volatile retries while furiously hoping we can commit the database write).

Further, we can build the system so that if for some catastrophic reason we *cannot ever* dispatch the event, it is eminently clear that we are failing catastrophically so that we can alert on this and operators can come in and try to fix the systemic failure.

## Understanding The Outbox

The idea of the [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) is actually quite simple: instead of trying to synchronously dispatch the event, we instead record the event along with the primary data we are trying to save in the database, and we do so in the *same* transaction, that way the pending event is stored atomically with the data change.

Then, at some point later (and this is really up to the implementation how much later) we try to dispatch saved events from previous committed transactions.

In a traditional RDBMS (like MySQL or Postgres), the implementation might look something like this:

```
+------------------+    +----------------------+
|    SOME_ENTITY   |    |   PENDING_EVENTS     |
+------------------+    +----------------------+
|  id   : BIGINT   |    |  event_id   : TEXT   |
|  name : VARCHAR  |    |  event_body : BYTES  |
+------------------+    |  timestamp  : TSTMP  |
                        +----------------------+
```

Rather than asking the event dispatching API to dispatch immediately, we store the event, and then point the event dispatching API at the durable record for dispatching. If the API fails, we simply try again, because it is stored durably along with our record. Here is a revised code snippet:

```kotlin
fun saveChange(someEntity: SomeEntity) {
  val txn = db.beginTransaction()
  try {
    // Step 1: Save the entity itself
    db.save(someEntity)

    // Step 2: Save the pending event in the database
    db.save(PendingEvent(EntityUpdatedEvent(someEntity)))

    // Step 3: Commit them together
    txn.commit()
  } finally {
    if(!txn.committed()) txn.rollback()
  }
}
```

This implementation is notably not working with the `eventPublisher` API at all, it is simply saving the event into the database. Now we need another system to periodically dispatch events for us - we could naively build something like this that runs on a separate thread in our service:

```kotlin
// This could be run once every 100ms. or something
fun pollForPendingEvents() {
  val txn = db.beginTransaction()
  try {
    val events = fetchPendingEvents()
    events.forEach { evt ->
      eventPublisher.dispatch(evt)
      db.delete(evt)
    }
    db.commit()
  } finally {
    if (!txn.committed()) txn.rollback()
  }
}
```

However, we have opportunities to make this more efficient. For one thing, we know once we've committed the event to the database that we can dispatch it immediately, so we can build a system to wake up the poller when we know we've persisted something:

```kotlin
db.save(entity)
val eventId = db.save(event)
txn.commit()
dispatchNewEvent(eventId)
```

This "dispatch" can effectively do a more targeted event publication like this:

```kotlin
fun dispatchNewEvent(eventId: String) {
  val txn = db.beginTransaction()
  try {
    val event = fetchPendingEvent(eventId)
    if (event != null) {
      eventPublisher.dispatch(evt)
      db.delete(evt)
    }
    txn.commit()
  } finally {
    if (!txn.committed()) txn.rollback()
  }
}
```

Note however, this "notify" is *opportunistic* only. We still must poll (remember, if the app crashes the notify will never happen). However, once we make this change where we actively dispatch in the "happy path", we can make the polling system much less aggressive:

1. We can now poll once every few seconds instead of every few milliseconds
1. We also only want to look for events with an age greater than (for example) 5 seconds -- we don't need to passively dispatch brand new messages, as they will normally get notified. Being "old" is a good indicator that an event is undispatched due to a previous failure.

This "synchronous + asynchronous" model means that, when things are running well, events are still dispatched nearly as fast as before, but are hardened against loss. If failures occur, the system knows how to self-heal and will eventually get events dispatched, and we can ensure we have "at least once" delivery.

Now, I have not gone completely through how to do this, and as I described the solution here, there are still gaps. For example:

In fact, in [this wonderful talk from Michael Bryzek, the Postgres-based architecture behind Flow.IO is discussed at length](https://www.infoq.com/presentations/microservices-arch-infrastructure-cd/?utm_source=presentations&utm_medium=ny&utm_campaign=qcon), and conceptually looks something like this model.

So all is great, no problems. But what if we're using Mongo instead of a relational database?

## The Challenge of Mongo Atomicity

In a microservices world, relational databases can be a challenge. They don't handle schema evolution as well, they don't scale horizontally easily in most cases, and their connection management can easily hit limitations (max number of sockets per instance, for example). However, one major advantage of the relational model is the ability to perform multi-table transactional writes. A nuance of the above example is that we have to write to two tables to update both `SOME_ENTITY` and `PENDING_EVENTS` at the same time atomically. Further, in a real distributed system, there may be *several* tables that must be paired with writes to `PENDING_EVENTS`, which adds complexity to the challenge.

However, with Mongo (at least, traditionally) writes are only atomic to a single document. If you need to write to multiple documents of the *same* collection or multiple documents in different collections, you run into [Mongo Transactions](https://docs.mongodb.com/manual/core/transactions/). If you are on a reasonably recent version of Mongo (4.0+), then transactions are available. However, they are not without a cost:

* Mongo versions < 4.2 do not support "sharded" or "multi-cluster" transactions
* The overhead for Mongo of ensuring transactional multi-document writes is not trivial; it is a consensus problem for the database to have to solve
* It influences how reads behave across your cluster in complex ways and [you have to be prepred for that in your application coding](https://docs.mongodb.com/manual/core/transactions-production-consideration/#transactions-prod-consideration-outside-reads)
* It has impacts on the oplog and can have production cluster management complexities

While I could discourage the use of transactions, I think the Mongo documentation does a good job of it already:

> In most cases, multi-document transaction incurs a greater performance cost over single document writes, and the availability of multi-document transactions should not be a replacement for effective schema design. For many scenarios, the [denormalized data model (embedded documents and arrays)](https://docs.mongodb.com/manual/core/data-model-design/#data-modeling-embedding) will continue to be optimal for your data and use cases. That is, for many scenarios, modeling your data appropriately will minimize the need for multi-document transactions.
>
> For additional transactions usage considerations (such as runtime limit and oplog size limit), see also [Production Considerations](https://docs.mongodb.com/manual/core/transactions-production-consideration/).

So if we can't use transactions, what should we use? Well, the paragraphs above already hint at it: denormalized data models. Consider a Mongo structure like this for our "some entity" data type:

```javascript
{
  "id": ###,
  "name": "...",

  // Event metadata on a document may be null:
  "pendingEvents": {
    "count": ###,
    "events": [
      "... base64 event, or JSON structure... ", ...
    ]
  }
}
```

Here, we embed the pending events directly inside the document (or the [Aggregate Root](https://martinfowler.com/bliki/DDD_Aggregate.html) in CQRS terms).

Astute readers will immediately pick up the difference between this and the RDBMS model: the RDBMS model ensures that all writes to `PENDING_EVENTS` are serializable to each other and we can organize the events based on a uniform timestamp so they are dispatched in order. In the Mongo case, we are instead only going to have *per-document* order guarantees. This is significantly worse than the RDBMS case, right?

Well, if we were to interrogate this a little bit, we might realize it is less important than we think, and that relying on overly broad order guarantees is actually an anti-pattern in distributed systems. Here are some thoughts worth considering:

* If you have multiple instances of your micro-service in production, they will likely be accepting writes to entities at the same time. Fundamentally the write order across entities is largely arbitrary unless they are handled by the same service instance (at which point, are they really different entities?)
* Production event systems are usually partitioned to support horizontal scalability (for example, [Kafka](https://kafka.apache.org/) or [Spring Cloud Stream](https://content.pivotal.io/blog/spring-cloud-stream-the-new-event-driven-microservice-framework)). More than likely, the partition key is based on the entity itself, and any ordering outside of that "partition" loses all meaning in a real system.
* Given that in "general terms" (and yes, I am aware of the limited Kafka argument) [exactly-once delivery is an unacheivable myth](https://bravenewgeek.com/you-cannot-have-exactly-once-delivery/), consumers cannot typically assume order guarantees in an event stream anyway -- what if an event failed delivery at the producer side and later shows up due to recovery? Now you have an out-of-order situation, and if you aren't prepared for it, you have Problems

Overall, order guarantees beyond a single entity are, almost always, dangerous to rely upon. Therefore, document-centric event metadata is usually a perfectly accetable solution in Mongo. So, how do we scalably read it?

In the model above a solution is already available. We can simply index on "count", and then query based on "not null && greater than zero":

```javascript
db.someentity.createIndex(
  { pendingEvents.count: 1 },
  { name: "pending-events" }
)
```

Querying this is straightforward:

```javascript
db.someentity.find( { pendingEvents.count: { $gt: 0 } })
```

Of course, when looking at this from the perspective of the "event dispatch loop", multiple collections now have to be considered. There is a variety of ways to handle this, but one way is to register all "Event-capable" collections (that are known to have an indexed `pendingEvents` data type) in the loop itself:

```kotlin
const val collections = hashSetOf("someentity")

val docsWithPendingEvents = collections
  .flatMap {
    mongo.getCollection(it).find(gt("pendingEvents.count", 0)).list()
  }
```
