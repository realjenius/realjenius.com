---
title: 'The Architecture of Ultima 6: Intro - Revisiting the Past'
summary: 'Exploring the fascinating architecture of one of the most innovative games of its time'
date: 2020-03-28
tags: ["classic-gaming", "ultima6"]
series: ultima-6-arch
draft: true
---

Since I was a little kid, I've been an avid gamer of both PC games and console games all the way back to my Atari 2600 and my Apple II+. At the same time, I spent a number of years as a game programmer. Truth be told, however, as a game programmer I was so worried about keeping servers alive and dealing with heavy traffic and distributed databases, that I never really got a chance to learn from the past like I have in so many other aspects of computer science.

Today I start a new series where I take a (hopefully very) deep dive into the architecture of one of my favorite games as a child: [Ultima VI - The False Prophet](https://en.wikipedia.org/wiki/Ultima_VI:_The_False_Prophet).

[Richard Garriott](https://twitter.com/richardgarriott) has always been an innovator, and this game definitely advanced the state of the art for big open-world RPGs, and through this series we will see together that there is a ton to learn by really digging into the amazing work of these amazing developers.

<!--more-->

{{< series >}}

# Up-Front Acknowledgments: Recognizing The Real Hard Work

{{< figure src="/img/articles/u6/u6-cover-art.png" class="figureright" link="https://en.wikipedia.org/wiki/Ultima_VI:_The_False_Prophet" caption="Savior or Murderer?" >}}

Before I go anywhere on this journey through Ultima VI (which I will henceforth call Ultima 6 to avoid constantly typing VI), I absolutely must call out both the [Ultima Fandom Wiki](https://ultima.fandom.com/wiki/Ultima_VI_Internal_Formats) and more directly the [**Nuvie** Modern Ultima 6 Game Engine Project](http://nuvie.sourceforge.net/). While I would love to say that all of my work here was produced solely on my own, in reality I'm simply learning directly from the incredible work of others.

[Nuvie](http://nuvie.sourceforge.net/) (pronounced `New-Vee` - from **N**ew **U**ltima **VI** **E**ngine) (much like [Exult](http://exult.sourceforge.net/) for Ultima 7 and [Pentagram](http://pentagram.sourceforge.net/) for Ultima 8) is an end-to-end remake of Ultima 6's game engine, built in C/C++ and designed to run on modern hardware (using a much more portable architecture).

Nuvie uses every ounce of the original game files (and a lot of reverse engineering) to produce a version of Ultima 6 that:

* is highly faithful to the original
* fixes all of the issues with running the original Ultima 6 on modern hardware
* provides a variety of enhancements and improvements to make it easier to play without compromising the original vision of the game
* is extensible and can support the subsequent games forked from the original U6 game engine: [Worlds of Ultima: The Savage Empire](https://en.wikipedia.org/wiki/Worlds_of_Ultima:_The_Savage_Empire) and [Worlds of Adventure 2: Martian Dreams](https://en.wikipedia.org/wiki/Ultima:_Worlds_of_Adventure_2:_Martian_Dreams).

{{< figure src="/img/articles/u6/nuvie_logo.png" link="http://nuvie.sourceforge.net">}}

I highly recommend checking out the Nuvie project. What I'm doing here is an exercise in learning all about the great history and ideas of this game, but is not meant to replace or usurp the incredible work of this team, and the results of their labor.

As a result, any code I'm producing as part of this project is licensed to match Nuvie, and is attributed directly to their efforts -- I admit unabashedly that much of the code is a lift of the ideas they have in their own project.

# A Short Journey Back in Time

The year was 1990 - I was 9 years old (dating myself), and the PC gaming world was rapidly exploding into the mouse-driven VGA era. 1989 and 1990 were amazing years for PC gamers. Just a few of the games that came out in that time frame:

* Ultima 6 (of course)
* [Wing Commander](https://en.wikipedia.org/wiki/Wing_Commander_(video_game))
* [Kings Quest 5](https://en.wikipedia.org/wiki/King%27s_Quest_V)
* [Space Quest 3](https://en.wikipedia.org/wiki/Space_Quest_III)
* [The Duel: Test Drive 2](https://en.wikipedia.org/wiki/The_Duel:_Test_Drive_II)
* [Sim City](https://en.wikipedia.org/wiki/SimCity)
* [Prince of Persia](https://en.wikipedia.org/wiki/Prince_of_Persia)

{{< figure src="/img/articles/u6/sierra.jpg" class="figureright" >}}
{{< figure src="/img/articles/u6/origin.png" class="figureright" >}}

*Sidebar: You may be noticing that this was a huge two years for both [Origin Systems](https://en.wikipedia.org/wiki/Origin_Systems) and [Sierra On-Line](https://en.wikipedia.org/wiki/Sierra_Entertainment) in particular -- and you'd be right! Both studios were known for their innovative game design, and 1988 to 1993 was really a renaissance for both of these companies. Sadly, by the mid-90s, both had been sold to larger companies (EA and CUC respectively) that would cannibalize and marginalize what made their games great and their products so loved.*

When Ultima 6 first hit the shelves, I was ignorant about the Ultima series, and games with this level of depth in general. For the first several years of my gaming life I was primarily focused on the Atari 2600 and then the NES; PC games were less of a key piece of my life. That meant that by and large the games I played were more arcade-like experiences. True, Zelda and Metroid were much more open-world adventures than anything before, but they are still far more action oriented than the thoughtful "keep a notebook handy" style of story telling of Ultima.

For many, however, Ultima was not a new thing: after all, this was the 6th iteration (or 7th if you count [Alkabeth](https://en.wikipedia.org/wiki/Akalabeth:_World_of_Doom)). I should also note that, for a lot of diehard fans, Ultima 6 was a huge and divisive departure from the games they were familiar. It was the first Ultima game to use VGA graphics, the first to try to integrate the mouse, and the first to have a consistent scale and isometric view, as opposed to having different scales for towns with more of an ascii-style overhead view. Admittedly, some of these gambles and innovations paid off, and some did not and needed another game version to get sorted (I'm looking at you U6 inventory management).

{{< figure src="/img/articles/u6/little-rj.png" class="figureleft" caption="Go Chiefs!" >}}

But none of that history mattered to me as a young pup. A friend of my father was a programmer by trade, and he actively played PC games. He was teaching me about programming, and he introduced me to Ultima 6 (and a variety of other games) right at this key time in my life: I was just now at an age to start asking deeper and more interesting questions of all of the content I consumed, and was fascinated by challenging reading and ideas.

I remember watching him play, and he explained the mythology of the world (he was one of those long-time Ultima gamers), and I immediately wanted to try for myself. I then distinctly remember taking the game home and just wandering around the game world for hours and hours, staying up incredibly late hours in the lazy summer. I honestly think it took me probably two years to ever go and actually try to beat the game. The first time I binged on Ultima 6, it was just six months of me wandering around the giant world and peeking under every rock, in every cave, and opening every abandoned house I came across. Stealing from houses, drinking in pubs, and doing mischevous things with the world. It was truly fascinating how many threads I could open and questions I could leave unanswered. As I did things in the world, people in the world reacted. Things changed, sometimes irrevocably. There were hundreds of people to talk to (256 people, specifically), and new creatures and secrets behind every corner. It was surprisingly immersive to me as a child.

This, of course, was at a time where not every secret and dimension of a game was known and documented exhaustively on the internet. So, while the game was large, my mind made it 10x bigger thinking of all of the things I had yet to discover.

{{< figure src="/img/articles/u6/vgau6.png" link="https://en.wikipedia.org/wiki/Ultima_VI:_The_False_Prophet" caption="Welcome to 1990!" >}}

How they managed to create that feeling of immersiveness and scale is a big part of what I want to explore with this series of articles.

# The Technology of the Time

To appreciate the accomplishments, we have to understand a bit about the technology of the time. Because, let's be honest, 1990 was a very different time for technology.

{{< figure src="/img/articles/u6/386.jpg" link="https://www.youtube.com/watch?v=qoN0HhDnRR8" caption="Photo Credit: Terry Stewart, Youtube" >}}

As mentioned above, Ultima 6 came out in the middle of 1990. At that time a **top of the line** PC had a 33mhz 386 CPU, VGA graphics, 4 MB of ram, and maybe a 200MB hard drive. Most of us plebians had a lot less than that. While I sadly don't have nor recall the specs of my first 386, it was likely a 25mhz 386, with 4 MB of ram, and 80 MB of hard-drive space. I do remember pushing RAM chips directly into the board to upgrade to 8 MB of ram before I finally gave up and got a Pentium.

It's worth noting that the target platform for Ultima 6 was not ever really a brand-new 386 from 1990. For IBM PC compatible machines, their target requirements were a 286 compatible PC running MS-DOS 3.3, with 640K of RAM. In other words, they were careful to ensure that the PC could run in a very tight amount of memory ([Real Mode](https://en.wikipedia.org/wiki/Real_mode)), and could support a machine that might have as little as 20MB of hard-drive.

Ultima 6 for the PC could ship on 3.5" floppies or on 5.25" floppies. In the case of the 5.25" floppy disks, it came on **7** disks. Despite that huge number of disks, the game itself was squeezed into 2.25 MB. It was split into multiple disks organized by theme as much as possible (intro disks, endgame disks, etc), and it could be run without fully installing it on your hard-drive, though it was always very, very preferable to run the game from a full hard-drive install if you could. Multiple disks were needed to manage the primary game data. 2.25 MB may seem trivial to install on a drive, but keep in mind that was likely up to 1/10th of your entire hard-drive. In comparison, that is like a 50-100GB game install in common terms for today.

Further, because the world was not all settled on running on IBM clones yet, if you wanted to maximize profits as a game developer, you had to target a lot more platforms. In the end, Origin ported Ultima 6 to a significant number of other game platforms, including:

* [The Amiga](https://en.wikipedia.org/wiki/Amiga) - (based on the PC codebase)
* [The Atari ST](https://en.wikipedia.org/wiki/Atari_ST) - (based on the PC codebase)
* [The FM Towns Gaming PC](https://en.wikipedia.org/wiki/FM_Towns) - This may be the only "superior" U6 port to the PC version; it actually had voice audio for NPCs!
* [The PC-98](https://en.wikipedia.org/wiki/PC-9800_series)
* [The X68000](https://en.wikipedia.org/wiki/X68000)
* [The C64](https://ultima.fandom.com/wiki/C64-Port_of_Ultima_VI) - This may be the most challenging, as the amount of crunching to work comfortably on a C64 is severe. The game had to effectively fit on 1MB of storage (three 5 1/4" **double-sided** floppies), and run with 160x200 resolution and 64KB of memory). It's a testament to their approach to building games that they could re-visit their graphics to actually achieve this port.
* [The Super Nintendo](https://en.wikipedia.org/wiki/Super_Nintendo_Entertainment_System) - By all measures, this port was going to always be a challenge. The game was not designed for a controller, it was not designed to meet Nintendo USA's expectations for content censorship, and it was not built to fit on a ROM cartridge. To make this work so many things were changed (like re-working the conversation system, removing the portraits, and removing a lot of text), that it is hardly worth visiting this as a counterpart of this game. It also was years late.

# What's Next

Over the next (I have no idea how many) articles I will start exploring how the game data and the game world was constructed with Ultima 6, and we can learn together how they managed to build an immersive and realistic fantasy game world in 1990.

To get there we're going to build out some real functional code! I'm a Java developer by historical trade. And, while I'm not uncomfortable with C and C++ at this point, the [Nuvie](http://nuvie.sourceforge.net) team has done that already and if you want to understand Ultima with that language, just visit their Github repo.

To me, there is something intriguing about the challenge of going to a higher level of abstraction, and building this with Java certainly meets that (perhaps questionable) expectation. In fact I plan to use [Kotlin](https://kotlinlang.org), rather than Java. But all the same: target the JVM.

If you're going to code a game on the JVM though, you still eventually will have to figure out how to push pixels to the screen and render graphics. So what is my plan for that? For that there is a suprisingly well-aligned sweet spot with **[LibGDX](https://libgdx.badlogicgames.com/)**. LibGDX is a multi-platform game runtime, much like [Godot](https://godotengine.org/) or maybe even [Unity](https://unity.com/), though far lower-level. LibGDX is well-suited to 2d-style games first and foremost (sprites and textures and such). This matches quite well with the design of Ultima 6. And, as luck would have it, LibGDX supports Kotlin as well!

Now, you may think that LibGDX is overkill for modeling a game from 1990. And while I might agree (and contemplated the idea of going raw JDK libraries), some of the primitives in LibGDX immediately solve a lot of problems that are not worth fighting for this learning experience. Camera transformations, fast texture blipping to GPU memory, clean music APIs, etc.

As a result I plan to provide a journey through the world of Ultima 6 using a variety of tools familiar to this blog, and some new interesting and fun stuff:

* [Kotlin](https://kotlinlang.org) as a language
* [Gradle](https://gradle.org) as a build system
* [LibGDX](https://libgdx.badlogicgames.com) as a game runtime
* Various open source libraries to help us along the way

# Getting the Ultima 6 Data Files

If you want to code along, you will need an original copy of Ultima 6. I highly recommend you buy [a modern copy of Ultima 6 from GoG.com](https://www.gog.com). Unfortunately, you will need a Windows machine to easily open the executable to get to the installation guts for the game, but, yes, I still believe it should be bought, and no I don't condone abandonware downloading of it -- if you choose to that's on you.

To get where we are going, all that is required is the contents of the install disks. You'll know you've succeeded in finding what we need for this tutorial if you have these files:

```
ANIMDATA      DUNGEON.M    INTRO.M       NEWMAGIC.BMP  U6CURS.CGA
ANIMMASK.VGA  EEGADRV.BIN  INTRO.PTR     OBJTILES.VGA  U6CURS.EGA
BASETILE      EGADRV.BIN   INTRO.SHP     PALETTES.INT  U6CURS.TGA
BLOCKS.SHP    END.M        LOOK.LZD      PAPER.BMP     U6MCGA.PTR
BOOK.DAT      ENDPAL.LBM   LZDNGBLK      PORTRAIT.A    U6PAL
BOOTUP.M      END.SHP      LZMAP         PORTRAIT.B    ULTIMA6.COM
BRIT.M        ENGAGE.M     LZOBJBLK      PORTRAIT.Z    ULTIMA.M
CGADRV.BIN    FOREST.M     MAINMENU.SHP  SCHEDULE      VELLUM1.SHP
CHUNKS        GARGOYLE.M   MAP           STARPOS.DAT   WOODS.SHP
CONVERSE.A    GYPSY.SHP    MAPTILES.VGA  STONES.M      WORLDMAP.BMP
CONVERSE.B    HORNPIPE.M   MASKTYPE.VGA  TGADRV.BIN
CONVERT.PAL   INTRO_1.SHP  MCGADRV.BIN   TILEFLAG
CREATE.M      INTRO_2.SHP  MIDI.DAT      TILEINDX.VGA
DITHER        INTRO_3.SHP  MONTAGE.SHP   TITLES.SHP
```

# Setting up LibGDX

If you want to follow along with code, you will need a new LibGDX project. [LibGDX has an easy setup app](https://libgdx.badlogicgames.com/download.html), which makes it very easy to generate a new scaffold project. The project generated will have a format like this:

```
root/
  core/ -- Main game logic
  desktop/ -- Desktop bootstrapper
```

For now this series will focus only on launching from the desktop. While iOS and Android are interesting targets for a real game, this is purely educational at this time, and so the desktop (right next to where we are coding) is the more interesting part.

# Playing Along

I am also keeping my most current progress on a **[public Github repo here: https://github.com/realjenius/redmoongate/](https://github.com/realjenius/redmoongate/)**

In my repo I have made some minor customizations to the various gradle files to correct and update the Kotlin version. Nothing significant, but just pinning versions and pulling in precisely what I needed, so be sure to check those out.

# Next: A Basic Set of Utilities

The next step will be to start crafting a basic set of utilities and helpful functions to make bridging the gap between 1990 and 2020 much easier. After that it's time to dig in to the actual game data.

I hope to see you in Part 2!
