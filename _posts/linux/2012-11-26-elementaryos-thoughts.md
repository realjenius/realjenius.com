---
title: ElementaryOS Luna Beta1 Thoughts
summary: 'After spending a little over a week with ElementaryOS Luna Beta 1, I share my thoughts'
category: journal
tags: elementaryos linux luna beta
layout: post
published: false
---

Earlier this month, the folks over at [ElementaryOS.org](http://elementaryos.org) hit a milestone by releasing the first BETA of their much awaited Luna version.

{% assign src='linux/elementary-luna.png' %}
{% include image.html %}

Betas being what they are, it's not uncommon to find all kinds of bugs and problems. Linux is my daily development environment, so stability is important, but I also have a very stringent back-up scheme and a separately partitioned home folder so that I run with a pretty low risk of data loss and long recovery times. I can usually re-install my OS in just a few minutes.

As such, I chose to give this OS a try, after having been on Ubuntu 12.04 and 12.10 there-after for multiple months (and yes, I was using stock Unity as my desktop environment).

### First Impressions

My first impressions were phenomenally good. Compared to Unity, the Gala desktop environment is fast, buttery smooth, and to top it off, quite pretty. Additionally, they have gone to great pains to make sure every app (no matter what toolkit version or source) will look comfortable in the environment. Everything I opened looked just right: GTK2, GTK3, QT, and even Java-Swing apps. It's nothing that can't be done in Ubuntu itself, but it's certainly a nice indicator to the type of polish they've put into their desktop environment.

### Default Apps

I found some of the default app choices interesting (mostly because I had scarcely heard of them), and also very pretty. Here are some thoughts:

* **Geary** - This is the mail client of choice, and overall, I was pretty impressed. It's early development still, but it handles a full and busy GMail inbox admirably. Multi-inbox support is a big lacking feature, however.
* **Noise** - I was quite surprised they chose to go a completely different direction with the Music app. Noise looks quite good, and has good controls. Unfortunately, every time I tried to import my NFS-mounted music library it would crash spectacularly.
* **Maya** - Maya is the default calendar application, and again, it's quite pretty. Unfortunately it's not particularly functional for me as, so far as I can tell, it doesn't support syncing with Google Calendar. I'm sure this is something they'll rectify in future builds, but for now, it's off the docket for me.

So in short, my experience with the default bundled apps was brief. Thankfully two of the three above I'm already using web clients for, and the third, I'm comfortable with installing Rhythmbox or Banshee - which both seem to work fine and also look quite good.

### Configuration

Out of the box I was impressed with some of the things that just worked that some
