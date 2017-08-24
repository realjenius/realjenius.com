---
title: "Kotlin Libraries: I/O operations"
date: 2017-08-20
series: kotlin-libraries
tags: ["kotlin","java","kotlin-libraries"]
---

This is the first in a series of articles that will quickly go over some of the more interesting bits of the Kotlin standard libraries for Java. Today's run-through is about some of the more interesting affordances for I/O based programming.

<!--more-->

{{< series >}}

For users of the Guava library from Google, many of the tools afforded by the Kotlin standard library will seem familiar.

# Strings and Readers

In Java, it's often common to interface with an API that generalizes around I/O APIs, where-as you are dealing with in-memory string data. In these cases you will typically do one of two things (depending on the case):

* Wrap the string in a reader
* Encode the string into bytes and wrap the bytes in a byte array input stream

```java
Reader sr = new StringReader("test-string");
InputStream in = new ByteArrayInputStream("test-string".getBytes("UTF-8"));
```

In Kotlin they have made these operations first-class citizens of the String type by using an extension method on String:

```java
val sr = "test-string".reader()
val in = "test-string".byteInputStream(Charsets.UTF_8)
```

This second example also shows that Kotlin (similar to [Guava](...)) has introduced a constants class for charsets that are reasonably available on all JVM platforms.

Similar to encoding strings into streams and readers, Kotlin also makes affordances to decoding back to Strings. There are a number of these available for a variety of uses.

First, here is how to turn a reader into a String in Java. Without something like "Guava", it can be pretty gross:

```java
StringBuilder builder = new StringBuilder();
boolean proceed = true;
char[] chars = new char[CHAR_BUFFER_LEN];
while(proceed) {
  int charsRead = reader.read(chars, 0, chars.length);
  if(proceed = charsRead>0) {
    builder.append(chars, 0, charsRead);
  }
}
String text = builder.toString();
```

You're left with a lot of self-buffer management here; not ideal. Here's the same functionality in Kotlin:

```java
val reader = fromSomewhere()
val text = reader.readText()
```

When dealing with byte arrays, you have a similar complexity in Java. The cleanest option is likely to wrap the byte array in a reader and use the above mechanism:

```java
InputStream in = fromSomewhere()
Reader r = new InputStreamReader(in, "UTF-8");
StringBuilder builder = new StringBuilder();
boolean proceed = true;
char[] chars = new char[CHAR_BUFFER_LEN];
while(proceed) {
  int charsRead = reader.read(chars, 0, chars.length);
  if(proceed = charsRead>0) {
    builder.append(chars, 0, charsRead);
  }
}
String text = builder.toString();
```

In Kotlin, there is also a mechanism to turn any input stream into a reader, but as before, it is made available on input stream itself:

```java
val in = fromSomewhere()
val text = in.reader(Charsets.UTF_8).readText()
```

Kotlin does support controlling the size of the underlying buffer created to handle this read operation (much like the `CHAR_BUFFER_LEN` in my first example), but you aren't required to use it.

# Byte Streams

Similar to readers, you are mostly left to your own devices when it comes to streams in Java. The newer NIO based channels have some different benefits, but in the end most Java solutions are petty low-level.

For example, to do a basic "inputstream" to "outputstream" copy in Java you wind up with something like this:

```java
InputStream in = fromSomewhere();
OutputStream out = fromSomewhereElse();
byte[] buffer = new byte[BUFFER_SIZE];
int len = in.read(buffer);
while (len != -1) {
    out.write(buffer, 0, len);
    len = in.read(buffer);
}
```

This read chunks up to `BUFFER_SIZE` in length from the input stream and writes them to the output stream.

As with the readers, this is made possible in Kotlin via the use of the `copyTo` operation on inputstream.

```java
val in = fromSomewhere()
val out = fromSomewhereElse()
in.copyTo(out)
```

Also as with the reader example, this method supports providing the buffer size to be used (defaults to 8K bytes).

When dealing with any particular input stream or output stream you can ask to buffer them correctly:

```java
val in : InputStream = // ... from somewhere
val bufferedIn = in.buffered()

val out : OutputStream = // ... from somewhere
val bufferedOut = out.buffered()
```

You can also automatically translate from a raw in/out stream into a buffered reader:

```java
val in : InputStream = // ... from somewhere
val readerIn : Reader = in.bufferedReader()

val out : OutputStream = //
val writerOut : Writer = out.bufferedWriter()
```

Any and all byte arrays can be translated into an input stream as well:

```java
val bytes = byteArrayOf(1,2,3)
val in = bytes.inputStream()
```

# File Management

In Java 6 when you wish to copy a file, you are left on your own to juggle streams. On Java 7 and above you *do* have the option to use `Files.copy(...)`, which is capable of a variety of functionality, but is still primitive in functionality.

You can use the `File.copyTo` method to copy from one file to another.

```java
val file : File = ...
file.copyTo(File("some-target-file"))
```

This method also lets you control if you want to overwrite or not with a 2nd optional parameter.

One added benefit of this method is that it auto-creates any missing parent directories (`mkdirs()`), which is almost always desired in cases like this.

Kotlin also introduces recursive operations. This is an area where Java has always been fairly weak. In Java, to copy a nested directory of files you have to walk the tree using recursion. Most solutions suggest using Apache Commons, simply because Java at its core doesn't solve this problem. In Java 7 you can also use a walk-file-tree operation as indicated here: [StackOverflow Copy Directory](https://stackoverflow.com/questions/6214703/copy-entire-directory-contents-to-another-directory). Deletes are similarly headachy, and again most people fall back to Apache Commons.

In Kotlin these have both been made a one-line operation (assuming the limitations fit your constraint):

