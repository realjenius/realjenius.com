---
title: 'The Architecture of Ultima 6: Part 3 - Parsing Indexed Palettes'
summary: 'VGA Colors and '
date: 2020-04-02`
tags: ["classic-gaming", "ultima6", "kotlin"]
series: ultima-6-arch
draft: true
---

One of the most foundational pieces of displaying anything out of the Ultima 6 data is knowing the colors to display. For a variety of reasons, Ultima 6 uses indexed color palettes, which means we will need to learn about the "how" and the "why" before we can go any further.

Thankfully, the palette files also happen to be some of the easiest to parse. So, let's get to it!

<!--more-->

{{< series >}}

# Indexed Colors Overview

Ultima 6, like many, many PC games of the era (and basically everything that shipped on a console platform), uses indexed palette colors. [Indexed Colors](https://en.wikipedia.org/wiki/Indexed_color) have been around for a very long time as a concept. The idea is to create an indirection between potentially large amounts of graphical data and the colors that represent them. The fewer colors used, the more efficient indexed color tables will be.

Consider any pixel-based graphic. A simple implementation to store color data for this graphic might consider storing the RGB values for every pixel in a big array. In effect:

{{< figure src="/img/articles/u6/rgb_pixel_data.png" caption="Don't ever store images like this..." >}}

Of course, this is not very efficient. As described here, we're looking at 3 bytes for every single pixel. Even a 16x16 pixel graphic would take a surprisingly large `16 * 16 * 3 = 768 bytes` to store. This will use a lot of storage on disk when you consider we have a `16,384 x 16,384` pixel map in Ultima 6: that would take a whopping `805 megabytes`, assuming we don't do anything fancy; but of course we will do many fancy tricks.

The idea of indexed colors is simple: instead of storing the actual color data, what if we store a *logical* value for each pixel that points to the more complex color data. For example:

{{< figure src="/img/articles/u6/indexed_colors.png">}}

To understand how much storage (or memory) this might theoretically save, we have to know how many unique "color ids" we will have. As it turns out, this comes down to understanding the graphics standards. So let's go down another rabbit-hole. First, we should understand that the native resolution of Ultima 6 was `320 x 200`, as that will matter here:

* [CGA](https://en.wikipedia.org/wiki/Color_Graphics_Adapter) - "Color Graphics Adapter" - A standard from 1981. For most practical measures CGA graphics were limited to **4 colors** at `320x200`. **Side Note** - This is a bit of a misnomer as the [8-Bit Guy has shown](https://www.youtube.com/watch?v=niKblgZupOc) - first: at lower dimensions, higher color ranges were possible, but also: with NTSC artifacting you can "mix" 16 colors on some old hardware that supported CGA on composite video mode (he shows some interesting color mixes with [Maniac Mansion](https://en.wikipedia.org/wiki/Maniac_Mansion)), though that is really just academic for us at this point, as Ultima 6 did not focus on this usage pattern. The 8-bit guy video also actually has CGA graphic video snippets of Ultima 6 in 4-color mode, if you're curious. {{< figure src="/img/articles/u6/cgau6.png" caption="4 Colors Aren't Enough. Courtesy of Nuvie" link="http://nuvie.sourceforge.net/?ss=1">}}
* [EGA](https://en.wikipedia.org/wiki/Enhanced_Graphics_Adapter) - "Enhanced Graphics Adapter" - A standard from 1984. EGA at `320x200` supports a 16-color palette. {{< figure src="/img/articles/u6/egau6.png" caption="16 Colors is very Try Hard. Courtesy of Nuvie" link="http://nuvie.sourceforge.net/?ss=1">}}
* [VGA](https://en.wikipedia.org/wiki/Video_Graphics_Array) - "Vidoe Graphics Array" - Starting in 1986, in `320x200` mode, VGA supported **256 colors** per pixel. This era is, frankly speaking, the heart of the nostalgia of my childhood. {{< figure src="/img/articles/u6/vgau6.png" caption="The Glory of 256 Color Choices. Courtesy of Nuvie" link="http://nuvie.sourceforge.net/?ss=1">}}

So, for this Ultima 6 journey our target is VGA graphics (the only one where you can tell what is going on), and as a result we know we have up to `256` colors, meaning all of our color IDs can fit into single 8-byte range. As a result, even with VGA, this "Color ID" format immediately offers a 3:1 savings over the RGB format described above. Now our 16x16 graphic will only take `16 x 16 = 256 bytes`, which is much better!

Admittedly, it's possible we could compress our RGB storage below 3 bytes, and, in fact, Ultima 6 does in fact use an index range of `0-63` for each RGB value it renders, so a single byte could be used with clever bit-shifting.

However, indexed colors add one other key benefit: the color indirection allows us to perform [Palette Rotation](https://en.wikipedia.org/wiki/Color_cycling), which Ultima 6 uses to great effect to animate things like magic spells and fire. Look at this example of a smoldering fireplace in Ultima 6: this animation is purely produced through the use of color shifting on a fixed palette:

TODO fireplace animation graphics

# Parsing Palettes

OK, so what do the Ultima palette files look like? It turns out they are literally *just* unsigned RGB values in color index order. There are two primary palette files in Ultima 6:

* `U6PAL` - This contains the main game palette
* `PALETTES.INT` - This contains palettes for each of the cutscene animations - 6 of them specifically

Looking at `U6PAL`, the contents simply look like this:

```
Color: [       Index #1       ][       Index #2       ]
Bytes: [  00  ][  01  ][  02  ][  03  ][  04  ][  05  ] [...]
Repr : [ RED  ][ GRN  ][ BLUE ][ RED  ][ GRN  ][ BLUE ]
```

Parsing this file is quite straightforward in Kotlin:

```kotlin
data class Rgb(val red: Int, val green: Int, val blue: Int)

fun load(file: File) = file.source().buffer().use { source ->
  (0 until 256).map {
    Rgb(source.readUByte(),
        source.readUByte(),
        source.readUByte()
    )
  }
}
```

That's it? Yup, that's it! This will produce a `List<Rgb>` with 256 entries. However, most rendering engines actually use a range from `0-1` as a floating point value for each of the color components. And, recall that I said Ultima uses a range from `0-63` for each component. This is easily solved:

```kotlin
data class Rgb(val red: Float, val green: Float, val blue: Float)

fun load(file: File) = file.source().buffer().use { source ->
  (0 until 256).map {
    Rgb(translateColor(source.readUByte()),
        translateColor(source.readUByte()),
        translateColor(source.readUByte()))
  }
}

private fun translateColor(value: UByte) = value.toFloat() / 63
```

Now we have some data that is more closely representative of modern usage patterns.

What about `PALETTES.INT` though? Well, all we need to do there is extend things a bit further to accept a file can contain multiple palettes stacked one immediately after another. Let's add another data class to help with organization:


```kotlin
data class Rgb(val red: Float, val green: Float, val blue: Float)

data class Palette(val colors: List<Rgb>) {
  companion object {
    fun loadAll(file: File, count: Int) = file.source().buffer().use { source ->
      (0 until count).map { load(source) }
    }

    fun load(source: BufferedSource) = Palette(
      (0 until 256).map {
        Rgb(translateColor(source.readUByte()),
            translateColor(source.readUByte()),
            translateColor(source.readUByte()))
      }
    )

    private fun translateColor(value: UByte) = value.toFloat() / 63
}
```

This new code can now parse all of the color palettes for Ultima!

```kotlin
val cutscenePalettes : List<Palette> =
  Palette.loadAll(GameFiles.loadExternal("PALETTES.INT"), 6)
val gamePalette : Palette =
  Palette.loadAll(GameFiles.loadExternal("U6PAL"), 1)[0]
```

But of course, this doesn't prove anything. It'd be nice to have some evidence this isn't just total garbage data.

# Sanity Checking

It's time we finally try out some LibGDX code.
So, let's code a quick GUI to print our palette colors!

+ palette rotation code
