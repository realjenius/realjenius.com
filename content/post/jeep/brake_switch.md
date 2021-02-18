---
title: 'Jeep JK Wrangler Code P0571: Brake Switch Fix'
tags: ["Jeep","Wrangler","JK","P0571","Brakes","TCS"]
date: 2021-02-17
---

At the start of the year, my Jeep began reporting a failure in the traction control system, as well as eventually starting to throw a `P0571` check-engine ODB-II code. This post describes what I discovered as the root cause, and what could easily be your issue if you have had similar symptoms.

<!--more-->

Over the course of the tail-end of 2020, I had noticed once in a very rare occasion that my Jeep's TCS failure light would turn on. This light is also sometimes colloquially known as "Car Snakes" because, perhaps unsurprisingly, it looks like a car being chased by snakes.

{{<figure src="/img/articles/jeep/jeep_snakes.png" caption="Jeep Snakes">}}


Note: Jeeps actually have two dash lights very similar to this: one that says "OFF", indicating that traction control is intentionally off, and another which doesn't have any words which indicates that the TCS system is off because the car's computer has detected an inconsistency and isn't confident it can safely enable TCS without causing other problems.

Traction control in Jeeps (and the huge majority of cars) works by selectively enabling the brakes automatically on individual wheels to attempt and restore control when it detects sliding or other traction issues occurring. To do this, two key things are needed by the onboard computer:

1. Wheel Speed Sensors: Wheel speed at all four corners so it can compare how fast the wheels are turning relative to each other, which can indicate slipping
2. Electronic Braking via ABS: The computer's ability to apply the brakes automatically via the ABS (antilock braking system)

As a result, if either of those systems are disrupted (or the computer is confused by what it is detecting), TCS will be disabled by the computer for safety reasons. Based on just that, we know that my issue with my periodic TCS failure light was *likely* related to wheel speed or brakes. At the time I was suspecting wheel speed sensors, as they can be impacted by a variety of external factors. But then my Jeep threw a check engine code: `P0571`.

{{<figure src="/img/articles/jeep/p0571.png" caption="DTC Code Thrown">}}

(I happen to have a radio that can read OBD II codes, but any OBD II scanner could find this)

[P0571 is a "Cruise Control/Brake Switch A Circuit Malfunction"](https://www.yourmechanic.com/article/p0571-obd-ii-trouble-code-cruise-control-brake-switch-a-circuit-malfunction-by-ian-swan) code, which seems to indicate a brake issue. This strongly implies that the problem likely involves an issue with the braking system somehow.

After learning this bit of trivia, I reset the code, and then I started to pay attention to when the error was thrown by my Jeep, and sure enough I was able to correlate it to (only sometimes) when I was braking; coming up to a stoplight, reversing down the driveway, etc.

After doing some online researching (and I apologize I can't find the original reference now) I learned that on the Jeep (as on many cars), there are two independent brake sensors:

1. One that goes through the TIPM and main computer based on the brake being applied
2. One that is wired directly to a switch behind the brake pedal that indicates the physical position of the pedal

Notably, I learned that on the JK (and most modern Wrangler variants), the "3rd brake light" is wired to the physical pedal switch, while the main brake lights are routed through the TIPM.

This gave me the idea to see if I could get one set of lights to illuminate without the other -- and sure enough, with a very gentle push on my brake pedal, this would happen:

{{<figure src="/img/articles/jeep/3rd_brake_light.png" caption="Help was required to get this photo">}}

This wasn't always happening, but given a gentle enough pedal push (brake coasting) it would happen every time. What this led me to believe is that the physical pedal brake switch was worn out, and was not lining up perfectly with the pedal anymore.

Here is what this switch looks like -- it's a small plastic module with a plastic spring-loaded plunger sticking out one end:

{{<figure src="/img/articles/jeep/brake_switch.png" caption="The malfunctioning part">}}

This plunger is physically in contact with the metal arm of the brake pedal... unless of course the plunger is misbehaving or is misaligned. Some sites online discussed that this plunger could be re-aligned, but after finding Mopar official replacements online for 20 bucks, I simply ordered a new one.

Replacing the switch is thankfully quite straightforward. The first step is to take the lower dash panel out from under the steering wheel:

{{<figure src="/img/articles/jeep/outer_dash_panel.png" caption="Remove the outer panel">}}

Then, easy access can be gained by removing this metal access plate:

{{<figure src="/img/articles/jeep/access_panel.png" caption="Remove the inner panel">}}

After this, the switch can be seen, and most arms should be able to reach through the access panel.

{{<figure src="/img/articles/jeep/installed_switch.png" caption="There it is!">}}

To replace:

1. Turn the module counter-clockwise, and it will unlock from the mounting plate.
2. Disconnect the wire harness
3. Connect the replacement with the wire harness
4. Press the replacement into the mounting plate, and turn clockwise until it locks into place

As with all car repairs, this may or may not be your issue, but given that this was an easy and cheap fifteen minute repair, I figured I would share in the event this has happened to you as well!
