---
title: 'The Architecture of Ultima 6: Part 2 - Squeezing Every Last Byte'
summary: 'Crunching game data into a couple megabytes'
date: 2020-04-01`
tags: ["classic-gaming", "ultima6", "kotlin"]
series: ultima-6-arch
draft: true
---

With this first journey into how Ultima VI was built, we're going to look at the game files of Ultima 6: how the files were structured on the filesystem, how the engineers saved space, and some of the common patterns they used in persisting some of this sophisticated game data.

<!--more-->

{{< series >}}

In my intro article I pointed out that Ultima 6 is squeezed into 2.2MB of disk storage. To be fair, I didn't intend to paint this as a miraculously small amount of data. For example, the largest NES cartridge ever was 512KB (1/4th of this size); The original Super Mario Bros was squeezed onto an incredibly small **31KB** of ROM storage. However, the comparison is not apples to apples -- Ultima has an incredibly large game world compared to most console games, but it also to ship a lot of "support" with it that is simply built-in to game consoles (such as graphics driver support). And even still, at 2.2MB, the feat of squeezing this entire world in that space still requires a lot of tricks.

# Exploring the Size of Ultima

To understand a little bit more about where this space goes, let's do a quick analysis of the various files in the game, categorizing them into the value they added. This provides some context at a high-level to where the most expensive pieces were, and what was most important to Richard Garriott and his team:

| Purpose | Size (bytes) | Description |
|---------|--------------|-------------|
| Executables, Drivers, and Game Logic | 573,852 | These are all of the (statically linked) binaries that make the game work. Everything from installers, to graphics and sound drivers, to the core game executables that implement the game loop and game logic. Keep in mind Ultima has to support several graphics levels (CGA, EGA, VGA) as well as several sound driver formats -- those all ship here. |
| Menus, HUD, and Cutscene Graphics (Intro, Ending, Character Builder) | 486,405 | This is all of the "one-off" experiences in the game, including cutscenes, the title menu graphics, the gypsy character build experience, as well as the graphics for the HUD |
| Tile Graphics and Animations | 363,532 | The core game graphics are built on indexed tiles (we'll get to that) so most of the core of the pixel graphics, colors, visual dithering, transparency, and animation are hidden in here |
| NPC Conversations, Books and other Scrolls | 340,284 | A big part of Ultima is always conversing and reading. This world building is key in the entire series. These files include all of the conversational logic (including triggering world events through interaction), as well as the text in all world books. |
| Portraits and Informational Text | 306,283 | Ultima 6 was the first Ultima with immersive portraits for all NPCs you encounter. Everyone has a unique face that made them feel more real. This is the cost of doing that. |
| World Data | 119,442 | Tile graphics aren't any good unless they look like an interactive world. This is that structural data, including all of the buildings, dungeons, rivers, and roads  |
| Initial New Game State | 83,594 | You can think of this as the "initial save game" for the game. The starting characters, positions, inventory, NPC behaviors, etc. When you join the world and are in Castle Britannia facing Lord British, this set of files is what puts you there. |
| Music Files | 11,065 | This is all of the music files that can be mixed to go through various music drivers. |

Some really interesting things to note from this breakdown:

* A big part of the game developer reality was supporting all of the various platforms via shipped drivers. This was just a sunk cost for them.
* One-off graphics were humongous and there was no real space benefit to be found with reuse because the graphics, by their nature, were never reused. As a result the 2nd largest piece of the installation by far is the first and last 10 minutes of game play.
* The "text" content of the game is nearly equivalent in size to all of the graphics and world data - this speaks as much to the volume of text as it does to the impressive optimization performed in the tile graphics system.
* The initial game data is only 85K (carefully constructed with a fixed number of initial actors), but as you interact with the world save games will become much larger. The player will create new objects through killing monsters and monsters re-spawning and so forth, so a real save game is broken into smaller chunks, often up to 400K in size.
* For as important as they are for the immersion, the actual music files (being similar to MIDI in nature) are miniscule.

# Someone Please Give Me a Sign

As I mentioned in [the Part 1 Intro]({{< ref "u6-part1.md" >}}), I am a Java developer by historical trade. Anyone that knows much about Java has probably discovered that unsigned values are not officially supported by the language. There are ways to "cope" with unsigned values, but it is generally just hiding unsigned binary values inside of signed variables, and frankly, it's pretty unpleasant.

So, before we read our first byte of game data, if we are going to use the JVM, we already have a problem: all of the game data files in Ultima 6 are unsigned by design. It makes sense when you think about it: most of the game data is representing arrays of information or data; arrays start as zero, and go up from there. So having `0 to 255` values makes a *lot* more sense than `-127 to 128`. This is also true about so many of the other bits and pieces of game data:

* Health does not go negative
* Experience does not go negative
* Item counts do not go negative
* Color indexes and tile indexes make a lot more sense starting at zero

As you can see, negative values are often far less useful in a real game world, so most of the files are unisgned because it's far more efficient (double the storage per byte) and matches the needs of the game.

When looking at the counts of items in the world, it is clear they optimized around powers of two to optimize byte usage:

* Every color palette is 256 indexed colors
* There are 256 NPCs in the starting world
* There is reserved room for 1024 possible object types (though in practice the game only uses 500-some of those)
* The world map is 1024x1024 tiles in size
* There are 2048 distinct tile graphics

Sadly, even as of Java 11, unsigned numeric type support in Java is very basic and requires a lot of judicious use of static helper methods. If I had to use a crystal ball, I would say that they are intentionally waiting for [record types](https://openjdk.java.net/jeps/359) as a solution to start providing more expressive layers on top of the primitives that already exist.

Thankfully, Kotlin already has the fundamentals of inline classes (which are a poor-mans compiler version of record types), and I have [already discussed the experimental Unsigned support for Kotlin]({{< relref "/post/kotlin/unsigned-types.md" >}}) previously, which internally uses inline classes to model unsigned types using the normal signed value types, without any runtime overhead, and a lot of extra compiler confidence. Kotlin will not let you use signed and unsigned integers together; you have to coerce between the two types explicitly (though under the covers this is effectively free). This explicit coercsion prevents a lot of subtle "treating unsigned values as as signed values" bugs.

So, for this project, this is what I intend to use. Note, however, that even with unsigned value types, in Java it's sometimes easier (and just as efficient memory-wise) to simply use a signed integer, so coersing away from explicit unsigned types may still be a practical factor in many cases.

Here is a short example:

```kotlin
val unsignedByte: UByte = 255U
val asInt = unsignedByte.toInt() // as a signed int, still 255
val signed: Byte = unsignedByte // compiler error. Cannot be casted between them
val coerced: Byte = unsignedByte.toByte() // prints -1 (flips to signed)
```

# Which End is Up?

[Endianness](https://en.wikipedia.org/wiki/Endianness) has a complex history in computing. Java historically has always favored big-endian encoding for the various file and stream APIs. This is partially because the defacto standard for network order is big-endian, and many of the stream APIs in Java are designed to work seamlessly between network and filesystem data.

A little bit more about Java's endian choices:
!
* Classes like `Data(Input/Output)Stream` are big-endian only.
* `ByteBuffer` supports both, but defaults to big-endian (though given that buffers often wrap native memory which has endianness based on the hardware architecture, the story under the covers is a bit more complicated than that, but that is an implementation detail for some other day).
* For reading text files, Java either accepts it is "single byte" encoding (endian-irrelevant), or that the character set has some mechanism for determining endianness, like the [UTF-8 Byte Order Mark](https://en.wikipedia.org/wiki/Byte_order_mark) (or BOM).

All of the data files that ship with Ultima 6 are little-endian. As a result, when parsing data with Java, this is another slightly awkward detail.

If you don't know what I'm talking about, let's take a quick journey through byte-order. Endianness defines the order of bytes when storing a multi-byte value like an integer which uses 4 bytes or a long, which uses 8 bytes. As a result, when dealing with single-byte values, the byte order is irrelevant. Here is the difference in visual terms using 2-byte shorts:

```
# 62,100 as a big-endian unsigned short

