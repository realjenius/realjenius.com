---
title: 'Objectify Entity Subclass Migrations'
summary: 'When using Objectify on Google App Engine, sometimes you want to introduce polymorphism after the fact. This entry shows how you can do that.'
layout: post
category: journal
tags: java gae objectify
---

If you're using Google App Engine with Java, chances are good that you're using Objectify. While Objectify 4.0 final is not technically released, the release-candidate has been available for some time, and has shown to be quite stable.

Unlike with a relational database, the generally preferred way to migrate data in a NoSQL datastore where you may have terabytes of data is gradually, and on an as-needed basis. Typically this manifests in two potential ways, either:

1. When loading the data, apply a transformation to it to fit the new structure, and re-save it right then, or at least mark it for re-saving later.
1. When saving a record with new changes, look for any transformations that need to be applied to upgrade it, and apply them prior to saving.

Whichever is chosen, devs often also decide to asynchronously migrate records in the database concurrently to the main application flow, by simply loading them and re-saving them in a background task queue. This forces the migration put in place above.

This assertive asynchronous process provides the advantage that at some point you can remove some of your old migration hitches, with the primary disadvantage that you have to visit a lot more data in a fixed time-window. Sometimes this isn't feasible (particularly on large log rolls of data), but it can be a useful technique.

Objectify provides all kinds of powerful tools for gradually migrating data in your uber-big GAE datastore to a new model. In particular it has:

* `@OnLoad` for applying transformations inside your entity class right after it was loaded.
* `@OnSave` for applying transofrmations inside your entity class right before it is saved.
* `@IgnoreSave` for disabling an old field after you have loaded and transformed it.
* `@IgnoreLoad` for disabling an old field from being loaded, but still allowing you to save it.
* `@AlsoLoad` for loading other field names into a new field that is a composite.

These allow you to apply all sorts of transformations to entities, but there are always places that can be problematic. One such area is introducing polymorphism into entity records in your environment.

Say for example you have a record type of `WidgetEvent` that you have used to track when a widget is enabled in your application. Then, in a subsequent release you realize that you also want to track widget disables, and you will want to refer to both enabled and disabled events as the more abstract `WidgetEvent` in your application code, and have common Refs from entities to them, as seen here:

{% highlight java %}
@Entity
public class MyOtherEntity {
	// ...

	// May be a WidgetEnabled or WidgetDisabled
	private Ref<WidgetEvent> event;
}
{% endhighlight %}

To be able to have those common refs, you're going to need polymorphic support from Objectify. This allows Objectify to ask for a common data type by key, and then load it into a specific runtime type based on stored values. So, you decide you want this final entity structure for your app:

{% highlight java %}
// Make the old type an abstract super-class, and push enabled-specific logic down.
@Entity
public abstract class WidgetEvent { }

// Make a new subclass to represent existing data.
@EntitySubclass(name="we")
public class WidgetEnabled extends WidgetEvent { }

// Make a new subclass to represent the new data.
@EntitySubclass(name="wd")
public class WidgetDisabled extends WidgetEvent { }
{% endhighlight %}

Starting fresh, this is no problem with Objectify. In concrete terms, Objectify will store records with a hidden `^d` property in GAE (meaning, discriminator). When saving, this value is set to what you specify in the annotation. When loading, Objectify looks at the value coming from the datastore `^d` field, and constructs the appropriate sub-type based on your registered annotations.

Unfortunately, there is a rather confounding issue of introducing hierarchies like this in the form of migrating existing prod data. How can we load existing data that doesn't have the discriminator value persisted with it? If you just leave it alone, Objectify will quickly start throwing runtime errors in this case because it can't instantiate the `abstract WidgetEvent` class.

You could, of course, do this:

{% highlight java %}
@Entity(name="WidgetEvent") // forces widget enabled to use the old event kind name.
public class WidgetEnabled { }

@EntitySubclass(name="wd")
public class WidgetDisabled extends WidgetEnabled { }
{% endhighlight java %}

