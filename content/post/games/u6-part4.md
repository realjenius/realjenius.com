---
title: 'The Architecture of Ultima 6: Part 4 - Understanding LZW'
summary: 'Saving space with indexed colors'
date: 2020-04-03
tags: ["classic-gaming", "ultima6", "kotlin"]
series: ultima-6-arch
draft: true
---


<!--more-->

{{< series >}}

# LZW: The Compression of the Time

In 1984, Abraham Lempel, Jacob Ziv, and Terry Welch published the specification for [LZW (Lempel+Ziv+Welch) lossless compression](https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Welch). LZW became very popular in the early personal computing days, and for good reason:

* It uses a variety of tricks like variable-width codes to maximize compression, and does a reasonably good job, especially with text
* The algorithm itself is pretty easy to implement
* It is very fast and can be streamed, meaning you don't have to load all of the code tables up-front, you only need enough memory to maintain the table of codewords.

LZW shows up in specifications all over the place, including GIF, TIFF, PDF, and of course, game files.

LZW was under patent until June of 2003. Given that fact, combined with further improvements in compression research, LZW lost favor for other formats [like DEFLATE](https://en.wikipedia.org/wiki/DEFLATE) and [GZIP](https://en.wikipedia.org/wiki/Gzip).

However, at the time of Ultima 6, LZW was still recognized as a powerful and defacto standard compression tool. Origin licensed LZW to make their games fit on old PCs, and so it shows up all over the Ultima 6 game files.

Thankfully, it is 2020, and all those LZW patents are long-since expired, and as a result we can implement the algorithm ourselves legally free from any risk! For this effort we're only going to implement the decompression algorithm, mainly for two reasons:

1. Space is a *lot* cheaper than it used to be, and so we're not super worried about compressing to save space
2. If we wanted to compress files, we could use more modern and more openly supported algorithms.

# LZW Concepts

At its core, LZW works by using multi-byte codeword identifiers as recursive pointers that build a bytestring, and then reusing those codewords throughout the data set. The more often codewords are reused, and the deeper the chain, the better the compression. The compression and decompression algorithms have to agree on certain details.

Some basic facts about LZW to get us started:

* By it's nature, LZW compresses into multi-byte integer codewords: the original specification is for 12-bit integers. How you manage to store and read those integers is an implementation detail (and there are a lot of details in Ultima 6), but for Java developers you can think of the input and output as an `int[]`. Note however that 12-bit codewords are inherently larger than their single byte inputs, so without careful byte encoding on the output, LZW would never save space.
* The codeword dictionary is typically given a size limit, and once this is reached, new codeword chains are not generated. This is synchronized between the compression and decompression as part of the specification. For example, the GIF image format uses LZW internally, and allows up to `4095` codewords (the full 12-bits of the original specification). Incidentally, this is also the size to which Ultima 6 adheres. However, some uses of LZW will target lower values -- `2047` codewords would ensure you never exceed 11-bit codewords (which might be preferable for a use-case)
* Most LZW examples show compressing text: taking a string and turning it into an array of integer values. In reality LZW can work on any stream of single-byte values. It can be easier to understand starting with strings, however.
* All of the single-unsigned-byte values from 0-255 are logically "pre-seeded" in the codeword dictionary, which is effectively `N -> N`.
* New codewords are added to the dictionary in monotonically increasing order (e.g. `[256, 257, 258, 259, ...]`). This ensures the compressor and decompressor can work together.

Overall the idea of LZW compression is simple, but it can be a little hard to mentally parse at first. Here is an english description:

* We start with all of our single-byte values in the codeword dictionary
* For each input value, we append it to a sequence of un-emitted bytes, and look for that new sequence in our codeword dictionary.
  * If the sequence exists, we move on to the next input character with our expanded sequence
  * If the sequence does not exist, then we emit the codeword for the prior sequence, write a codeword for the new sequence that did not exist, and start the sequence over with our new character.

Let's look at this in Kotlin - I'll be using Strings so we can understand our debug output a little more easily:

```kotlin
val input = // ...

// 1. Pre-seed the dictionary with the ascii characters
//    e.g.: [..., A=65, B=66, C=67, D=68, E=69, ...]
val dictionary: MutableMap<String, Int> =
    (0 until 256)
        .map { "${it.toChar()}" to it }
        .toMap(hashMapOf())

// 2. Initialize our starting codeword and starting sequence
var codeword = 256 // 0-255 are already reserved
var sequence = ""  // starting sequence is empty
val result = arrayListOf<Int>() // not efficient, but simple

// 3. Iterate over our input string:
for (char in input) {
  val nextSequence = "$sequence$char" // add to the sequence
  // 3a: Next sequence is still already in the dictionary
  //     Expand the sequence and keep looking for a miss
  if (dictionary.containsKey(nextSequence)) sequence = nextSequence
  else {
    // 3b: We found a sequence that does not have a codeword.
    //     Store it and increment the codeword
    dictionary[nextSequence] = codeword++

    //     Emit the codeword for the last "matched" sequence

    result.add(dictionary[sequence]!!)
    //     Reset the sequence to the character for our next codeword
    sequence = "$char"
  }
}

// 4. If we have a trailing sequence, emit the codeword for it.
if (sequence != "") result.add(dictionary[sequence]!!)

return result
```

Let's look at what this algorithm would do to a relatively simple string: `AAABBBAAABBB`, by seeing what the sequence, result output, and multi-byte dictionary values look like at each stage of the way:

| Idx | Char | Curr. Seq  | New Seq | Hit? | Out | Dictionary |
|-------|-----------|------------------|--------------|----------|--------|------------|
| 0 | `'A'` | `""` | `"A"` | Hit | `[]` |  |
| 1 | `'A'` | `"A"` | `"A"` | Miss | `[65]` | AA: 256 |
| 2 | `'A'` | `"A"` | `"AA"` | Hit | `[65]` | AA: 256 |
| 3 | `'B'` | `"AA"` | `"B"` | Miss | `[65,256]` | AA: 256, AAB: 257 |
| 4 | `'B'` | `"B"` | `"B"` | Miss | `[65,256,66]` | AA: 256, AAB: 257, BB: 258 |
| 5 | `'B'` | `"B"` | `"BB"` | Hit | `[65,256,66]` | AA: 256, AAB: 257, BB: 258 |
| 6 | `'A'` | `"BB"` | `"A"` | Miss | `[65,256,66,258]` | BBA: 259, AA: 256, AAB: 257, BB: 258 |
| 7 | `'A'` | `"A"` | `"AA"` | Hit | `[65,256,66,258]` | BBA: 259, AA: 256, AAB: 257, BB: 258 |
| 8 | `'A'` | `"AA"` | `"A"` | Miss | `[65,256,66,258,256]` | BBA: 259, AA: 256, AAA: 260, AAB: 257, BB: 258 |
| 9 | `'B'` | `"A"` | `"B"` | Miss | `[65,256,66,258,256,65]` | BBA: 259, AA: 256, AAA: 260, AB: 261, AAB: 257, BB: 258 |
| 10 | `'B'` | `"B"` | `"BB"` | Hit | `[65,256,66,258,256,65]` | BBA: 259, AA: 256, AAA: 260, AB: 261, AAB: 257, BB: 258 |
| 11 | `'B'` | `"BB"` | `"B"` | Miss | `[65,256,66,258,256,65,258]` | BBA: 259, BBB: 262, AA: 256, AAA: 260, AB: 261, AAB: 257, BB: 258 |

Every "hit" is a place where a codeword was reused instead of emitting the raw byte values. Let's talk through the hits:

* At index 1 we get our first miss for `AA`, so we store `AA` as our first new codeword: `256`
* At index 2 (our first non-trivial hit), we found that the codeword for `AA` was already in the dictionary as `256`, so no new codeword was emitted.
* Then, at index 3 `AAB` is not in the dictionary, so we emit `256` to represent our previous subsequence of `AA`, `AAB` is stored as `257`, and then the sequence is reset to `B`.
* Again, at index 5 we see something similar happen with `BB`. Consequently, at index 6 `258` is emitted for `BB`, `BBA` is stored as `258`, and then the sequence is reset to `A`.
* Index 7 matches `AA` again, and index 8 misses with `AAA`, stores `AAA` as `260`, and emits `256` again (**Note: even though that is a sequence we have seen before, it is not already a codeword because of the way LZW works**)
* Index 10 and 11 do the same thing as 7 and 8 again.

As you may notice, if this sequence were repeated over and over, eventually the codeword dictionary would be full of all of the possible matches, and the efficiency of the output would increase. For example, the finished dictionary contained `AAA` and `BBB` but they never were used. If this cycle repeated, eventually we would have all of these in the dictionary:

* `AAA`
* `AAAB`
* `AAABB`
* `AAABBB`

... (and so on). The larger the codeword, the more efficient the compression.

Now that we have a scheme for compressing, what does decompression look like? The decompression routine must mimic the codeword dictionary behavior of the compressor. Here is a short english description:

* We start with all of our single-byte values in the codeword dictionary (just like compress)
* The first sequence to match is the first byte in the input stream. We know the compressor will never emit a multi-byte codeword for the first value.
* For each compressed value:
  * If the dictionary contains the codeword, emit the value from the dictionary, add the current sequence plus the first character of the dictionary of the matched codeword into the dictonary as the next codeword, and set the current sequence to the matched codeword
  * If the dictionary does not contain the codeword, emit the current sequence plus the first character of the current sequence, add the current sequence plus the first character of the current sequence to the dictionary as a new codeword, and set the sequence to the previous sequence plus the first character of the previous sequence.

Let's look at some code:

```kotlin
// 1. Pre-seed the dictionary with the ascii characters
//    e.g.: [..., 65=A, 66=B, 67=C, 68=D, 69=E, ...]
val dictionary: MutableMap<Int, String> =
    (0 until 256)
        .map { it to "${it.toChar()}" }
        .toMap(hashMapOf())

// 2. Setup our first codeword, emit the first codeword as an ASCII character
//    and capture it as our starting sequence
var codeword = 256
var sequence = "${compressed[0].toChar()}"
val result = StringBuilder(sequence)

// 3. Starting at index 1, iterate codewords
for (index in 1 until compressed.size) {
      val next = compressed[index]
      // 3a. Either use the codeword value, or generate the next value as the
      //  current sequence + the first character of the current sequence
      val entry = dictionary[next] ?: "$sequence${sequence[0]}"

      // 3b. Emit the entry value
      result.append(entry)

      // 3c. Store the entry value as a new codeword
      dictionary[codeword++] = "$sequence${entry[0]}"

      // 3d. Update the tracking sequence to either the matched codeword,
      //     or the sequence + char[0]
      sequence = entry
    }
    return result.toString()

```

This one is admittedly a bit harder to understand how it works, at first.

The first thing I noticed was the lack of symmetry. The compression algorithm only updates the dictionary on misses, whereas decompression adds a codeword on every step. This is a by-product of the fact that every value emitted by the compression algorithm is tied to a codeword in some way or another; the omitted steps on the compression algorithm are not in the input stream here. In effect, adding every sequence chain into the dictionary (irrespective of a hit or miss) replicates what will happen as a result of the compression algorithm.

The next really confusing piece for me was the use of `sequence + sequence[0]`. At first I could not understand how that possibly would result in matching the input. However, the key is to understand what a "codeword miss" means on the decompression. This does not mean "just any new codeword"; remember: the decompressor is always generating new codewords even on hits.

For the decompressor to encounter a new codeword that wasn't already generated by a previous step, that codeword *has* to be composed of the *immediately previous* encountered codeword values (which is the mind-bendy part). All single byte characters will already be in the dictionary as codewords, and new codewords are added by the decompressor even as it matches single byte values. Let's consider two different cases:

`AAAB` will result in these compressed bytes: `[65, 256, 66]` because:

* `A` matches a codeword, so we iterate without emitting anything
* `AA` does not match, so we emit the *previous* sequence (`65`), and create new codeword for the miss: `256=AA`, and set the starter sequence to `A`
* `AA` **does** match this time, so we hold on to `AA` and iterate. Here we are referring to a codeword we **just added -- this is our special case**.
* `AAB` does not match, so we emit the *previous* sequence (`256`), create a new codeword for the miss: `257=AAB`, and set the starter sequence to `B`
* We have no more characters, so we emit `66` for B.

But what about `AABAA`?  This results in the very different `[65, 65, 66, 256]`. But why?

* `A` matches a codeword, so we iterate without emitting anything
* `AA` does not match, so we emit the *previous* sequence (`65`), and create new codeword for the miss: `256=AA`, and set the starter sequence to `A`. **From this point on, 256 is in the dictionary already**.
* `AB` does not match, so we emit the *previous* sequence (`65`), and create new codeword for the miss: `257=AB`, and set the starter sequence to `B`
* `BA` does not match, so we emit the *previous* sequence (`66`), and create new codeword for the miss: `258=BA`, and set the starter sequence to `A`
* `AA` **does** match this time, so we hold on to `AA` and iterate. Here we are referring to an **older codeword**.
* We have no more characters, so we emit `256` for `AA`.

In effect, this edge case illustrates that the decompressor is "one step behind" the compressor in terms of keeping the dictionary current. The decompressor has to know that to get "immediate" codeword reuse, the new codeword has to have been built on top of characters recently repeating from previous codewords. In the second case where the repeating was found previously, the codeword was already in the dictionary from earlier work for both the compressor and the decompressor, so there is no need to generate it on the fly.

Let's go through our decompression steps for `AAABBBAAABBB`:

| Idx | Codeword | Curr Seq. | New Seq. | Hit | Out | Dictionary |
|-------|----------|------------------|--------------|----------|--------|------------|
| 1 | `256` | `"A"` | `"AA"` | Miss | `"AAA"` | 256: AA |
| 2 | `66` | `"AA"` | `"B"` | Hit | `"AAAB"` | 256: AA, 257: AAB |
| 3 | `258` | `"B"` | `"BB"` | Miss | `"AAABBB"` | 256: AA, 257: AAB, 258: BB |
| 4 | `256` | `"BB"` | `"AA"` | Hit | `"AAABBBAA"` | 256: AA, 257: AAB, 258: BB, 259: BBA |
| 5 | `65` | `"AA"` | `"A"` | Hit | `"AAABBBAAA"` | 256: AA, 257: AAB, 258: BB, 259: BBA, 260: AAA |
| 6 | `258` | `"A"` | `"BB"` | Hit | `"AAABBBAAABB"` | 256: AA, 257: AAB, 258: BB, 259: BBA, 260: AAA, 261: AB |
| 7 | `66` | `"BB"` | `"B"` | Hit | `"AAABBBAAABBB"` | 256: AA, 257: AAB, 258: BB, 259: BBA, 260: AAA, 261: AB, 262: BBB |

* Index 0 happens outside the loop and seeds the "Starting point" for decompression.
* Index 1 encounters a new codeword. The decompressor has not generated it yet, therefore it must be the last codeword and the first character of the last codeword again.
* Index 2 is a hit (single-byte), so we reset our sequence to `B`, but also capture `AAB` in the dictionary to match the compression codeword behavior.
* Index 3 is another miss. This follows the same pattern as index 1, and adds `BB` to the dictionary.
* Index 4 encounters the codeword produced on index 1, and adds `BBA` to the dictionary.
* Index 5 is a single byte hit, and adds `AAA` to the dictionary.
* Index 6 encountered the codeword produced on index 3 and adds `AB` to the dictionary
* Index 7 is a single byte hit, and adds `BBB` to the dictionary.


# Ultima 6's Flavor of LZW

Within the U6 implementation of LZW segments inside of Ultima 6, the potentially LZW-compressed byte segments always start with a 4 byte integer indicating the uncompressed size of the data after the LZW decompression is complete. If this length value is `0`, then it implies the data is not LZW compressed, and should be treated as-is.

It's worth noting that LZW is not always applied to an entire file in Ultima 6 -- sometimes it is applied to a subset of data in the file based on other indicators in the file format itself.

** Call out end and re-init markers

TODO TODO TODO


## Efficiency in Persistence

Ultima 6 needs a scheme to store LZW data efficiently. It is not as simple as calculating the number of omitted characters to determine how efficient LZW will be. Remember that every "new" codeword (which implies compression) is at least 9-bits large or larger. A naive binary format for an LZW stream would be to just emit 12-bit values. However, using our previous `AAABBBAAABBB` example, what would be the storage use if we just used whole bytes? Well, this is a complicated question. We can start with ASCII/single-byte storage of our data prior to compression, as that is easy to understand:

```
1 byte (8 bits) * 12 characters = 12 bytes (96 bits)
```

So our target is 96 bits, or perhaps more accurately 12 bytes, since a byte is our smallest discrete unit of transmittable data.

As above I mentioned that, logically, we can treat the codewords as a stream of integers. While we were able to logically reduce four positional values through codeword reuse, a full Java integer is 4 bytes (32-bits), so if we were to simply write all of the codewords naively as integers, then things don't look so great:

```
4 bytes (32 bits) * 8 codewords = 32 bytes (256 bits)
```

So our compression algorithm made our data 2.5 times larger. Not a great start. Of course, this is way too naive. We already know that our maximum codeword is 12-bits. So, we know that we can write them all as Java `short` values (2 bytes per integer). But there is still a lot of waste there: 4 bits per codeword will never be used. So let's assume that we can find a way to efficiently write our 12 bit values, how much closer does that get us?

```
# Writing short values
2 bytes (16 bits) * 8 codewords = 32 bytes (256 bits)
# Writing 1.5 byte values
1.5 bytes (12 bits) * 8 codewords = 12 bytes (96 bits)
```

OK, so we finally break even on our very specific example, if we can write partial bytes... but we don't save any space. Of course, ideally it should get more efficient with larger compressed values, but there are values like `ABCDEFGHIJKL` which will not compress at all, and will be significantly larger still (`144 bits/18 bytes` vs the uncompressed `96 bits` if you're curious).

