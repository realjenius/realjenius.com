---
title: A Good Week for Linux, Java, and Framework Laptops
date: 2024-05-31
tags: ["linux", "java", "framework"]
---

I've been a Java developer for 25 years, a Framework laptop user for the last 2.5 years, and I've been using Linux as my primary operating system for over 10 years. This has been a very encouraging week for people in a similar space as myself.

<!--more-->

## Wakefield Hits EAP with Jetbrains

Recently [I wrote about the challenges for Java applications on Linux as a result of the migration to Wayland]({{< relref "../java/why-wakefield-matters.md" >}}). Java has historically only supported the X server protocol, and even support of XWayland was not always great due to quirks and limitations in that bridge. 

As of this week, [Intellij 2024.2 EAP 2 has been released](https://blog.jetbrains.com/idea/2024/05/intellij-idea-2024-2-eap-2/). This is an early adopter build of the next major release of Intellij, and it includes the usual variety of nice new features and upgrades. However, one notable addition is the move to the Jetbrains Runtime 21 (JBR21). As I mentioned previously, Jetbrains ships their own custom fork of OpenJDK with their IDEs to provide the best experience possible running on any environment. The advantage of this is that they can try out new features and tweaks on their fork before it lands officially in the mainline OpenJDK.

JBR21, for the first time, includes initial Wakefield support and as such will start to support Wayland natively:

> With IntelliJ IDEA 2024.2 EAP 2, we’re transitioning from JetBrains Runtime 17 (JBR17) to JetBrains Runtime 21 (JBR21). Starting with this build, all IntelliJ IDEA 2024.2 updates will come with JBR21, offering enhanced security and performance, as well as Wayland rendering support for Linux.

## Framework Laptops on Linux

Something else I hinted at in the Wakefield article (but have not really written about in depth) is my challenges as a Framework laptop user on Linux.

I bought my 11th generation [Framework laptop](https://frame.work/) in December 2021. The entire premise of a thin ultrabook style DIY laptop with freely upgradable parts and a company built around those fundamental principals was ideal for me. In general, despite all the early adopter and growing pains quirks, I have absolutely loved my Framework. There are two things, however, that have been a constant struggle:

1. The display resolution
2. The firmware situation

Let me summarize these problems, and how they have suddenly been fixed in two major announcements this week.

## Framework Laptop Displays and Scaling

The display on the Framework is `2256×1504` - which is a `3:2` aspect ratio. The quality of the screen is actually quite good, however the dimensions put it in a place where 1x scaling feels uncomfortably small, and 2x scaling feels uncomfortably large. Anyone that has used Linux for any decent amount of time with a high resolution screen probably knows that fractional scaling support is shaky, at best. Different desktop environments have different levels of support for configuring fractional scaling, and that has only gotten worse with the gradual transition to Wayland over time, as many app runtimes simply didn't natively support it. As mentioned in my previous post, this could result in mis-sized or blurry applications. Over the years, notables examples have included: Firefox, anything built with Electron (e.g. VS Code, Slack, Signal, etc.), and of course anything built with Java.

This meant that the last 3 years have been a regular struggle to find the right configuration and environment; and has often meant that trying other environments was off the table just due to scaling bugs around the edges. Inevitably, the only platform that worked for me mostly consistently was KDE Plasma, thanks to hitting the 80% rule as much as possible with their configuration options. Gnome and GTK derivatives were almost always unpleasantly broken.

As an example, here is Pop OS showing a Java-based (Compose Multiplatform) application with the completely wrong scaling:

{{<figure src="/img/articles/wakefield/jetbrains-toolbox.png" caption="Tiny Little Toolbox">}}

Thankfully, things have started to get better:
* Wayland has official protocol support for fractional scaling
* Native Wayland support has been added to Electron, to Firefox, and to others
* Fedora setting a target date for their dismissal of X has, to some extent, also forced the hand of multiple teams

Unfortunately, that doesn't suddenly magically fix fractional scaling support across the Linux ecosystem - there are still problems littered throughout.

The Framework team has heard this criticism, and this week announced a new [higher resolution 2.8k screen](https://frame.work/blog/introducing-the-new-framework-laptop-13-with-intel-core-ultra-series-1-processors) which, in addition to looking even more sharp and dynamic, will also look properly sized to most eyes on a non-fractional 2x scaling on Linux:

> The new 2.8k display option with 2880x1920 resolution at 256 PPI makes pixels invisible from a normal viewing distance, resulting in incredibly sharp text and graphics.  Even better, that resolution allows for a streamlined experience in Linux through 2:1 display scaling. The 120 Hz refresh rate with variable refresh rate support, 500 nit brightness, 1500:1 contrast, and anti-glare matte surface make this an all-around excellent panel across a range of use cases.
> [...]
> The 2.8k display is a configuration option on DIY Edition, and you can also pick it up in the Marketplace to upgrade any existing Framework Laptop 13.

With this $260 purchase and a little delicate work with a Torx T5, all Framework 13 users can fix their Linux display woes.

## Framework Firmware Finally Fresh

The other major thing that has been a struggle for Framework users on Linux has been Firmware/BIOS updates. There have been a variety of vulnerabilities that have only been truly fixable with a firmware update, and more recently the new higher capacity battery requires a firmware update to take advantage of.

Linux users have been left in the cold with the Firmware update process. It's been slow, inconsistent, and the general response was "sadly, it's not ready, use a Windows boot USB stick to do the firmware update from Windows for now" - which is frankly a terrible workaround.

[Last month Framework finally acknowledged their problems](https://frame.work/blog/enabling-software-longevity), and as of this week they have released the latest firmware, as well as their new commitment to communication and regular updates. This new release unlocks the new high capacity battery for users like myself, as well as fixing a variety of vulnerabilities and quirks.