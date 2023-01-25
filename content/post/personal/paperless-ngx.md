---
title: "Paperless-ngx - Adulting in 2023"
date: 2023-01-24
tags: ["paperless","personal","evernote","paperless-ngx"]
draft: true
---

This year, I switched to hosting my own installation of [Paperless-ngx](https://docs.paperless-ngx.com/), and it has leveled up my paperless adulting life in several ways.

<!--more-->

Since about 2015 I was using Evernote as a home for paperless filing via [NoteSlurp]({{< relref "./paperless-noteslurp.md" >}}), which is a bespoke set of tools I wrote to efficiently ingest my scanned documents from Linux. I've been using some form of Evernote (originally their Mac OS application with filesystem sync) since around 2014 for my paperless filing needs, and it served me well for some time. I chose Evernote years ago as their OCR seemed to be unmatched (specifically better than other cloud drive solutions), they had tools for auto-synchronizing as well as published APIs, and there were a variety of powerful organization schemes available with tagging. Evernote did a few things, and did them all well, so it was an easy subscription fee to pay for.

However, in more recent years I've grown to like a lot of things about Evernote less and less:

* The OCR seems to have faltered, and is no longer better than even those things freely available with open-source tools
* Their new web and mobile experiences are atrociously slow, with an incredible amount of up-front SPA bloat, and seemingly a need to rebuild the entire experience every time I open the app
* Even if the new experiences were fast, they aren't exactly out of your way and pleasant to use, most of the newer features they've added have been for an audience that is clearly not me
* They have risen prices on me three times in the last 8 years, and will not stop trying to upsell me with ads for their other tiers, which I have no interest in
* Their API is stagnant and missing important features to enable my Linux-centric workflow
* The sheer number of "bespoke" tools I've had to scrap together in NoteSlurp has increased multiple times a year

With the release of [TrueNas Scale Bluefin](https://www.truenas.com/docs/scale/scale22.12/), I decided it was time to revisit my home NAS situation, which was really just some extra drives in my desktop Linux machine, and needed some more "official" treatment. Along with that, I decided to check out various purpose-built paperless filing tools. I did some trial installations and experiments with several that could easily be hosted on my new NAS that were recommended by [Awesome Self-Hosted](https://github.com/awesome-selfhosted/awesome-selfhosted) - here are my thoughts on each:

* [PaperMerge](https://www.papermerge.com/) - PaperMerge has a lot of potential. The intake pipeline is very rich and the ability to work with so many document types, document merging, document versioning, and others made it very compelling. However, at the time of my review, v2.0 and v2.1 both existed, and 2.1 is a rewrite of 2.0 that has a fundamentally different storage system, and is missing features. In other words - as best as I can tell, in a year this might have been the winner
* [Teedy](https://teedy.io/) - Teedy has a lot of compelling features as well, though I didn't like the "Document vs Attachment" hierarchy concept as the default, which made creating documents manually quite slow (most of the time, my attachments *are* my documents), though I did like that at the time I reviewed it, it could ingest emails, which is a very common workflow with me and Evernote
* [DocSpell](https://docspell.org/) - This is an interesting project, but is simply not as far as long as some of the others in certain places
* [Paperless-ngx](https://github.com/paperless-ngx/paperless-ngx) - The UI for this is the most efficient for intaking documents, and it matches my "80%" case of my single attachments being "the document". The OCR is very solid, and adding support for Windows-esque document types is a simple case of linking to a few other Docker images (Tika and Gotenberg). The biggest issue at my trial of this project was that the email support *only* could ingest attachments, and could not ingest raw emails as if they were documents themselves

As if reading my mind, a new release of Paperless-ngx became available with e-mail ingestion support. As a result, it became the clear winner and I chose to jump right in. After ingesting more than 7000 PDFs, emails and other documents from my archive, I can honestly say it is an ideal replacement for my previous workflow, and only adds efficiencies.

Here are a few highlights of the experience:

* OCR auto-association
* Intelligent tagging
* keeps originals organized as you see fit
* scans a directory
* scans an IMAP folder path with sophisticated "post-ingest" operations
* supports forward auth configurations for tools like Authelia or Authentik



