---
draft: true
---

## IO Operations

By far the other most common place I see Java developers miss key benefits is in working with I/O. Java developers are simply familiar with all the boilerplate that they are used to when it comes to shuffling bytes between inputs and outputs (in fact this problem even happens just in Java itself; developers aren't always aware of functionality available in alternate Java APIs added over the years either, I/O vs NIO vs NIO2, etc).



TODO

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