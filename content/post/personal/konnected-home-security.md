---
draft: true
---

As someone who had a home built in 2004, I have desperately wanted to avoid paying the "ADT" tax to have my home alarm equipment unlocked and attached to their byzantine security ecosystem. In 2016, I was an early adopter of SmartThings and have been using it ever since to pay attention to my home, but it meant ignoring those lovely pre-wired resistance-based door sensors already in my home. However, As of today, I finally get to use my home's built-in sensors with SmartThings, thanks to the [Konnected Alarm Panel Pro](https://konnected.io). Let's take a tour of this cool product.

<!--more-->

In 2015 I moved into my current home. At that time, the smart-home market was just slowly opening up, with Zigbee and Z-Wave technologies starting to show up on the market, finally unraveling the super-insular, super-proprietary market the existed before. At the time, the small startup [SmartThings](https://smartthings.com) was one of the best user-experiences in this new marketplace available at the time, and I quickly bought in to that ecosystem. To this day, despite a very "cloud-only" experience, and of course later being acquired by Samsung, I don't regret that purchase, as it has been an excellent platform for connecting and making "smart" most of my things, including lights, door and window sensors, garage sensors, leak detectors, sprinkler systems, and other weird automated fun in the home. Further, so far, Samsung hasn't explicitly broken my trust after the purchase... though I'll admit the relationship has given me anxiety.

However, my home in 2004, was built with a fairly standard-at-the-time alarm system that supported:

* Door Sensors and Window Sensors
* Smoke Alarm Detectors
* Motion Sensors
* Window-Break Sensors
* In-wall Key-pads
* 24-7 monitoring with support

In other words, ADT... and in my case, a quite standard an Ademco Honeywell alarm panel to drive it all. While the 24/7 alarm monitoring was important to me, it was always frustrating to me that (a) it required a relationship with a specific company and that (b) when the last owners of this home canceled their alarm relationship, the alarm system was "locked" with a pin-code, effectively making this entire panel useless to me and all the sensors inaccessible.

So of course, it has always nagged at me that I had to completely ignore the pre-wired sensors in my home. The 24/7 support would be nice, but I felt like I should be able to find that in other ways. So, I just put on my own sensors and ignored the pre-wired equipment, with a mild bit of disdain.

<!-- TODO door sensor -->

<!-- TODO break sensor -->

My SmartThings purchases included a variety of replacements for these various systems; here we can see one of my SmartThings motion detectors right next to a glass-break sensor in a given room. Of course, this resulted in me basically having situations where I had one "built-in" sensor doing nothing, sitting right next to a "stuck-on" sensor using a CR-2450 battery, of which I've grown increasingly tired of buying and replacing over the years.

<!-- TODO - smartthings next to in-home -->

At the same time, over the last several years, several "alt-alarm" companies like [Simplisafe](https://simplisafe.com/) have popped up that basically use a newly-proprietary alternative to my existing SmartThings setup (battery powered contact sensors), but include the 24/7 monitoring. I'll even admit being lured by them to an extent, however with the fact I already own tons of sensors for my current setup and a ton of other "SmartThings-capable" things, this was investing in exactly the wrong thing.

Then, SmartThings announced their [ADT Alarm Package](https://www.adt.com/smartthings which was 24/7 monitoring with SmartThings monitors. Oh, the irony. Now I could get the 24/7 alarm monitoring, but not on my built-in (ADT-lock-in) sensors on my home... of course, I chose to ignore this, and I'm glad I did, as they discontinued this product almost immediately after launching it.

In reality, my in-home sensors are actually quite simple (as are most pre-smart-home door sensors). They are mostly-two-wire analog systems in design (sometimes four-wire for in-device voltage), but they had no *simple* integration options for anything related to modern smart-home automation. There were options on the web, but I'm *far* from an electrical engineer on the best of days, and didn't want to try my hand at making custom solutions work, just to eliminate this duplication. In my research, it became increasingly clear that there were a *lot* of smart-home yeomen like myself who had these home alarm systems and didn't want to deal with either ADT or some other fly-by-night alarm company to work with our old phone-base alarm systems, but also didn't want to buy in to a new "easy but still proprietary" system like [Simplisafe](https://simplisafe.com/).

Thankfully, this Autumn (2021) I stumbled upon the [Konnected.io Alarm Panel](https://konnected.io) system, which checked all of the boxes I was looking for:

1. Connected to legacy alarm system door monitors, window monitors, motion sensors, window-break sensors, and other security system accompaniment
2. Connected to your home via *very* flexible power solutions (~12 volts with < 1k MA power draw)
3. Wifi-Capable so no hard-wired Ethernet has to be provided
4. SmartThings and Hubitat/Home-Assistant friendly (meaning that even if Samsung betrays my trust, this isn't a wasted investment)

So I ordered the kit, and it showed up only a week later, ready for install. The entire experience with this company has been very positive. They, like many companies of this stripe, started as a Kickstarter that inevitably proved out a market waiting for buyers, and have since been "productizing" into something that is worth buying. Their tech is, as you might expect, a PCB with a variety of connectors and micro-controllers that is meant to make things simple.

<!-- Boxes -->

<!-- 12 alarm system picture -->

I chose to go with the 12-zone "pro" system, as I knew I had at least 8 zones (without considering serial wiring which is a whole separate fun thing in these old homes), and didn't want to deal with trying to figure out multiple installations.

The delivery was quick, the packaging was nice, and the product was exactly as-advertised. In my order I got a standard wall-receptacle power adapter, an accessory kit including screwdrivers, wire-labeling stickers, and window stickers, and finally the actually wired smart system itself.

<!-- pictures of alarm system -->

It turns out, old alarm systems were kind-of weird, but were also quite powerful and using all kinds of connectivity that the rest of the world has had to catch up to. Most of this was built into these boards installed oh-so-many-years-ago. These boards would take in A/C power, usually supported a backup battery, wired in to all kinds of sensors, typically connected to an [RJ-11](https://en.wikipedia.org/wiki/Registered_jack#RJ11,_RJ14,_RJ25_wiring) phone line for emergency dial-out (via modem behavior, basically), as well as potentially via Ethernet over something like an [RJ-45+](https://en.wikipedia.org/wiki/Registered_jack#RJ45S) adapter to enable online access to the status of things.

Now, in my case, I knew that things that required power were wired and working fine with the old AC-based alarm system. In fact I w   anted to avoid touching any of the 5 or 12 volt-based systems that existed. So I explicitly chose to leave these sections of my alarm panel alone:

<!-- diagram with circle -->

<!-- real picture with circle -->
