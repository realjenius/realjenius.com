---
title: 'Do not Upgrade Your (ve) to Ubuntu 12.04!'
summary: 'I''ve recently been battling an issue with my Media Temple (ve), and a rather archaic kernel. I share it here in the hopes you won''t get bitten too.'
tags:
- hosting
- media temple
- LTS
- EOL
category: journal
layout: post
---

A quick scan of my site shows that I'm typically one of [Media Temple's](http://www.mediatemple.net) [biggest supporters](/tag/media%20temple/). Their customer service is prompt, friendly, and their technological offering is always very stout for the price.

However, for the past little bit, I have been battling a rather unfortunate situation (which admittedly is not all their fault) regarding Ubuntu upgrades and their (ve) servers. My site currently runs on a [Media Temple (ve)](http://mediatemple.net/webhosting/ve/) hosting virtualized Ubuntu 10.04.4 LTS. I had been doing regular upgrades and dist-upgrades on this environment without fail, but I hit an issue where some of the packages in the Lucid Apt repos were just getting a little long in the tooth. I generally use Ruby, Nginx, and a few other things out there that have a tendency to move pretty quickly on their upgrades, and individual releases generally don't introduce the major/minor package version upgrades of the sort that might happen with some of these components. Two and a half years can be a long time when it's a server for a hobbyist exploring a lot of new tech.

Now, I do know that with Ubuntu's community, you can inevitably dig up somebody who has a PPA that can be tied to your respective LTS install to individually bring in packages up to a newer version than that particular major release ships, but inevitably those are supported less effectively than the mainline distro packages, and there's also a spidering of trust it results in, where you being relying on more than just "official" community package maintainers. This is, in fact, what I've had to do, however.

Choosing to leave the LTS wasn't a huge deal for me anyway, as this is my personal server; not some commercially-hosted product. So, I decided to do the magical `do-release-upgrade` to level-up. Whoops.

Earlier last year if you decided to do a release-upgrade on Media Temple (ve)s with "normal" as your release profile (as specified in the `/etc/update-manager/release-upgrades` file), it would upgrade you to 10.10, and everything was shiny. You could then upgrade to 11.04, and even to 11.10. No problems.

However, if you tried to go to 12.04, the upgrade bombed very, very badly. In short, [this is the error I received](http://askubuntu.com/questions/146610/why-does-upgrading-to-12-04-on-an-openvz-vps-warn-that-the-kernel-size-is-0).

> Please check your current kernel version with uname -r. If it is less than 2.6.24, the upgrade will fail half-way with a glibc error. That happens because the glibc included by default with 12.04 requires a minimum 2.6.24 kernel -- glibc are the critical C libraries used by every application.

Yup - sure enough, just as this link indicates, MediaTemple runs an [OpenVZ](http://openvz.org/Main_Page)-based virtualization by [Parallels](http://www.parallels.com/). Here's my `uname -r`:

	2.6.18-028stab101.1

Uh-oh. 2.6.18 is definitely not 2.6.24. The forum Q&A above suggests a rather ominous pinned dependency for `glibc`. I tried this temporarily, but never got it to work, and decided that it was not something I wanted to be shimmed on my server long-term anyway, so I went back to 10.04.

*(Incidentally, this isn't discussed much, but if an Ubuntu upgrade fails, you better have a spare boat handy, as this one is going to sink, and no bailing of water will help. It's very difficult to do anything resembling a rollback without just restoring a backup. So be prepared!)*

Now, something else happened shortly after 12.10 Ubuntu came out last year that made all of this muddier for Media Temple Ubuntinians. While 10.10 has been EOL for a while, it hasn't actually been moved off of the main APT sources, so doing a "normal" upgrade would work as you'd expect. Through some administrative cleanup (I presume), it's now properly under "old-releases" on the Ubuntu site. So if you try to upgrade from 10.04 LTS to 10.10 with `do-release-upgrade` and a normal upgrade setting, you will get a rather obscure 404 error. Worse, because Lucid isn't actually in the EOL package tree for standard builds (because it's not EOL), the general recommendation for upgrades fails ([see here](https://help.ubuntu.com/community/EOLUpgrades)). In short, they recommend you to:

1. Move your current release sources to old-releases
1. Do a full "update+upgrade" to get caught up
1. Perform the do-upgrade-release to the next version, which may be EOL.

This doesn't work, as step-2 throws 404 errors. Now, I suspect so long as you did a full update before moving this over (flip steps one and two) it probably would work. But it feels rather unpleasant, and inevitably you'll still hit 11.10 and have to stop. So now you've upgraded to a more recent build, but you're no longer on an LTS release, and instead are on a release that will likely sunset in April, 2013, with nowhere to go.

**Even worse** for most (ve) folks is the fact that the default upgade process is not to do a "normal" release, but to do an LTS upgrade on 10.04 now that 12.04 is out. So if you login to your Media Temple instance, it will suggest you upgrade to Precise, and if you do -**BOOM**- trashed install. I made that mistake as well during this process.

So in short, 10.04 LTS is probably the best you can do on a (ve) without doing some less-than-ideal workarounds, and the kernel is going to be 2.6.18 until Media Temple straightens it out.

To describe how old 2.6.18 actually is, 2.6.16 (just 2 patches prior) came out in *March of 2006*. 2.6.27, which came out over a year after was, at release, a long-term-support kernel for Linux - and even that was EOL'ed back in early 2012. 

Now, to defend Media Temple a little bit, Ubuntu 10.04 LTS is still in active support, and will be for some time. So my distro is totally acceptable, even if the (admittedly, heavily patched and customized) kernel their virtualization uses is EOL. Also, from what I've been able to determine, this was originally a problem with Parallels, and not Media Temple. Unfortunately, it's been something of a lacking priority for them, or at least not very transparent that it was a priority. Here is a forum thread that's been going on since shortly after 12.04 went live: [Ubuntu 12.04 LTS on Media Temple (ve)](https://forum.mediatemple.net/topic/6345-ve-ubuntu-1204-lts/).

Here are the most recent updates from Media Temple - November 6th of last year:

> We still don't have a time frame as to when Ubuntu 12.04 LTS will be available for the (ve) Servers.  The 'technical obstacle' is we are bringing in a whole new infrastructure for our (ve) Servers.  I apologize for the inconvenience but this is all the information I have for you at the moment. 

... and on January 11th this year:

> Hey there! Apologize for the inconvenience but we currently don't have any update on when Ubuntu 12.04 LTS will be available. 

I even decided to bring this up with the Media Temple twitter team, and got a similar non-commital response unfortunately: [https://twitter.com/realjenius/status/298973645991182337](https://twitter.com/realjenius/status/298973645991182337).

But, even with bad news comes good. The main reason for the delay seems to be that they are working on some rather large (and non-descript) infrastructural change for the (ve) servers. The one huge benefit so far is that they doubled their RAM offering for all (ve) tiers, and existing customers were allowed to upgrade with a simple push of a button. So I guess I shouldn't complain too much!
