---
title: 'Repairing a Very Corrupt Nix Store'
summary: 'Finding my way through a broken nix store'
tags: ["nixos"]
date: 2023-07-21
---

Recently I moved all of my personal setups to NixOS, from my Framework laptop, my gaming desktop, and my work MacOS setup. However, my laptop recently was no longer able to rebuild or re-apply, due to weird Nix errors.

<!--more-->

Overall, NixOS has been an excellent experience, and one of the most enjoyable Linux distros I've used in a long time. Further, it's made a lot more complete and less fiddly "dotfiles" experience with Home-Manager than I ever got with my own dotfiles Git repo with install scripts.

However, today I ran into a really ugly case that surprised me based on my initial perception of Nix as the "indestructible OS". I made some iterative changes to my `configuration.nix` based on various utilities I was missing, and when I ran `sudo nixos-rebuild switch`, I got a very strange error that aborted the derivation:

```
error: status failed '/nix/store/....' No such file or directory
```

From everything I had read, this was not really supposed to happen; wasn't the idea that NixOS was safe to try new things and undo? But of course, this did happen, and I had to figure out why. So I (correctly) assumed it probably was a lack of knowledge on my part, or being careless (also likely correct). Though, admittedly googling for hints proved a little difficult. To be fair, this did happen on my laptop, shortly after I spent some time trying to debug some lid-close + hibernate configuration, so it's possible I corrupted some ext4 storage with unplanned restarts due to the system being unresponsive.

Or... it is possible it had something to do with auto-optimizing the store, as I ran into a couple of interesting reports of there being assumptions in optimization:

```
# Are you dangerous?
nix.settings.auto-optimise-store = true;
```

Finally, I am on `nixos-unstable`, which is probably more prone to frequent changes.

Whatever the case may be, I had a broken store, but little idea of what to do about it. More unfortunately, no amount of rollback using something like `nixos-rebuild --rollback switch` or picking a different boot version made any difference - the system was fine and booted OK, but upgrading it or changing any of the configuration was thoroughly broken - meaning the store problem spanned revisions.

The first solution I found from searching was `nix-store --verify --check-contents --repair` which I was excited to try, but quickly got a variety of errors around missing symlinks with valid referrers, and then a frightening SQLite foreign constraint that caused the process to fail. This lead me to believe I was in huge unrepairable trouble. I was about to dig back up my USB stick and reinstall NixOS.

And, as a small aside, this is the big benefit of Nix. Even if you completely manage to destroy an installation, you should always be able to get back to it.

Nevertheless, after a good bit more googling, I found [this post](https://discourse.nixos.org/t/cant-rebuild-system-after-deleting-a-package-from-the-store/15063/8) which mentioned a garbage collection command to run:

```
nix-collect-garbage -d
```

This was new, and looked promising. Up to this point I had been relying on some "automatic" settings for GC:

```
  nix.gc = {
    automatic = true;
    dates = "weekly";
    options = "--delete-older-than 14d";
  };
```

However, because it's early days, I've been making a LOT of revisions - in fact, I doubt that automatic configuration has done much of anything yet.

So, after running `nix-collect-garbage -d` (which took quite a long time), 24 *Gigabytes* of data was cleared up, and then I was able to successfully run `sudo nix-store --verify --check-contents --repair` (which also took much longer and seemed far more thorough).

After *that* was done, I was able to upgrade things again.

**Moral of the Story**

So, what's the moral of the story? I'd say it's this: when you fundamentally switch your technology stack, even if it works incredibly well at first, maybe learn a bit more deeply about it to ensure you aren't neglecting any important steps. For me that means better understanding the lifecycle of old revisions in the store, and what it means to take good care of my new "indestructible OS" so it stays that way.
