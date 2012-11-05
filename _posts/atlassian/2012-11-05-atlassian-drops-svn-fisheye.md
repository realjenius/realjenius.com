---
title: 'Atlassian OnDemand Drops Fisheye'
summary: 'As yet another sign of the coming Git-pocalypse, Atlassian is dropping support for hosted Subversion from Atlassian OnDemand.'
layout: post
category: journal
tags: atlassian ondemand fisheye bamboo svn git
---

{% assign align='right' %}
{% assign src='/atlassian/ondemand_logo_landing.png' %}
{% include image.html %}

The folks over at Atlassian have been providing some form of hosted JIRA+code for some time - starting with a variety of variants of what they called "JIRA Studio", up to their most current cumulonimbus offering: [Atlassian OnDemand](http://www.atlassian.com/software/ondemand/overview).

My company has been using these hosted forms of the Atlassian suite for upwards of three years now for a variety of things:

* Issue Tracking
* Code Repository
* Internal Documentation
* Client Support Tools
* Code Reviews and Auditing with FishEye
* CI Builds with Bamboo

At the time we signed up, the "Belle of the Ball" was [FishEye](http://www.atlassian.com/software/fisheye/overview) with Subversion, and it has served us fairly well, overall. I will be the first to admit that it's had its warts too, as most of you familiar with SVN are probably not surprised. It's only been in about the past year or so that the Atlassian folks have supported any other legitimate options for VCS, and because of the sheer volume of code that we interact with at any point in time between our own products and clients we work with, we have stuck with SVN up to this point. Mountain moving takes time after all.

In a move that wasn't entirely disappointing, but certainly felt aggressive, Atlassian recently announced that they are [dropping support for hosted Subversion altogether](http://go-dvcs.atlassian.com/display/EOL/Source+Review+Bundle+End+of+Service) - and they're taking FishEye out along with it.

Looking back at the various blog entries, open tickets, and other breadcrumbs I found while frothing for better VCS support over the past couple years, I suppose I should've been able to read the tea leaves and see this one coming. They have been putting all of their feature effort into [BitBucket](https://bitbucket.org/) after they bought it, and have made it clear that it was their direction for the future for some time now.

I suppose at some level I don't blame them; FishEye is notorious for the amount of horsepower it requires (having been in a self-hosted environment with it previously, I can attest to this), and frankly, so is Subversion given the relatively poor delta management and the centralized nature of it - since one begets the other, I can imagine that it's a rather ugly hosting proposition. Our experience over the past few years with hosted FishEye has been mediocre at best; the tool is very capable, but the performance in the on-demand space has been hit and miss.

Nonetheless, I found it rather surprising how quickly they have chosen to force the retirement. The cut-over is happening by October of 2013; I wouldn't be surprised if that date moves some (often times their EOL dates do), but all the same, they are not doing any hand-waving about keeping SVN around.

At this point the move to BitBucket/DVCS will comfortably replace most of the features that FishEye has that we actually use, including code reviews and web-based browsing of the code; and of course, Git and Mercurial are both capable of replacing SVN whole-hog - it's really just a matter of the system-administration and ramp-up for the team.