+----------+----------+
| 11110010 | 10010100 |
+----------+----------+

11110010 00000000 = 61952
00000000 10010100 =   148
                  =======
                    62100


# 62,100 as a little-endian unsigned short
# Note that the "148" bit comes first, and the "61952" bit comes 2nd

+----------+----------+
| 10010100 | 11110010 |
+----------+----------+
```

As you can see in the latter example, the bytes are simply flipped. In effect, big-endian order is how you would read numbers when written down: left to right.

We can look at this in code as well. Here is example code to read an unsigned integer from a byte array in big-endian order in Java:

````java
byte[] bytes = // ...
int byte1 = bytes[0];
int byte2 = bytes[1];
int byte3 = bytes[2];
int byte4 = bytes[3];

int value = (byte1 << 24) + (byte2 << 16) + (byte3 << 8) + byte4;
````

To read an integer in little endian format, you just have to reverse this:

```java
byte[] bytes = // ...
int byte1 = bytes[0];
int byte2 = bytes[1];
int byte3 = bytes[2];
int byte4 = bytes[3];

// Note the order is flipped.
int value = (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
```

This can also be done in Kotlin, though Kotlin does not have `shift` or `bitwise and/or` operators, but instead has infix helper functions (notably `shl` for "shift left"):

```kotlin
val bytes : ByteArray = // ...
val value = (bytes[3] shl 24) + (bytes[2] shl 16) + (bytes[1] shl 8) + bytes[0]
```

Note that Wikipedia as linked above has examples of how you can revese a big-endian integer into a little-endian integer (and vise-versa) -- though if we use a good library we shouldn't have to actually cope with any of this.

For example, with core Java it would be possible to do something like this:

```kotlin
val fileBytes: ReadableByteChannel = FileInputStream(file).channel // ... from filesystem
val buf: ByteBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.LITTLE_ENDIAN)
fileBytes.read(buf)
val someInt: Int = buf.getInt() // reads as byte1, byte2, byte3, byte4 LE format.
```

However I've never found the core byte buffer API particularly enjoyable. Just getting a byte buffer from a file requires a lot of hoop jumping, and managing the position, limit, and offset is always an exercise in scribbling on a piece of paper.

So, instead, I'm going to use [OKIO](https://square.github.io/okio/) from Square. OKIO's `Buffer` and `BufferedSource` classes are very similar to Java's `ByteBuffer` in concept, but the API is just a little more pleasant. Also, it is written-in and has first-class-support-for Kotlin. So it is cleaner in a variety of ways to use.

Rather than make the byte order a property of the buffer object, OKIO just provides alternate little-endian methods to read bytes:

```kotlin
val buf: BufferedSource = file.source().buffer()
val someInt: Int = buf.readIntLe() // buf.readInt() for big-endian
// ...
```

Of course, we have a bug here: we need to clean up after ourselves, as this could leave file handles open. For this, Kotlin has the `use` inline function for all closeables (similar to [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) in Java):

```kotlin
file.source().buffer().use {
  val someInt = it.readIntLe()
  // ...
}
```

# A Few Helpful Extensions

Since we'll be using OKIO and unsigned Kotlin bytes, let's make it a little cleaner with a few extension functions:

```kotlin
// BufferExtensions.kt
fun BufferedSource.readByteToInt() = this.readByte().toInt()
fun BufferedSource.readUByte() = this.readByte().toUByte()
fun BufferedSource.readUByteToInt() = this.readUByte().toInt()
fun BufferedSource.readUIntLe() = this.readIntLe().toUInt()
fun BufferedSource.readUShortLe() = this.readShortLe().toUShort()
fun BufferedSource.readUShortLeToInt() = this.readUShortLe().toInt()
```

All of these are just conveniently chaining some conversions to the most appropriate types:

```kotlin
file.source().buffer().use {
  val someInt: UInt = it.readUIntLe()
  val someByteToInt: Int = int.readUByteToInt()
  // ...
}
```

# Code Organization



# What's Next: Time to Dig In!

Now that we have some foundational fun stuff, it's time to start trying to actually dig in to the Ultima 6 game data. For this first round we need to start with one of the most basic parts of the runtime of a classic VGA game: [Palettes](https://en.wikipedia.org/wiki/Palette_(computing)).

See you in Part 3!
