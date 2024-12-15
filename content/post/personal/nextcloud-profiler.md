---
title: Slow Nextcloud? Try the Profiler
summary: It is very easy for Nextcloud to get slow. The profiler helps to diagnose why
date: 2024-12-15
tags: ["Nextcloud", "Self-Hosting", "Performance"]
---

For friends and family that want a little additional privacy, I self-host a Nextcloud instance as a useful alternative to Dropbox, Box, Google Drive, or other similar cloud storage solutions. It's a powerful and effective tool for a lot of use-cases. However, it can also struggle from performance issues, just like anything else you self-host. After exhausting several obvious paths (slow query logs, cpu/mem monitoring, etc) to discover mine, I discovered the Nextcloud Profiler app and thought I'd share how to use it for anyone else struggling.

<!-- more -->

I'm sure like many people, when I installed Nextcloud I tried out a variety of the Nextcloud Apps from the app ecosystem. Tasks, cookbook, contacts, photos, OpenID, etc etc etc. The ecosystem is so large and the product is so flexible, that it felt like a good plan to see what all it was possible to do with Nextcloud in my self-hosted life.

At some point along the way, after configuring auth, setting up my DB properly, getting large file storage configured, and feeling otherwise satisfied, I realized that Nextcloud suddenly seemed quite slow. And yet, I couldn't figure out why.

Searching the internet for this results in all kinds of suggestions, often of the form "that's the price you pay for how powerful it is" or "it's not actually that bad", or "you are using an underpowered machine, my experience is great!". But then, there are a small number of what I would call "entry-level" suggestions encouraging tuning. Things like:

1. [Your database configuration needs to be adjusted](https://help.nextcloud.com/t/web-interface-very-slow-bad-performance/152757/5)
2. [Nextcloud CRON is misconfigured](https://www.reddit.com/r/NextCloud/comments/lahooa/is_nextcloud_always_this_slow/)
3. [Use Redis](https://www.reddit.com/r/NextCloud/comments/lahooa/comment/glpc7ha/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button)
4. [Your PHP runtime doesn't have enough memory](https://www.reddit.com/r/NextCloud/comments/lahooa/comment/glpd2x3/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button)

None of these are bad, and are all worth checking and doing (though many of these are no longer needed out of the box, as Nextcloud's defaults have gotten better). But, they are all just guessing without proper tooling.

One suggestion jumped out to me however, which was someone saying "my performance improved after disabling App XYZ". This, like the CRON suggestion, line up to the fact that PHP is inherently global, interpreted, and stateless. So, when you load any page in Nextcloud, it's going to be spinning through all of the "per-request" things to see if they have anything to do. CRON, for example, will be checking to see if it should run. Similarly, apps are going to be asked if they have anything to contribute to the rendering page.

After a variety of DB profiling, double checking my Redis configuration, and otherwise fiddling with Nextcloud, I decided it was probably this. But rather than playing the guessing game and binary searching my way through my performance as I disabled half of my apps, I did some more searching and finally found the [Built-in Profiler App](https://docs.nextcloud.com/server/latest/developer_manual/digging_deeper/profiler.html).

The Nextcloud profiler is, in itself, an app. However, unlike most apps (at least at the time of this post) it is not available in the marketplace. Instead, you have to:

1. Fetch the github repo for the app
2. Build it with `npm`
3. Manually enable the app with the `occ` command line via `occ app:enable profiler`
4. Turn on the profiler with the `occ` command line and the `profiler` subcommand via `occ profiler:enable`

Note: if you are running within docker this usually looks something more like:

```
docker exec Nextcloud ./occ app:enable profiler
docker exec Nextcloud ./occ profiler:enable
```

Once this is done, the profiler will be enabled in the Nextcloud UI. The profiler works by logging all of the operations that were done on the page render against the database, the cache, and other external systems (like LDAP), as well as which component (i.e. an app module) ran the code. This results in a flame-graph of sorts as well as a variety of diagnostic information:

![Profile Flame Graph](/img/articles/nextcloud/nextcloud_profiler.png "Profile Flame Graph")

Immediately you can see that something is running *837* queries in this case. (Spoiler, for this case it's the [Cookbook App](https://apps.nextcloud.com/apps/cookbook), and I'm ok with that as it is only when loading the actual cookbook page, and I have lots of recipes needing data).

After looking at the data you can likely find where most of the time/queries/whatever was spent, and then after disabling the offending app, you may suddenly find page loads to be much faster:

![Profile Flame Graph - Better](/img/articles/nextcloud/nextcloud_profiler_2.png "Better Profile Flame Graph")

Nearly 2 seconds faster, and 820 fewer queries. Not bad!

After profiling and fixing your issues, the next step is to make sure you disable the profiler so it doesn't add performance issues on its own. Again, if running in Docker this will likely look something like this:

```
docker exec Nextcloud ./occ profiler:disable
docker exec Nextcloud ./occ config:system:set debug --value false --type bool
```