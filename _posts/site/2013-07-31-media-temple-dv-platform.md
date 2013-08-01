---
title: 'Media Temple''s New DV Platform'
summary: 'Earlier this year I was vocal about the geriatric underpinnings of Media Temple''s (ve) platform. Thankfully, things have changed.'
tags:
- hosting
- media temple
- LTS
- EOL
category: journal
layout: post
---

Earlier this year, I [lamented about the aging virtualization]({% post_url 2013-02-06-media-temple-ve-and-ubuntu-1204 %}) underpinning the (ve) platform at [Media Temple](http://mediatemple.net). While their hardware is high quality, and their support is always great, the kernel was ancient, and it allowed me to trap myself into upgrade hell. The long and short of it was that Ubuntu 12.04 was very unadvisable as an OS choice on the (ve), and that included upgrading directly through the OS. This was largely due to a very old virtualization kernel that prevented some of the core libraries in 12.04 from being accessible due to needing too new of a kernel.

Because of timing of some major platform changes, Media Temple was not offering a quick solution:

> We still don’t have a time frame as to when Ubuntu 12.04 LTS will be available for the (ve) Servers. The ‘technical obstacle’ is we are bringing in a whole new infrastructure for our (ve) Servers. I apologize for the inconvenience but this is all the information I have for you at the moment.

I'm happy to say that as of last month, they now have completely refreshed their virtualized platform offerings. Previously, the two platforms that MT offered were (DV) and (ve), with the former being a Plesk-based managed VPS that received controlled upgrades and improvements, and the latter being a bare-bones virtualized Linux box, much like you'd receive from an Amazon EC2 box.

They still offer these two concepts, but have moved them onto the new hosted platform (which is a newer platform from Parallels), and have renamed the products to reflect their similarities:

> We’ve consolidated all of our VPS platforms into one single, simple platform named the “DV”. We’ve simplified the VPS product line by consolidating and eliminating some things. We’ve removed the parens () ornamentation and we are no longer using version numbers. We’ve upgraded and changed the products formerly known as (dv) and (ve),  to [DV Managed](http://mediatemple.net/webhosting/vps/managed/) and [DV Developer](http://mediatemple.net/webhosting/vps/developer/).


I admittedly have not pulled the trigger on the upgrade yet. Unfortunately, it requires a full re-image of your instance, which will require me re-configuring a variety of sites and services. Because of that, I can't speak to details like the kernel version at this point in time, but they are calling it 2013 tech, which suggests quite recent.

I'll check back in with more details once I find time to schedule the upgrade!