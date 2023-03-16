---
draft: true
---

In a recent blog post I talked about the benefits of the Kotlin 1.8 [Recursive Copy/Delete File APIs]({{< relref "./recursive-copy-delete.md" >}}), and it occurred to me there are a variety of other gems buried in the Kotlin standard library that can help Java developers be more idiomatic, efficient, and focus more on their actual application code, and less on shoveling data around. Let's take a look at a few of them.

<!--more-->

I should note that in this article I don't plan to go over the collections API in Kotlin, as that may require a more in depth overview, and also I am fully aware this is also not an exhaustive list; the standard Kotlin library is wide and deep, and has a variety of benefits. However, if you are a well-experienced Java developer with less experience with Kotlin, this list may help you with some common places to find things you are used to that may be a little cleaner, a little more efficient, or just fit better with the rest of the Kotlin application code in a given codebase.

Disclaimers aside, I plan to show the naive code for using a Java API to do something, and then show the Kotlin library-based version for comparison.

## System Operations

Some helpers that I think are often overlooked is in the `kotlin.system` package. Notably, Kotlin has lightweight helpers for timing blocks of code:

```kotlin
// Java approach - milliseconds
val start = System.currentTimeMillis()
doSomething()
val end = System.currentTimeMillis()
val duration = end - start

// Java approach - nanoseconds
val start = System.nanoTime()
doSomething()
val end = System.nanoTime()
val durationNanos = end - start

// Kotlin helper: kotlin.system.measureTimeMillis
val duration: Long = measureTimeMillis { doSomething() }
// Kotlin helper: kotlin.system.measureTimeNanos
val durationNanos: Long = measureNanoTime { doSomething() }
```

{{< alert "circle-info" >}}
I should note that, unfortunately, these callbacks do not return any value other than the timing. As a result, if you need to time functions that return values, you will need to use the Java APIs, or more likely use something like OTEL or Micrometer metrics for capturing (but not returning) timed values for aggregation.
{{< /alert >}}

When writing standalone applications it can be handy to assertively end the process. Kotlin has a function to make this easy as well:

```kotlin
// Java
System.exit(123)

// Kotlin
exitProcess(123)
```

## Concurrency



* kotlin.concurrent.thread(...)
* kotlin.concurrent.Lock.withLock(action: () -> T)
* kotlin.concurrent.ReentrantReadWriteLock.read(action: () -> T) and write(action: () -> T)
* TODO - timers

# IO Operations
* kotlin.io.Closeable.use(block: (T) -> R)
* kotlin.io.Console.print, println, readln, readlnOrNull, 
* kotlin.io.ByteArray.inputStream()
* kotlin.io.String.byteInputStream(charset: Charset)
* kotlin.io.InputStream.buffered(bufferSize: Int) and kotlin.io.OutputStream.buffered(bufferSize: Int)
* kotlin.io.InputStream.reader(charset: Charset) and OutputStream.writer(charset: Charset)
* kotlin.io.InputStream.bufferedReader(charset: Charset) and OutputStream.bufferedWriter(charset: Charset)
* kotlin.io.InputStream.copyTo(out: OutputStream, bufferSize: Int)
* kotlin.io.InputStream.readBytes(estimatedSize: Int)
* kotlin.io.Reader.buffered(bufferSize: Int) and Writer.buffered(bufferSize: Int)
* kotlin.io.Reader.forEachLine(action: (String) -> Unit)
* kotlin.io.Reader.readLines(): List<String>
* kotlin.io.Reader.useLines(block: (Sequence<String>) -> T): T
* kotlin.String.reader()
* kotlin.io.Bufferedreader.lineSequence()
* kotlin.io.Reader.readText()
* kotlin.io.Reader.copyTo(out: Writer, bufferSize: Int)
* java.net.URL.readText(charset: Charset) and java.net.URL.readBytes(charset: Charset)