```java
File("some-directory").copyRecursively(File("some-target-directory"))

File("some-target-directory").deleteRecursively()
```

There are additional parameters available on the copyResursive operation to give you some control:

```java
File.copyRecursively(
  target : File,
  overwrite: Boolean = false,
  onError (File, IOException -> OnErrorAction { _, exception -> throw exception })
)
```

As you can see, by default, this method refuses to overwrite, and in the case of an error re-throws the error.

The documentation lists the types of errors that this handler may be given:

> Exceptions that can be passed to the onError function:
>
> * NoSuchFileException - if there was an attempt to copy a non-existent file
> * FileAlreadyExistsException - if there is a conflict
> * AccessDeniedException - if there was an attempt to open a directory that didn't succeed.
> * IOException - if some problems occur when copying.

Some notes:

* Under the covers this uses `File.copyTo`, which as discussed above is a basic stream-to-stream copy function. This means it benefits from the same `mkdirs()` pre-step.
* This makes no special affordances currently for symbolic links, meaning any symbolic link will be followed and copied deeply. Java 7's [Files.copy](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#copy-java.nio.file.Path-java.nio.file.Path-java.nio.file.CopyOption...-) method, but note that this is not recursive; it simply supports the `NO_FOLLOW_LINKS` directive. Ideally Kotlin would support this in the future, when they upgrade to supporting higher than Java 6 as a baseline platform.
* This copy has no ability to copy attributes of the underlying files today. Again, this is possible with the `Files.copy` utility, and once Kotlin removes Java 6 as a baseline, it will probably be able to take advantage of that as well.

# Tree Traversal

Many examples [like this one](https://dzone.com/articles/what%E2%80%99s-new-java-7-copy-and) suggest that to copy recursively in Java 7+ a `FileVisitor` should be used applied against the Java `walkFileTree` operation. Kotlin also introduces a file-tree-walker API, except it is designed to play ball with the functional nature of Kotlin instead of the visitor pattern as used in Java. Since the FileTreeWalk object is a sequence, and Kotlin allows sequences to be used in for comprehensions, you can simply iterate the tree depth-first. Here's a brief example:

```java
for (src in File("some-file").walkTopDown().onFail { file, ex -> throw RuntimeException(ex) }) {
   // for each file or directory do something
}
```

Additionally, since Kotlin sequences are operable comparably to Java streams, you can use any sort of map / filter / collect transformation you wish.

Note that there are a series of other customizable hooks on the file tree walk API, allowing you to intercept when it traverses:

```java
File("some-file").walkTopDown()
  .maxDepth(5)
  .onFail { file, ex -> ... }
  .onEnter { file -> ... }
  .onLeave { file -> ... }
```

Finally, note that there is also `walkBottomUp`, as well as `walk(FileWalkDirection)`, both of which allow you to control the direction over the files that is traversed.

The distinction is whether the directory is visited before or after the underlying files and child directories, or before. In both cases this is depth-first.

# Line Traversal

Kotlin also provides a few operations for one of the most common uses of files: traversing the lines of the file.

First, Kotlin supports reading all of the lines (as defined by the platform line separator) into a kotlin `List<String>`:

```java
val lines = File("some-file").readLines()
// with an optional charset:
val lines = File("some-file").readLines(Charsets.UTF_8)
```

This operation is also more generally available on any reader:

```java
val r = // some reader
val lines = r.readLines()
```

Alternatively, if you consider a file of a large size, it instead might make sense to walk the lines using a Kotlin sequence, and let Kotlin handle closing the stream when done. Specifically:

```java
val r = // some reader
val result = r.useLines { seq  ->
  // do something with a Sequence<String>
  // return something as "result"
}
```

Additionally, any reader can be transformed into a `Sequence<String>` by using the line sequence method instead:

```java
val r = // some reader
val seq = r.lineSequence() // Sequence<String>
```

Note, however, in this case the reader will not be closed as a result of the operation; you must close it yourself.

# File Management Miscellany

Lastly, there are a variety of additional largely unrelated tools and shortcuts available for working with files, so I'll just list those here:

```java
val reader = File("some-file").bufferedReader()
val writer = File("some-file").bufferedWriter()
val bytes = File("some-file").readBytes() // returns a ByteArray
File("some-file").appendText("Add some text to the end of the file")
File("some-file").appendBytes(byteArrayOf(1,2,3))
```

URL also has the benefit of the short-cut read-to-datatype functions:

```java
val url : URL = // from somewhere
val bytes = url.readBytes()
val text = url.readText()
```

# Extensions, Inlines, and Patterns of Use, Oh My

It's worth reiterating that most operations in this list are decorated on to the Java types using Kotlin's extension system, and most of these functions also use inline.

For example, `File.reader` can be found in `kotlin.io.FileReadWrite.kt`, and it looks like this from a declaration standpoint:

```java
@kotlin.internal.InlineOnly
public inline fun File.reader(charset: Charset = Charsets.UTF_8): InputStreamReader = inputStream().reader(charset)
```

There is a lot to soak in if you are a prospective Kotlin library author here:

* One-line functions, like Scala, do not require brackets
* This function is a clear poster-child of inlining as it has no variables and complexity that makes the function anything but an organization detail
* Defaults are a good thing!
* Extension methods are a great way in Kotlin to provide additional value without moving away from the core libraries. In Java, library developers would never add "java.io" operations to String, as it would fundamentally break the modularity organization. In Kotlin it's possible via the use of extension methods, but is only added in the case this code is evaluated.
* This is composed from other inline functions - this is where inline functions really shine as the deeper and more composed the operations the less you pay a cost you normally would.