However, using 12 bits for every codeword is still not the best we can do. In fact, we know that all of our codewords can fit in 9 bits in our single example, so in theory we should be able to do this instead:

```
9 bits * 8 codewords = 72 bits
```

Aha! Finally some space savings. So if we can consider minimal codeword size, what are the bit ranges we have to work with? Let's look at (yet another) table:

| Codeword Range | Minimal Space Required |
+----------------+------------------------+
| 0 - 255        | 8 bits                 |
| 256 - 511      | 9 bits                 |
| 512 - 1023     | 10 bits                |
| 1023 - 2047    | 11 bits                |
| 2047 - 4095    | 12 bits                |

Knowing what we do about

* Because codewords are monotonically increasing, we will know at any point what the maximum codeword size can be (for a time 9 bits is the maximum, for example)
* Based on the shared "maximum codeword size" of the decompressor and compressor, 12 bits is the absolute maximal codeword size, so it will never get larger than that
* The larger the codewords get, the more space they take to store, *but* the more savings they almost certainly imply. So in theory by the time we are using 12-bit codewords we are ideally seeing large amounts of reuse.

Here is a visual example of how codewords could span byte boundaries:

```
+-------------------+-------------------+-------------------+------------
|    CODEWORD 1     |    CODEWORD 2     |    CODEWORD 3     |   [...]
+-------------------+-------------------+-------------------+------------
|0|1|2|3|4|5|6|7|8|9|0|1|2|3|4|5|6|7|8|9|0|1|2|3|4|5|6|7|8|9|   [...]
+-------------------+-------------------+-------------------+------------

+-----------------+-----------------+-----------------+-----------------+
|      Byte 1     |      Byte 2     |      Byte 3     |      Byte 4     |
+-----------------+-----------------+-----------------+-----------------+
```

