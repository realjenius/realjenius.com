---
title: 'Diagnosing a 3.6L "Pentastar" Jeep JK Radiator Fan Issue (Code: P0480)'
tags: ["Jeep","Wrangler","JK","Pentastar","Cooling","Radiator","Fan","P0480","3.6L"]
date: 2018-06-28
---

Recently, I was driving my 2015 Jeep Wrangler JK Unlimited through a parking lot on a 95 degree day, and after leaving a
stop sign, a check engine light flashed on (later I would learn it was a [P0480 Cooling Fan Relay 1 Control Circuit](https://www.obd-codes.com/p0480)), and shortly thereafter my Jeep overheated. What followed was a multi-day Google searching and diagnostic session, which made me realize there is a lack of information as well as a lot of mis-information about the 3.6L "Pentastar" Jeep Wrangler model years, and how the radiator fan works.

This is my summary of what I learned about the cooling fans on these Jeeps, so hopefully more people can diagnose issues themselves.

<!--more-->

Once the code triggered on my Jeep, I immediately pulled over and parked, but I left the engine running. Thankfully, [my stereo has an OBDII diagnostic link](http://www.kenwood.com/usa/car/navigation_multimedia/ddx9703s/), so I was able to immediately get the code, which as mentioned above was a `P0480`, which means the radiator cooling fan isn't working right. I knew it wasn't just some sensor issue, as while I was sitting there googling in a parking spot, my 3.6L quickly began to overheat, reaching 230 degrees and quickly climbing. I did some quick analysis while sitting there, and realized the cooling fan was indeed not spinning at all. After doing a quick scan of fuses and doing a relay swap for my "best guess" based on the owner's manual, I quickly realized this was not going to get fixed right there. The good news is, as long as I shut off the AC and didn't sit in one place too long, the engine stayed cool without the fan, and I was able to get home.

Unfortunately, I quickly hit a dead-end. The internet knowledge of the 3.6L pentastar fan setup is woefully inadequate. I suspect there are a few reasons for this:

1. In most every other aspect, the JK Jeep models ran from 2007-2018, and things like suspension, interior, brakes, etc. were consistent throughout all the years. So, when you google for fan fixes on a JK jeep, you run into a ton of knowledge for 2011 and prior, which is an entirely different engine and fan setup.
1. The fan uses a good bit of black magic in its design as compared to more traditional mechanical clutch fans and electric fans
1. There are a lot of false trails left in the Jeep from the older installation that you think matter but actually don't.
1. The newer Jeeps are... newer, and as a result parts are failing a lot less often. I suspect that as they age and parts fail more, this knowledge will become a lot more common.
1. Chrysler products in general share a ton of parts, and so you wind up finding articles that seem related about cars like Dodge Caravans, Jeep Compass, Jeep Renegades, etc.

Here are some of the articles I hit (a sampling):

* [This article looked promising, but the conversation stopped](https://www.wranglerforum.com/f274/2015-radiator-cooling-fan-relay-2299097.html)
* [This article mislabels the advice to "present" Jeeps and much of the advice is invalid](https://www.jk-forum.com/how-tos/a/jeep-wrangler-jk-why-does-my-cooling-fan-not-work-407882)
* [Interesting advice about debris in the fan, but lots of other misleading info](https://www.jk-forum.com/forums/stock-jk-tech-12/p0480-cooling-fan-failure-diagnostic-help-139208/)
* [An example of someone with 3.6L fan issues that hit a dead end like me](https://www.wranglerforum.com/f202/cooling-fan-s-problem-p0480-2305106.html)
* [Someone with knowledge, but only a little bit shared](https://www.jkowners.com/forum/modified-jk-tech-dept/356578-p0480.html)
* [More misleading knowledge about testing the fan and what to check](https://wayalife.com/showthread.php/29819-Cooling-fan-broken-3-6-help)

I found a lot more than this as well - the forums are full of P0480 knowledge, but very little of it touches this model.

To recap, the 2011 and earlier Jeep JKs (with the 3.8L engine) have a fan setup with these characteristics:

* Run on a 60A fuse
* Have a low speed relay in the fuse box
* Also have a high speed relay near the driver's headlight
* Include a few extra bits of circuitry (more relays) that help trigger the fan on when the AC is running, or the engine temp crosses a certain level.
* In addition to throwing a P0480, these fans can also throw a code in the P069X range.
* Can be easily bench tested by jumping a power and ground wires to the fan harness directly to see if it spins up or not, and to see if the fan struggles or clutches correctly, or if it might be drawing too much current, etc.

As you can probably tell, I have collected a good bit of knowledge about the earlier Jeep JK fans. My hope was there would be a lot of parallels between the two powertrains; after all there are already relays and fuses and other equipment already in the Jeep for this; surely for cost savings they would reuse most of that with the newer engine. Unfortunately I have since learned that nearly all of this is incorrect for my Jeep.

The 2012 and up Jeep JK wrangler fan on the 3.6L Pentastar has a few key characteristics:

* It is an [Outrunner](https://en.wikipedia.org/wiki/Outrunner) motor design. This doesn't have much bearing on diagnosing problems, but it's a key difference from the more traditional fan designs in the earlier Jeep JKs
* The fan is a 3 wire design, which you can see if you detach the harness. The three wires are:
  * Heavy gauge power directly from the battery
  * Heavy gauge ground wire which terminates behind the power steering fluid reservoir
  * A small gauge wire that is the "[pulse width modulated duty cycle signal](https://en.wikipedia.org/wiki/Duty_cycle)" from the circuitry in the TIPM (aka smart fuse box)

Now, typically, a system like the old JK setup will have relays to do "high amperage" power switching. For example, if a fan needs 60 amps to run on high speed, the high speed relay acts as a large switch which, when enabled, sends the high amperage power to the fan to run. As soon as the power reaches the fan, it kicks on. However, given the description above you'll notice the only voltage going to the fan (other than the piddly PWM wire) is coming directly from the battery; no relays, and therefore no external control.

What that means is all of the circuitry for enabling the fan and controlling the fan speed is contained *inside the fan assembly itself*. The fan expects to get power from the battery at all times (so it can run even when the car is off), and translates the PWM duty cycle signal from the computer in the TIPM into a variable speed for the fan itself.

Consequently, the fan for a 2011 and prior JK Jeep is around [$65](https://www.carparts.com/details/Jeep/Wrangler_-openp-JK-closep-/Replacement/Cooling_Fan_Assembly/2008/REPJ160502.html?TID=gglpla&origin=pla&gclid=Cj0KCQjwjtLZBRDLARIsAKT6fXxaEGTp6J7OKZ2vTuQlzzWNrH5xrYb_NySo3sWXifHyob8gxyNxBY4aAo3qEALw_wcB&gclsrc=aw.ds) to [$200](https://www.extremeterrain.com/omix-ada-fan-assembly-jk-1710257.html?utm_content=XT%20Engine%20and%20Performance%20-%20Radiator%20and%20Cooling%7COmix-ADA&utm_source=google-pla&utm_medium=shopping&T5_Var2=shopping&T5_Var3=yellow&T5_Var4=J12191&intl=0&utm_campaign=XTW+Wrangler+Vehicle+Medium&dialogtech=ppc&gclid=CInFpMPB9tsCFcaTxQIdUgYNZA&gclsrc=ds) dollars, and there is a ton of aftermarket competition. However, the "fan assembly" for the Pentastar is pretty much only made by Mopar (Part 68143894AB), and it costs [$400](https://www.oechryslerparts.com/oem-parts/mopar-fan-assembly-68143894ab?origin=pla&gclid=Cj0KCQjwjtLZBRDLARIsAKT6fXyTfiropWMqN8KSPXjuuv93hLMD96arw6RUeJ1NXz5LzY6fYjij0yAaAmbfEALw_wcB) on a good day.

Part of the reason for a lack of an aftermarket is the complexity of having to reproduce a competitively priced setup that can do the same thing while keeping the factory computer from throwing codes. To work with the factory setup, any fan assembly has to:

* Read the pulses on the signal wire correctly (or ignore them and be less efficient or require an alternate signal source)
* Translate those into variable fan speeds (or ignore them... see above)
* Provide the correct resistance back to the TIPM computer so it will not throw an OBDII code

Aside from this complexity, another major reason there isn't much aftermarket is that the 3.6L fan itself is actually pretty good. The outrunner design makes it efficient and reliable, and [based on others' testing](http://www.jkowners.com/forum/stock-jk-tech-dept/270930-inside-pentstar-cooling-fan.html), even in heavy off-roading setups the 3.6L fan is able to keep up just fine.

So, I knew that the fan wiring seemed deceptively simple. But what I also knew was that there are tons of parts under the hood that are labeled as belonging to the fan. Notably:

* The 60A fuse (J19) in the TIPM labeled "radiator fan"
* The Relay in the TIPM (K11) labeled "Radiator Fan Low"
* The dual relays over by the brake master cylinder labeled "Fan Relays" on the internet (Mopar Part 56055666AB)
* The lack of a P069X style code being thrown led me to believe this was a unique problem with only the "high" speed part of running the fan.

I checked and re-checked all of this in hopes of a cheap and easy fix. You can see some of my research notes here: [P0480 - How to Diagnose 3.6 PWM Fan Issues](https://www.jk-forum.com/forums/stock-jk-tech-12/p0480-how-diagnose-3-6-pwm-fan-issues-346949/)

However, as it turns out, none of this is used for the radiator fan. As best as I can tell, some of this is leftovers from the old design, and some of this is likely used for other things (I suspect the fan relays are actually for the blower motor and AC system, though I don't know for certain). I stared at this wiring diagram ([found here thanks to BBB Industries](https://www.wranglerforum.com/f274/2015-complete-wiring-diagram-1235497.html)), trying to figure out why the above components weren't on it.

{{<figure src="/img/articles/jeep/jeep_fan_wiring.png" caption="The Sparse Wiring Schematic for 2013+ Jeep Cooling Fans">}}

Why aren't the components on it? Simple: they aren't used!

Also, as far as I could tell, the only code the fan will ever throw on these newer jeeps is P0480, which is all encompassing to mean the TIPM asked the fan to turn on, and couldn't detect that it did, or the engine overheated under a certain MPH and the computer doesn't know why.

Assuming you are getting valid temperature readings from the engine temp sensors, the only components you have to worry about when diagnosing the 2012+ Pentastar 3.6L JK radiator fan not running are:

* The Fan assembly itself
* The TIPM itself (aka, the computer for most accessories in the car)
* The power lead from the battery
* The fusible link (S108) between the battery and the power lead
* The ground to location G904A (behind the power steering reservoir)

Here are some pictures for orientation after I dismantled my engine compartment to trace everything as a last ditch effort before taking it to the dealership.

{{<figure src="/img/articles/jeep/fan_harness.jpg" caption="The Fan Harness with Three Wires">}}

{{<figure src="/img/articles/jeep/ground_location.jpg" caption="The Ground Location">}}

{{<figure src="/img/articles/jeep/fusible_link.png" caption="The Fusible Link Connecting to the Battery">}}

Unfortunately, testing the TIPM output requires a TIPM diagnostic tool (dealer equipment) or at least an oscilloscope so you can visualize the duty cycle square-wave coming from the PWM signal (even if you don't know it's right, seeing it would add confidence). It's not something I was able to ever actually capture using my multimeter.

Also unfortunately, you cannot simply bench-test the fan. Believe me, I tried. If you jump the fan to power and ground and try to provide a direct 12V signal to the PWM lead, nothing will happen and you will think your fan is broken, when in actuality it may just be ignoring the signal because it is not a valid duty cycle.

So, inevitably you *may* have to go to the dealership for a diagnosis (or you may just blindly replace the fan and the TIPM for a cool $1000 in parts and a bunch of time in your own garage), however, before you do that, it's worth checking everything else.

So, what was it in my case? Shockingly simple: The 10mm nut holding the fusible link to the battery had worked itself loose. It was still touching, but it was not a tight fitting. As a result, I was testing the power to the fan with a multimeter and getting a solid 12-14 volts, but the connection was not reliable enough to provide the high amperage demands of the fan, so the fan was not running, and the code was being thrown. If I had to play nostradamus, I'd guess that months ago when I installed my stereo I managed to back this nut off some (likely on accident) and didn't notice. It just finally worked itself loose with enough bumps and jerks to make the fan unhappy.

If that isn't your case, the other two likely causes:
1. The ground - if the ground is not stable and strong, you'll get the same symptoms I experienced.
1. The fusible link - This is just a stretch of wire that serves as a fuse, and is inline between the battery and the main fan power wire. If it fails, you will know with a multimeter at the fan harness as you won't be able to get a 12V signal at all (which does not have to be the case with loose fitting connections). Note however, if the fusible link is blown it could mean your fan is over-drawing current. The link exists to prevent the fan from pulling too much power. That typically happens with fans when the motor is overworked or possibly failing. If this is happening to you, and after replacing the fusible link it happens again, you are probably due for a fan replacement. A lot of forum posts talk about cleaning mud and debris out of the fan - certainly anything that inhibits the fan rotating should be cleaned out before throwing the whole assembly out.

Thankfully, both the TIPM and fan assembly are relatively easy to remove and replace based on my research. I nearly had the fan out of my Jeep in 15 minutes after removing the air box and some of the surrounding parts to verify the wiring (however, it turns out I didn't need to). [This article describes the TIPM replacement process](https://www.jk-forum.com/forums/jk-electrical-lighting-sound-systems-13/jk-tipm-problem-solved-266688/) -- while I didn't have mine *completely* out, I did have it unbolted from the Jeep and was able to see under the unit. It's expensive, but not hard to swap out.

Good Luck Jeep owners!

{{<figure src="/img/articles/jeep/jeep.jpg">}}
