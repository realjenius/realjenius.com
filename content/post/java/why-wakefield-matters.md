---
title: Why Project Wakefield Matters
summary: 'Project Wakefield and what it means for Java'
tags: ["java", "wayland", "linux"]
date: 2023-12-15
---

[Project Wakefield](https://wiki.openjdk.org/display/wakefield) is the effort to build support for the [Wayland](https://wayland.freedesktop.org/) display server in the JDK, allowing Java-based desktop apps to be native Wayland clients. This project has moved from an interesting effort with some momentum, to a far more important project than ever before.

<!--more-->

## What is Wayland?

Wayland has been an effort to build an alternative display server for Linux since early 2012. The goal has always been to build a modern replacement for the [X11 Window System](https://en.wikipedia.org/wiki/X_Window_System) that considers the needs of modern applications on modern hardware, while at the same time eliminating a lot of the architectural details that come from a 1980s Unix legacy, and no longer make sense. Notably, Wayland eliminates the client-server model for the window manager that is inherent in the X design; clients now draw directly to the Wayland compositor.

However, along with such a fundamental change, much of the nature of application interaction with the window manager is bound to change in general. There are a huge number of large changes, such as how window placement works (or doesn't...), but also small changes like how long-keypresses are represented and how apps should interpret them. Additionally, there are some things that are just not truly settled, with solutions unique to individual distributions and desktop environments that are not yet stable and available across the board (screenshots and clipping the screen, for example).

It has taken a very long time for Wayland to become a new defacto standard. Over the years the ecosystem around Wayland has grown, as competitive efforts ([such as Mir](https://en.wikipedia.org/wiki/Mir_(software))) have slowly diminished and the list of unsolved problems with Wayland as a usable platform grew shorter. At some point around late-2018/early-2019 Wayland hit its first major adoption milestone as core distros started to package it as a default. Since then, Debian, Ubuntu, Fedora and others have either shipped Wayland as an option or have made it the default out of the box.

To make this possible, [XWayland](https://wayland.freedesktop.org/xserver.html) was created. XWayland is a very clever solution that uses the X server protocol as a shim to the Wayland compositor. This allows for X-based applications (like most Java apps) to render and work mostly correctly on Wayland desktop environments. This problem is not unique to Java; most non-native application stacks have run into something similar, including [Electron](https://www.electronjs.org/apps)/[Chromium or Aura](https://www.chromium.org/developers/design-documents/aura/aura-overview/) apps (e.g. Visual Studio Code and Chrome), [XUL](https://wiki.mozilla.org/XUL:Home_Page) apps (e.g. Firefox and Thunderbird), and even [Wine and Proton](https://github.com/ValveSoftware/Proton/issues/4638) - that's right, most games running through Steam will be going through XWayland, eating up those lovely FPS.

If you run Linux on the desktop, there is a good chance you are regularly working with XWayland and you may not even know it. So, if that is the case, why is it so important for Java to natively support Wayland?

## Problem 1: The Limitations of XWayland

XWayland is clever, but it's no panacea. First, it must be mentioned that XWayland is far from "efficient". The entire nature of Wayland is to avoid the client-server model, but with XWayland it's effectively A `client -> server -> server` workflow; now both X and Wayland are participating in coordination of windowing; it is "compatible", but it's not going to result in the most snappy experience.

But in addition to waste/bloat, there are a ton of things that either X, Wayland, or the combination of the two together simply don't support. This can result in weird, subtle bugs and edge cases in apps that worked fine under X, but no longer do.

One challenge in particular is fractional scaling, which is the feature of scaling an app by a non-integer DPI. With the increase in HiDPI monitors, this has become increasingly important. I would challenge most people to try and be comfortable with a [13" Framework Laptop](https://frame.work/) scaled at either 100% or 200%. 100% scaling looks tiny, and 200% scaling looks like a fisher price toy. Here are two screenshots that show the display settings and how much of the overall 13" screen they take up.

{{<figure src="/img/articles/wakefield/100-percent-dpi.png" caption="100% Scaling - lots of real-estate...">}}

{{<figure src="/img/articles/wakefield/200-percent-dpi.png" caption="200% Scaling - giant everything">}}

In this case, 150% is the "goldilocks" DPI for most users:

{{<figure src="/img/articles/wakefield/150-percent-dpi.png" caption="150% Scaling">}}

Wayland now supports high-quality fractional scaling all the way to the protocol, which is an excellent step and provides a great experience for native Wayland apps. But what happens with XWayland? Unfortunately, things get lost in translation. Work has been done to investigate how to support full fractional scaling through X and through Wayland XWayland, [but that work is fraught with challenges and is still open](https://gitlab.freedesktop.org/xorg/xserver/-/issues/1318). So, what happens for now? By default, what users experience right now is going to be one of two behaviors on most distributions and desktop environments:

1. The finished "post-render" output of the app is rescaled by the Wayland compositor. This results in blurry, low quality rendering output as it was rendered at a lower resolution
2. The app is not scaled at all (or worse, is scaled incorrectly), resulting in the UI being scaled differently (perhaps uncomfortably so) than all of the native applications

There is no "one answer" for what behavior will occur, unfortunately, as this is a weird transitional time for the Linux desktop (though, when is it not, I suppose). In short, it currently depends on the desktop environment and the options it provides for scaling non-native apps. Notably:

* [KDE Plasma 5.26](https://www.phoronix.com/news/Plasma-5.26-Crisper-XWayland) provides an option to allow legacy applications to scale themselves. This is a combination of (a) not scaling the output from XWayland apps and (b) enforcing a variety of environment variable hints that allow those X apps to try and scale themselves
* [Gnome has taken a stance against providing this same behavior](https://discourse.gnome.org/t/xwayland-fractional-scaling-like-in-kde/17617), because leaving scaling to the legacy application could result in even worse experiences, and having a toggle that "might produce bugs" is kind of against the idealist Gnome way of doing things. So instead, apps on Gnome are always scaled by the compositor. And, while blurry, those apps should generally work as intended.
* Cosmic in [Pop OS! 22.04](https://pop.system76.com/) adds some additional HiDPI configurability that is not available in Gnome itself, but still is limited, and generally will result in either blurry apps or unscaled apps depending on your configuration.
* Other desktop environments, such as [Cinnamon](https://en.wikipedia.org/wiki/Cinnamon_(desktop_environment)) from Linux Mint, still use X by default, so this isn't going to be a concern yet (though you may have limited DPI options in general).
* For more esoteric platforms such as [Hyprland](https://wiki.hyprland.org/Configuring/XWayland/) and [Sway](https://wiki.archlinux.org/title/Sway), users have configurable options but have to handle it all themselves. In most cases it's going to be challenging (or impossible) to get exactly what you want, just like everywhere else.

For a concrete example, here is what Jetbrains Toolbox looks like currently on Pop OS 22.04:

{{<figure src="/img/articles/wakefield/jetbrains-toolbox.png" caption="Tiny Little Toolbox">}}

As you can see, the app is simply unscaled. Pop-OS in this configuration does not scale the output of the X-based [Skiko-rendered](https://github.com/JetBrains/skiko) app, and the app is not being given the hints it needs to scale itself, so it is tiny compared to everything else.

*(Side Note: As a consumer/tester of several of these platforms over the past several months, in my opinion, the best experience for a Java developer in late 2023, especially using Jetbrain products, is almost certainly KDE. While the flag in KDE could theoretically break some non-native applications, it works perfectly with Intellij)*

## Problem 2: The Pace of Adoption

The other major problem is that this year has seen Wayland adoption taking off at a rapid pace.

* [RedHat is dropping X](https://www.msn.com/en-us/news/technology/wayland-takes-the-wheel-as-red-hat-bids-farewell-to-x-org/ar-AA1kJuaf)
* [Cinammon has added Wayland support](https://9to5linux.com/cinnamon-6-0-desktop-environment-arrives-with-initial-wayland-support) and will be switching to Wayland by default soon enough
* [System 76's new Cosmic Desktop for PopOS is native Wayland](https://9to5linux.com/system76-shares-more-details-on-its-rust-based-cosmic-desktop
)
* Most other Debian/Ubuntu distros are Wayland by default and are eager to drop their X lineage for good
* Wine and Proton now have experimental native Wayland support

Overall, it have become clear that, all pros and cons aside, Wayland is going to be the future of most Linux Desktop environments. As a result, it has become increasingly important that Java apps can function to their best with Wayland, and aren't encumbered with a tech stack that is becoming increasingly less and less supported.

## The Path for Java: Wakefield

As it turns out, many of the XWayland-encumbered platforms I mentioned previously have started to support Wayland natively. Notably:

1. Aura-based apps now have the [Ozone Platform](https://chromium.googlesource.com/chromium/src/+/refs/heads/main/docs/ozone_overview.md) for experimental Wayland rendering (I'm writing this article through Ozone-enabled VS-Code).
2. Mozilla apps have supported a wayland rendering backend for a while via env-vars, [and are now shipping it on by default](https://9to5linux.com/mozilla-firefox-121-to-enable-wayland-support-by-default-on-linux) (I'm previewing this article through Firefox-on-Wayland).

That means that for these platforms, it's only a matter of ensuring that Wayland runs smoothly and they can start to reduce their focus on X over time. That path does not (yet) exist as an option for Java; the JDK supports X, and that's it. That path forward *is* Project Wakefield.

The Jetbrains developers have been well-aware of these problems for a while. They have [had instructions on how to workaround the issues as much as possible for multiple years now](https://intellij-support.jetbrains.com/hc/en-us/articles/360007994999-HiDPI-configuration) since Wayland became a going concern. Further, one of their most active tracked issues [has been specifically about the quality of scaling support in Jetbrains projects](https://youtrack.jetbrains.com/issue/JBR-3206).

Since August, Jetbrains have been promoting the "proof-of-life" of native Wayland support in Java using Wakefield: [Wayland Support for IntelliJ-based IDEs](https://blog.jetbrains.com/platform/2023/08/wayland-support/). The Jetbrains team being involved is a big boost for this effort, as they have one of the most heavily used and sophisticated Java UIs today (along with, I suppose, other Java IDEs). Between the Swing-based app set (Intellij Idea, Rider, Webstorm, etc), as well as their newer application UIs (Compose, Fleet IDE), Jetbrains have a lot of different perspectives on what works and what doesn't, and a lot of skin in the game.

The good news is that as of August, Intellij can start and "mostly works" on Wakefield. However, as illustrated by the blog-post, there is still a lot to do, and some of it has real unsolved challenges, given the current state of Wayland:

> * Vulkan-based accelerated rendering.
> * Input methods.
> * Clipboard and drag and drop support.
> * Splash screen.
> * Switching between windows with a keyboard shortcut (which, given the Wayland’s tough security model, is a complicated endeavor).

Nevertheless, progress is still being made, and as things are solved, it will get closer to becoming an experimental option available for teams. Additionally, Jetbrains ships their own Java runtime ([called the JBR](https://github.com/JetBrains/JetBrainsRuntime)) within their products, and they have the ability to patch Wakefield support into that along with other customizations they have made along the way to better support their IDEs.

This work will be fundamental for any apps that use Java on the desktop, but will also help ensure that next-generation tech stacks like [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) can continue their path to being viable solutions for building applications once for desktop, mobile, and the web. Thankfully, the much faster Java release train will also make it much easier to start getting Wakefield into Java for everyone, possibly as preliminary and experimental releases, once Wakefield reaches an initial feature-complete state.