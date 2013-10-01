---
title: 'DV Developer Acquired'
summary: 'I recently upgraded to the Media Temple DV Developer. This is a quick summary of my experiences.'
tags: personal realjenius.com mediatemple
category: journal
layout: post
---

As I discussed earlier this year, [Media Temple has refreshed their VPS offerings]({% post_url 2013-07-31-media-temple-dv-platform %}), improving their underlying virtualization software, getting a more recent kernel, and otherwise polishing up the various bits. After slogging through the last couple months of my second child being born, I finally got my head above water and pulled the trigger on migrating to a new installation. I was on an old (ve), so I chose to go to a [DV Developer](http://mediatemple.net/webhosting/vps/developer/), Level 2.

Since both the (ve) and the DV Developer are a raw, bare-bones Linux installation, there isn't a ton to report in terms of pretty oohs and ahhs, and the provisioning and migration occurred entirely without incident (as it should be), so I can't prattle on about that arduous process, since it was pretty much a single button press followed by "ssh #######" 5 minutes later.

Nevertheless, I can show a few interesting tidbits for those that are interested.

## As Advertised Boxi-ness

The Level 2 is "2GB Memory, 40GB Storage, and 650GB Bandwidth". One thing I've always appreciated about Media Temple is the number of cores they allow you to run on for your slice of the computing pie:

{% assign src='/misc/dv_stats.png' %}
{% include image.html %}

Other VPS offerings in the world seem to generally offer more like 2 or 4 cores, as opposed to the 16 made available on Media Temple. Even if the aggregate compute power available through MT is no better than other hosts, I prefer it being split across cores like this when hosting concurrent web apps.

You can also see that I do in fact have 2GB of memory at my fingertips; no trickery there. I should also note that the disks are SAS, and the ethernet is gigabit - so you have very few bottlenecks baked in (as you might with something like EC2).

## Newer Kernel; Newer Virtualization Platform

I lamented rather furiously about the state of the kernel on my (ve) - it was a rather ancient `2.6.18` build for OpenVZ from Parallels. Their new platform is a more recent, but not what I'd consider super modern `2.6.32`. It's certainly new-enough for most needs. The new virtualization platform is also supposed to be more readily upgradable on their part, so hopefully it won't fall behind quite so badly.

Concretely speaking, I'm currently able to run Ubuntu Server 13.04 without issue, which is a significant step forward from the 10.04 dead-end I previously encountered.

Media Temple also promises significant performance increases for the same resource utilization:

> DV Developer, formerly known as (ve), streamlines the DV platform for speed, performance and efficiency. Packed with the same upgrades as DV Managed – 25% increase in LAMP performance, 10X increases in backups and maintenance, and industry leading software packages including Ubuntu, CentOS, Fedora, and Debian – DV Developer also provides rebootless updates - upgrade kernel and reboot without downtime, improved stats using vSwap, and unlimited scalability by removing the previous 20 service limit per account. DV Developer customers will also benefit from the new (mt) Media Temple 20/20 Uptime Guarantee. Pricing for DV Developer, including all upgrades, remains the same starting at $30 per month with no commitments required.

## Consoles, Admins, APIs, Etc.

None of this is particularly new (in fact, it's effectively identical to the (ve)), but there are a number of good administrative features available:

### Admin and Stats Panel

MediaTemple has an a bevy of administration GUIs, and also has a stats panel that shows sliding history of box performance:

{% assign src='/misc/dv_admin_gui.png' %}
{% include image.html %}

### Power Panel

You also get a Plesk Parallels Power Panel (Pay Phat Pive Pimes Past), which allows you to perform a number of tasks from a GUI console instead of SSH-ing in:

{% assign src='/misc/dv_power_panel.png' %}
{% include image.html %}

### APIs

If you have need to automate your backends, you can with the [Media Temple ProDev API](http://mediatemple.net/api/). This allows you to do many of the things you'd expect:

* Add services (instances)
* Get current service information
* Reboot services
* Get service statistics, warnings, thresholds

## Summary

Overall I'm quite happy with the new DV Developer. It's a much needed upgrade, and it has allowed me to modernize my stack across the board without having to work around any particular issues. While performance seems quite good, I had never really felt any reason to complain there, so I can't speak about the marketed improvements; yet it wouldn't surprise me given the advancements made (even just in kernels alone).