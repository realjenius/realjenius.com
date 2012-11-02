---
title: 'CloudFlare and Media Temple: Free Goodies'
summary: Media Temple is offering free integration with Media Temple-hosted 
 sites with the CloudFlare CDN performance accelerator.
tags:
- cloudflare
- media temple
- realjenius.com
category: journal
legacydate: 8/9/2011 12:44
legacyId: 686
layout: post
---

{% assign align='right' %}
{% assign src='/cloudflare/cloudflare.png' %}
{% include image.html %}

Today, [Media Temple](http://www.mediatemple.net) [announced a partnership with CloudFlare](http://weblog.mediatemple.net/2011/08/08/supercharge-your-mt-website-with-cloudflare/), a content delivery network and security layer for websites. This is an interesting partnership for a number of reasons:

* CloudFlare performs content caching and delivery. This means that static content will be distributed regionally, and from (ideally) faster and more redundant hardware than your own. CDN distribution is almost always a good thing.
* Additionally, CloudFlare shields your system from many malicious attacks, and also monitors for malicious activity (think botnets, spam, email scraping, etc).
* The CloudFlare integration is, generally speaking, completely transparent. This is possible because CloudFlare is actually a site proxy. Unlike many CDN platforms, where you must route static content to some staticcdn.url.somewhere, CloudFlare is the top-level destination for your DNS, and then internally routes traffic from their system to yours, anywhere it need be dynamic.
* For your first personal site, CloudFlare is free!

As of early yesterday morning (just after it was announced), RealJenius.com has been running on CloudFlare. Over time, most of the traffic has migrated; since it relies entirely on DNS propagation, it takes a little time. I experienced a blip with my local router's DNS cache that temporarily made my site in-accessible, but that had nothing to do with the process, and aside from that it has "just worked". It took me no more time than it takes to brew a single Keurig cup of coffee.

This site runs on a Media Temple (ve) with Nginx, and I had to perform no manual intervention. I clicked through the wizard to set up my site, and despite hosting several sites on my VPS, Media Temple suggested options, and choosing the correct domain re-wired the correct ANames, and everything has seemed to run along perfectly fine.

{% assign caption='The Traffic Starts!' %}
{% assign align='left' %}
{% assign src='/cloudflare/it-starts.png' %}
{% include image.html %}

Since then, I've logged in to CloudFlare, and I'm just starting to see pretty graphs of traffic. The lines indicate different information:

* Green indicates valid requests.
* Purple indicates requests from addresses flagged as spam generators.
* Red indicates threat traffic: bots and other malicious requests looking for vulnerabilities.

CloudFlare also shows bandwidth and traffic savings, and as it stands now, nearly 40% of the traffic to my site has been reduced thanks to the service, and multiple MB of bandwidth. This is prior to full DNS propagation, so I only expect it to increase. Other stats are available about how often search engines have scanned your site, unique visitor counts, and a bevy of information about visitor locality. A lot of the information provides a good corollary to Google Analytics (another service that is surprisingly free).

Up to this point, my description of the service has probably made you think "what's the catch?" - admittedly, I wondered the same thing when I took the blue pill. In reality, there isn't one, other than hooking you with free candy. CloudFlare is a tiered service. The free version, which is what is offered out of the gates to Media Temple hosted folks, is free even directly through their site; Media Temple has just seamlessly integrated the services so it's easier to enable (which it definitely is). They also have a Pro and Enterprise tier - from which there are a number of benefits:

* More control over threat level management - The free version runs with a default threat level, of which you have limited control.
* Real-time traffic statistics - The reason I'm just now seeing traffic stats for my site is simply that they are delayed 24 hours on the free version.
* Better caching - The pro mode promises "faster subsequent page loads" via the use of prioritized/optimized caching (most important resources) and a smart pre-fetching system.
* Other features can be found on their [Pricing & Features](https://www.cloudflare.com/plans.html) page.

CloudFlare's pro plan is currently $20 a month for the first site, and $5 a month for each additional site. Overall, two thumbs up from me regarding ease-of-installation, price, and (at least so far) performance. I'll probably report back when I've had more time under this new umbrella.