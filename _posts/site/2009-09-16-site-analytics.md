---
title: 'Site Analytics'
summary: 'RealJenius.com - Watching the watchers.'
tags: personal realjenius.com analytics
category: journal
legacydate: 9/16/2009 19:51
legacyId: 224
layout: post
---

{% assign align='right' %}
{% assign src='site-analytics/statistics.png' %}
{% include image.html %}

With this site refresh, I decided I was going to do something that, for the most part, I avoided on my last site incarnation. I've installed a battery of statistical gathering tools on my site to see how users are using the various globs of data. It has become eye-opening to me just how much data you actually can gather now-a-days. Some of the tools I have configured on Realjenius.com:

* [Google Analytics](http://google.com/analytics) - Thorough statistics regarding site usage and benchmarking against public sites Tracks nationality, IP, user agents, click-throughs, and more.
* [FeedBurner](http://feedburner.com) - Tracks the usage of your site's RSS feeds, and click-throughs on those feeds.
* [Wordpress-Stats](http://wordpress.org/extend/plugins/stats/) - Tracks statistics centric to your wordpress installation. This is particularly valuable for seeing the content on the site in relation to each other (which particular articles are more popular).
* [Bit.Ly](http://bit.ly/) - Shortened URLs for social sites that track received clicks. One of the things I've started to like about this is the track-backs it provides to the actual conversations that mention the link.
* [Google Webmaster Tools](http://www.google.com/webmasters/index2.html) - Tracks search rankings for your site. Probably the most narrow-focused of the tools on this list, but the only one that actually gives insight into the actual GoogleBot indexing process.

What's fascinating about all of this is that none of these tools provide a complete picture. Admittedly, Google Analytics gets the closest, however it's information is high-level enough that the aggregation can hide some fascinating details. Great for overall statistics, however.

Meanwhile, tools like Bit.Ly and FeedBurner let you see the usage of and navigation to your content outside the normal purview of your site.

Overall, the point is that the value of a particular statistic is all about perspective. Different tools see different things, so it's all about the slice of metrics you're looking at, and how you are looking at it.

Additionally, as part of the rewrite, I did finally decided to invest in configuring sitemap tools so that Realjenius.com [would have a sitemap.xml](http://www.realjenius.com/sitemap.xml). It's been interesting to see the influence it had on how Google in particular indexed my site - not sure if it's made a positive difference yet or not - I don't have enough new unique content under the sitemap to be sure.

Time will certainly tell.