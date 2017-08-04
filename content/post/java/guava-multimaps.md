---
title: 'Google Guava and Multimaps'
summary: 'Multimaps are one of the many collections in Google Guava. I describe why you might want to use them.'
tags: ["java","guava","collections"]
date: 2011-12-22
---

It's not uncommon in Java to build some sort of in-memory registry that contains a map with a list of items at each position. Often, these implementations look something like this (excluding concurrency details for brevity):

```java
private Map<String,List<Something>> stuff = new HashMap<String,List<Something>>();

// ...

public void add(String key, Something item) {
  if(!stuff.containsKey(key)) {
    stuff.put(key, new ArrayList<Something>());
  }
  stuff.get(key).add(item);
}

public List<Something> get(String key) {
  return new ArrayList<Something>(stuff.get(key));
}
```

[Google's Guava Libraries](http://guava-libraries.googlecode.com) provide a few collections to help with this common case: `com.google.common.collect.Multimap`, and the more specific variants `ListMultimap`, `SetMultimap`, and `SortedSetMultimap`.

The above code can be re-written with Guava like this:

```java
private ListMultimap<String,Something> stuff = ArrayListMultimap.create();

// ...

public void add(String key, Something item) {
  stuff.put(key, item);
}

public List<Something> get(String key) {
  // might as well use the Lists convenience API while we're at it.
  return Lists.newArrayList(stuff.get(key));
}
```

The multi-map has a variety of fancy features that can be used as well. Here are just a few examples:

```java
// returns a composite of all values from all entries.
Collection<Something> allSomethings = stuff.values();

// A more traditional map that can be edited.
Map<String, Collection<Something>> mapView = stuff.asMap();

// remove an individual entry for a key.
boolean removed = stuff.remove(key, someVal);

// remove all for a key
List<Something> removedSomethings = stuff.remove(key);

// One for each value in the map. Updating this collection updates the map.
Collection<Map.Entry<String,Something>> allEntries = stuff.entries();
```

All of the collections returned by the various API are views of the multimap. This can make it particularly easy to work with the map in a variety of ways. It does mean you should probably perform defensive copying anywhere you might be exposing these APIs (generally good practice in most cases, anyway).
