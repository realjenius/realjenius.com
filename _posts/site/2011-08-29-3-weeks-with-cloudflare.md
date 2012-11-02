---
title: 3 Weeks with CloudFlare
summary: A few weeks ago I posted that I had just moved to CloudFlare for my Media Temple hosted site. Here are my experiences 3 weeks later.
tags:
- cloudflare
- media temple
- realjenius.com
category: journal
legacydate: 8/29/2011 17:13
layout: post
---

{% assign align='right' %}
{% assign src='/cloudflare/cloudflare.png' %}
{% include image.html %}

A few weeks ago I posted that I [had just moved to CloudFlare for my Media Temple hosted site]({% post_url 2011-08-09-cloudflare-and-mediatemple %}). I received a good bit of feedback on the post, so I thought it would be good to do a follow-up that covers the things I've learned and my overall impression since I first enabled this service.

{% assign src='/cloudflare/views.png' %}
{% include image.html %}

The first thing I will note is that I have had no technical issues with the service as of yet. It has run flawlessly since I turned it on and started tinkering. As you can see from the above graph, a steady flow of traffic has been sent through their system for my site, and I've had no complaints or known issues over the past several days. So far so good on that front.

I received some skepticism about the performance of CloudFlare from folks on Twitter, and although I admittedly didn't share any particular specs regarding the performance, it was clear from the seat of my pants that everything was faster. I decided to go ahead and do some analysis with a dumped cache, Firebug, and some hosts file editing so I could bypass CloudFlare.

Here is a snippet of the network downloads of my site running directly against my WordPress install:

{% assign caption='From my Server' %}
{% assign src='/cloudflare/my_server_timeline.png' %}
{% include image.html %}

Here is a similar snapshot as pulled from CloudFlare:

{% assign caption='From CloudFlare' %}
{% assign src='/cloudflare/cloudflare_timeline.png' %}
{% include image.html %}

As you can see - the latency from the CloudFlare servers is perceptibly better. Also note that while there is a built-in "wait" against my site (the purple section), there is effectively no wait from CloudFlare. Feel free to try this yourself against www.realjenius.com using the IP addresses above, if you'd like.

Now, to be completely fair, this isn't entirely apples to apples. One of the CloudFlare features I didn't tout when I did the initial write-up that I have since enabled is the automatic minification of resources. CloudFlare can automatically minify Javascript, CSS, and HTML in your site markup, and cache the result. This is even part of the free service. While I use some minified resources already, my basic site markup is not compressed (nor is the generated HTML markup for my posts), so there is definitely some benefit to be gained here.

{% assign align='right' %}
{% assign src='/cloudflare/minify.png' %}
{% include image.html %}

When connected to my server directly, without a cache, the initial download was **417.4 KB**. When connected through CloudFlare, the initial download was **293.4 KB**. That's a pretty significant difference.

One other interesting metric: over the past month CloudFlare has delivered hundreds of MB of cached resources on my behalf; effectively free bandwidth for me. Here is a bandwidth graph for my server over the past three months from within the MediaTemple monitoring tools - as you can see, there is a clear reduction (and stabilization) of the bandwidth consumption once August hits.

{% assign src='/cloudflare/bandwidth.png' %}
{% include image.html %}

Overall, after the better part of a month, my opinion of CloudFlare is still a very favorable one. I haven't made the jump to paid service as of yet, but am considering it much more strongly as the days progress.