So ideally we can build a theoretical model where the early part of our LZW data is all 9 bit values, then it transtions to 10 bit values, and then eventually transitions into 11 bit and even 12 bit values if it is large enough, and that model is what the Ultima 6 implementation focuses on.

1. Codeword size is calculated based on how many codewords have been encountered.
2. Even though it is possible that a variety of codewords early on will be 8-bits, we can have a 9-bit codeword as the very first codeword. As a result, 9-bits are used as the codeword size until enough codewords are generated that the next entry might require 10 bits, at which point 10 bits will be used as long as possible, and so on. Since codewords are monotonically increasing, this edge case is easy to see coming.
3. The bits are packed across byte boundaries to ensure there is no waste. Therefore, a 9 bit codeword will span two bytes, but share bits in those bytes with other codewords.
4. We can't easily detect the optimal size for **each** codeword (it could be 8-bits, or 9-bits, or more) so once we reach a certain maximal codeword size, *all* codewords are persisted at that size.

So, the larger the compressed payload is, the larger the average codeword entry will be, but also, the higher the benefit that each codeword will provide, on average. As a result the net gain should still continue to increase. In effect, a U6 LZW data stream will be structured like this:

```
[ 9 bit codeword entries ][ 10 bit codeword entries ][ 11 bit codeword entries ][ 12 bit codeword entries ]
```
