---
title: "TrueNas Scale: Resolving Docker Deployment Timeouts"
date: 2023-01-25
tags: ["truenas","truenas-scale","docker","self-host"]
---

Recently I've been using [TrueNAS Scale](https://www.truenas.com/truenas-scale/) at home, and exploring self-hosting a variety utilities for my personal life. As part of that I've run into at least one case where, despite Google Fiber, I was unable to fetch docker images for deployment in time. Here's an easy way to fix that.

<!--more-->

TrueNas Scale has a compelling Helm+Kubernetes-based application hosting solution for things you might want to self-host for personal life improvements. Examples include things like: [Paperless-ngx](https://docs.paperless-ngx.com), [AdGuard](https://adguard.com/en/adguard-home/overview.html), [Plex](https://www.plex.tv/) or [JellyFin](https://jellyfin.org/), [NextCloud](https://nextcloud.com/), and others.

I've been exploring options for self-host replacements of Google Photos. As a result, I recently tried to install LibrePhotos on TrueNas Scale using the excellent [TrueCharts](https://truecharts.org/) chart suite. Unfortunately, LibrePhotos timed out every time I attempted to install it. After looking at the Application Events, I saw this repeatedly:

```bash
errimagepull rpc error: code = Unknown desc = context deadline exceeded
```

As it turns out, the docker image was large enough that, despite me having 2.5 Gbps home internet, I was unable to pull it down before the deployment timeout would trigger, though it took me a while to confirm this was the case.

Thankfully, it turns out the solution to fix this is pretty straightforward, assuming you have shell access set up for your Truenas Scale machine. With enough searching, it turns out this is a common problem, and not always properly answered on forums and in various issues. Generally with TrueNas, to resolve a timeout due to docker image download failure during deploy, you simply need to manually fetch the image so it is locally cached before attempting the deployment.

To do this, log in to the shell (e.g. through SSH or through `System Settings > Shell`), and simply pull the image via the docker command line. Unlike the deployment process, `docker image pull` will not timeout after a fixed time window. To get the docker image name to actually use to fetch the image you might fight the application events in TrueNas truncate the image name with an ellipsis. For example, for nextcloud it might look like this in the TrueNas UI event log: `tccr.io/truecharts/nextcloud-fpm:25.0.2@sha256:59e6d2be5139cdeb030a095fb92b97e01d7d53071dc34b48795606...`. To find the full value, you can typically look at the chart specification over in the TrueCharts Github repository to find the full value. For the above example, looking here (at the time I wrote this) you can see the full docker image SHA hash in the `values.yml` in nextcloud's chart: https://github.com/truecharts/charts/blob/master/charts/stable/nextcloud/values.yaml.

Once you have the value, simply run it in the shell and wait until it reports completion:

```bash
docker image pull tccr.io/truecharts/nextcloud-fpm:25.0.2@sha256:59e6d2be5139cdeb030a095fb92b97e01d7d53071dc34b487956065a385d3a32

tccr.io/truecharts/nextcloud-fpm@sha256:59e6d2be5139cdeb030a095fb92b97e01d7d53071dc34b487956065a385d3a32: Pulling from truecharts/nextcloud-fpm
3f4ca61aafcd: Pull complete 
460703cf6140: Pull complete 
eba06349db87: Pull complete 
9130a4183abd: Pull complete 
2f05209be6c5: Pull complete 
94128e45b6c6: Pull complete 
9a9e149b2dc3: Pull complete 
a2120d157865: Pull complete 
a213d22620fe: Pull complete 
a173661a951b: Pull complete 
8ce36e622f58: Pull complete 
b6119d0efbb2: Pull complete 
7d053ffda6fd: Pull complete 
4d095a476643: Extracting [=======>                                           ]  25.07MB/
```

Once that's complete, try the deploy again, and it should work!