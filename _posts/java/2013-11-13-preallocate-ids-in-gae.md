---
title: 'Bi-Directional References in Google App Engine with ID Pre-Allocation'
summary: 'Setting up bi-directional relationships in Google App Engine efficiently can be tricky. I show you an efficient approach here.'
layout: post
category: journal
tags: gae gcs objectify java
---

It's not uncommon when dealing with any database that you'll occasionally have records where you need to navigate both from A to B, and from B to A - aka bi-directional relationships. In cases where your database is generating your IDs for you, you have a chicken-egg problem; to insert both records and establish the link at-once isn't generally possible, as only one of the records will have a generated ID ready in time.

In the relational world, you typically handle this by having foreign-key constraints going both directions, with one being nullable and the other not. You perform both inserts, establishing the link back to the first on the second, and then perform an update on the first record to point back to the first. Another approach is to move away from database auto-generated IDs to some sort of Hi-Lo generator you manage in the application, or similar.

In the Google App Engine / Google Cloud Storage world, you can of course do this the same way using the insert-then-update pattern. Here is a sketch of what this might look like using Objectify 4:

{% highlight java %}
EntityA a = // ...
EntityB b = // ...
ofy().save().entity(a).now();
b.setA(Ref.create(a));
ofy().save().entity(b).now();
a.setB(Ref.create(b));
ofy().save().entity(a);
{% endhighlight %}

If you are at all familiar with Google Cloud Storage (and where the costs are), this example is probably making you cringe. We just made three individual round-trips to GCS, and further, so we could get the allocated IDs in a synchronous fashion, we used the `now()` join method on the first two calls, tying all of the latency up in our active thread. This is brutal.

Now to be fair, without having any additional tools in our bag, we *could* optimize this a good bit to just two round-trips by using batch saves with null refs on both sides:

{% highlight java %}
EntityA a = // ...
EntityB b = // ...
ofy().save().entities(a, b).now();
b.setA(Ref.create(a));
a.setB(Ref.create(b));
ofy().save().entities(a, b);
{% endhighlight %}

This is better, but still far from ideal. We still have the synchronous block waiting for both A and B to be confirmed as saved and given IDs, and we're writing both entity twice, which means we're spending more money than we'd like.

Thankfully, we can do better still.

The GAE datastore has the ability to allocate IDs explicitly on the client. This is also exposed through the Objectify APIs. We can use this to pre-allocate IDs so we not only eliminate the double write cost, but also eliminate the synchronous blocking for the datastore.

Here's how:

{% highlight java %}
EntityA a = // ...
EntityB b = // ...
a.setId(ofy().factory().allocateId(EntityA.class));
b.setId(ofy().factory().allocateId(EntityB.class));
a.setB(Ref.create(b));
b.setA(Ref.create(a));
ofy().save().entities(a, b);
{% endhighlight %}

Now we've found a way to optimize away almost all of our extra datastore interaction - success!

#### A Caveat

There is at least one caveat as of the time of this writing regarding this approach. In modern GAE deployments, the *automatic* ID generation uses a "scattered" model, where IDs emitted are distributed all over the 51-bit floating-point-safe long integer range. This is, somewhat opaquely, intended to optimize datastore performance. There are two ways this would likely help performance:

1. The scattered ID generation might require the client to chat less with the datastore regarding ID ranges. I'm not totally aware of how GAE performs incremental ID range bucketing to avoid conflicts on multiple clients, but I suspect the scattered approach allows for less frequent unique range check-ins from the client to avoid collisions.
1. The scattered IDs likely distribute better in the key partitions in GCS. With IDs that are numerically close, it's possible that the hash-ring locations for records clump together more than would normally be desired, meaning that your application is unnecessarily biased to a certain part of the datastore.

I bring this up because, at least for now, the client-side ID allocation is still configured to generate the classic incrementally managed identifiers, and not the scattered IDs that were introduced earlier this year.

