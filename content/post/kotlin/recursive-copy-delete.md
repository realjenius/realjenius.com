---
title: "Kotlin 1.8 - Easier Copying and Deleting of Files"
date: 2023-01-20
tags: ["kotlin"]
---

I think most Java developers (and Kotlin JVM developers by proxy) have for a long time lamented the absence of a really simple "copy directory" or "delete directory" API. It seems, at the surface, such a simple thing. Of course there are edge-cases that exist that have prevented the existence of a "one-line" API in the JDK. Kotlin 1.8 ships with some new easy-to-use APIs for just this, with tools to handle the uglier edge-cases built right in. Let's take a look!

<!--more-->

A very common "shortcoming" complained about with `java.io` and `java.nio` file APIs on sites like [StackOverflow](https://stackoverflow.com/questions/29076439/java-8-copy-directory-recursively) is the lack of a recursive copy and a recursive delete for directories.

Generally, suggestions for handling this are either using the `FileVisitor` API in Java (which is probably the correct, though fairly verbose, answer), or use something like Apache Commons `FileUtils`, which implement the recursive behavior, but bail out in weird ways if something goes wrong along the way. Notably, something like `FileUtils` just gives up with an `IOException` if there is a reason it can't complete the task, and leaves you trying to figure out precisely how to recover. Additionally, these utilities rarely support custom functionality like filtering or skipping files selectively.

The `FileVisitor` solution is the more flexible API, and in turn enables more complete solutions, but as even just this single StackOverflow discussion indicates, there is still some degree of control that is up for debate, such as once you find a file, what is the best way to copy that file.

Kotlin 1.8 introduces experimental extensions to `java.nio.file.Path` which enable easier copying of recursive directories, but still support granular customization and edge-case handling, as well as the ability to very easily delete recursive directories. Thanks to the language features of Kotlin, it's possible to do a lot more with a lot less boilerplate.

The most naive copy behavior with this API would look like this:

```kotlin
val path = Path.of("some-directory")
path.copyToRecursively(
  target = Path.of("some-other-directory"),
  followLinks = false
)
```

**Note:** to use this API for now you have to either have `@ExperimentalPathApi` or `@OptIn(ExperimentalPathApi::class)` in the scope of the code to accept the fact the API may go through breaking changes.

However, this API is superficially not any less verbose than using a helper API like Apache, which would look like this:

```kotlin
val file = File("some-directory")
FileUtils.copyDirectory(file, File("some-other-directory"))
```

The difference begins to show up when you look at the other optional parameters available to this API. For example, as a developer it might be nice to know precisely why a copy might fail due to issues, such as ACL/permission issues to file, or files that couldn't be copied due to target space issues. With Apache, this error would be a hard-to-interpret `IOException`. With the new Kotlin APIs we still might get exceptions, but we get much more granular details where this happens:

```kotlin
path.copyToRecursively(
  target = Path.of("some-other-directory"),
  followLinks = false,
  onError = { source, target, exception ->
    logger.error(exception) { "Unable to copy $source to $target" }
    when {
      shouldFailOnError(source, target) -> OnErrorResult.TERMINATE
      shouldSkipOnError(source, target) -> OnErrorresult.SKIP_SUBTREE
      else -> throw exception
    }
  }
)

// ...

fun shouldFailOnError(source: Path, target: Path) { /* ... */ }
fun shouldSkipOnError(source: Path, target: Path) { /* ... */ }
```

In this case we can now provide a Kotlin lambda that decides what to do when an exception is encountered in the copy process. This (as the example indicates) could just mean more granular logging, but it also lets you decide if you should proceed or if you should stop the copy mid-way. Additionally (and perhaps most importantly), this callback starts to help you track what has worked and what hasn't so when the process is done, you can track the percentage of the copy you actually succeeded at completing (assuming, for the moment, it's less than 100%).

The options for an `OnErrorResult` are `TERMINATE` (meaning stop the copy) and `SKIP_SUBTREE` (meaning stop copying this file or directory, and any children it might have) - we can also throw the exception which enables normal application error handling, and may be appropriate in some cases (it's what the default behavior does, for example).

The other lambda available to developers is the `copyAction`, which enables intercepting for a given file or directory what behavior to perform. Like `onError`, this enables tracking the behavior of the copy, but also enables filtering and handling unusual cases with logic. For example:

```kotlin
path.copyToRecursively(
  target = Path.of("some-other-directory"),
  followLinks = false,
  copyAction = { source, target ->
    when {
      source.name.contains("_dont-copy-me") -> CopyActionResult.SKIP_SUBTREE
      source.name.contains("oops-this-is-bad") -> CopyActionResult.TERMINATE
      else -> CopyActionResult.CONTINUE
    }
  }
)
```

In this case the code is looking at the various paths/files in play and making choices on how to proceed. Of course we could also collect the progress in separate data structures to help understand what has been copied or provide other details to the API based on additional details learned from APIs such as `java.nio.file.Files` (e.g. `getPosixFilePermissions` or something equally granular).

Another aspect of copying which is commonly a problem is the "File exists" scenario; what to do when the give file path is already populated? For this, Kotlin introduces the `overwrite` flag as another optional parameter which enables whether or not the files should be overwritten or skipped if they exist in place. This can help with different scenarios and determining the right "idempotent" way to populate a target from a source (especially considering things like retry scenarios when something aborts prematurely):

```kotlin
path.copyToRecursively(
  target = Path.of("some-other-directory"),
  followLinks = false,
  overwrite = true
)
```

To help round out the API needs, there is also a `deleteRecursively`, which as the name indicates, attmepts to perform a recursive delete:

```kotlin
path.deleteRecursively()
```

At this time, this API either completes successfully or fails with an `IOException` if anything can't be deleted, just as a recursive Java API might. As a side note, it wouldn't surprise me if this API also gets callbacks to match the copy variant, as well as behavioral configuration for handling links prior to the removal of the `experimental` annotations.