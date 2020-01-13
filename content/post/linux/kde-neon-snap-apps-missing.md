---
title: Snap Applications Missing in Linux with ZSH
date: 2020-01-12
tags: ["linux", "kde", "neon", "zsh"]
---

I'm a regular [ZSH user]({{../workflow.prezto.md}}), and overall it works just fine as a replacement of BASH. However, recently I kept having trouble with my personal workstation losing track of my SNAP-installeed applications (in particular the [Atom Editor](https://atom.io/)) while running KDE Neon. Thankfully, I'm not the only one that had this problem, and there is a straightforward (and in hindsight obvious) fix.

<!--more-->

Periodically after I would update KDE Neon via the Software Center I would lose links to some (though not all) of my applications. Eventually I was able to discover it was applications I had installed through Snap in particular, such as the [Atom Editor](https://atom.io/)).

It turns out this disappearance is a common problem. The issue, in particular, is related to a variety of installation platforms assuming that `/etc/profile` will be used as a baseline for including things into the user's path and various environment variables. I discovered this from [this Reddit post](https://www.reddit.com/r/kde/comments/9pjos2/snaps_in_application_launcher/).

Having reviewed what was in my `/etc/profile` and `/etc/profile.d` configuration, it was clear that both Snap and Flatpak add things here assuming users are including BASH primitives in their shell startup.

Notably for this post:

* `/etc/profile` - This had some random default shell configuration in it (YMMV), but also notably had the loop logic for `/etc/profile.d`, as you'd expect
* `/etc/profile.d/apps-bin-path.sh` - This adds the Snap bin folder to the primary path, and also adds XDG data directories for Snap so that X11 and Wayland can find the Snap application launchers
* `/etc/profile.d/flatpak.sh` - This does thee same fiddly steps for flatpack

The Reddit post added a rather novel suggestion, which was to add this to the `/etc/zsh/zprofile` file (the ZSH profile variant):

```bash
# in /etc/zsh/zprofile

emulate sh -c 'source /etc/profile'
```

The `emulate` command, as the name implies, attempts to map some other shell into ZSH through an emulation layer. In effect this is a "safer" way of sourcing the /etc/profile file.

Adding this command naively fixes all of my issues, but it's important to know what this does -- there may be things in the `/etc/profile` structure you do not wish to load. You can always target files individually as well (for example invoking individual files in the `profile.d` directory).

An example might be:

```bash
# in /etc/zsh/zprofile

emulate sh -c 'source /etc/profile.d/apps-bin-path.sh'
```

Note, however, it is possible the files in the directory may change as Snap and Flatpak change their configuration over time, so buyer beware!