This will allow you to have the root type represent your original stored value. But... that doesn't make much sense, does it? A disabled event doesn't extend an enabled event. So while this works, it's messy at best. If you have enabled-specific logic, it will leak down into your disabled class.

Instead, you might want to try something like this to provide your soft migration path:

{% highlight java %}
@Entity
public abstract class WidgetEvent { }

// Try to load null as a discriminator for this sub-type to make it the default.
@EntitySubclass(name="we", alsoLoad=null) 
public class WidgetEnabled extends WidgetEvent { }

@EntitySubclass(name="wd")
public class WidgetDisabled extends WidgetEvent { }
{% endhighlight %}

The `alsoLoad` property is a system in Objectify to allow you to take ownership of multiple discriminator value types for one subclass, so it seems perfect for this case. Here we're trying to say "if there is no discriminator, choose `WidgetEnabled`". Unfortunately, while this may seem logicial, Objectify has a short-circuit on loading the event type that always chooses the root type (`WidgetEvent`) when it encounters `null` for the discriminator.

In fact, [I've opened a bug to see if this can be be changed to support migrating in this scenario](https://code.google.com/p/objectify-appengine/issues/detail?id=180&thanks=180&ts=1382632837), where null is explicitly specified on a subclass annotation.

In the mean time, what do you do? Well, one decent workaround that allows you to migrate over time (but unfortunately all ahead of time) is to patch your production environment and then use the raw datastore service to migrate your PROD data.

Using our previous example, that process looks like this.

#### Step 1: Create the Patched PROD Version

{% highlight java %}
@Entity
public class WidgetEvent {
	// All of the existing logic.
}

@EntitySubclass(name="we")
public class WidgetEnabled extends WidgetEvent {
	// Has no body - simply an empty subclass!
}
{% endhighlight %}

Once you've done this, you can update your code where you create new un-persisted `WidgetEvent` instances to create `WidgetEnabled` instances (theoretically this would be the place where, you know, widgets are enabled).

Note that you don't want to just refactor+rename `WidgetEvent` to `WidgetEnabled` and create a new abstract super-class called `WidgetEvent` in its place, because **you want the rest of your code to work directly with the existing superclass**. The reason for this is simple: Objectify, upon encountering a non marked instance in the database, will not create `WidgetEnabled`; it will create `WidgetEvent`. Therefore, it's extremely important your application treat all instances the same way you did before (as the superclass), with the single exception that where you construct *new* instances, you construct the subclass, and therefore force Objectify to store the `^d` value.

So, in short, new data will get the correct `^d` value in the database, and the rest will just plug along as usual.

#### Step 2: Begin Migrating Using Low-Level API

Now that you've got a "moving-forward" solution in place, you can start to work on the existing data. Unfortunately, this workaround still requires that you touch/migrate all of your data before you can upgrade to a polymorphic data-set, but at least you don't have to incur any downtime.

Migrating the existing data is as simple as iterating over the entities using the `DataStoreService`, and updating them in-place:

{% highlight java %}
DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
Query q = new Query("WidgetEvent");
PreparedQuery pq = ds.prepare(q);
Iterable<Entity> all = pq.asIterable(FetchOptions.Builder.withPrefetchSize(200).chunkSize(200).limit(Integer.MAX_VALUE));
int count = 0;
for(Entity e : all) {
    e.setProperty("^d", "we");
    ds.put(e);
}
{% endhighlight %}

This will go through and update every single instance using an iterative fetching process; 200 at a time.

You can choose to run this as a single long-running process, or split it up into a bunch of sub-tasks on a task queue by serializing batches of IDs fetched via `q.setKeysOnly()` or something similar.

#### Summary

This is one of the more complex migrations via Objectify. While it's unfortunate that you can't let the old data stay at rest and only migrate as needed, it would take a significant amount of data to make the "migrate-ahead-of-time" solution overly burdensome.