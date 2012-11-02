---
title: 'RealJenius.com - Now With Less PHP'
summary: 'The new RealJenius.com runs <a href="http://www.playframework.org">Play! Framework</a> and has no database on the back-end.'
tags: personal realjenius.com
category: journal
layout: post
---

I have long time been a proponent of WordPress for a quick and painless blogging platform. For years I toiled to create 
a favorable personal blogging platform; re-inventing it time and again in some variant of Java technologies, and constantly being fed up. Despite being a good learning experience, my custom blog was always a thorough pain in my ass to maintain.
That's what made Drupal, and later WordPress, so appealing. I was able to focus on what I wanted to do, which was deliver    content, without having to futz with my rushed and unfinished web forms. I knew that my site software was going to be
maintained and regularly audited by community reports with respect to security, etc. Unfortunately, despite the flexibility
and plugin architecture, WordPress carries a certain degree of cruft with it:

* PHP is surprisingly hard to host scalably and efficiently.
* WordPress needs a rack of plugins installed to be even remotely performant - most of it gross caching schemes.
* I rather despise maintaining relational database installations.

I've never been a fan of PHP in any shape or form, so I think it says a lot about how burnt out I had become on trying to 
build a one-man CMS, that I was using WP, and singing its praises.

A post over at [Nesbot.com](http://nesbot.com) inspired me to revisit my embargo on personal blog coding, as Brian Nesbitt [detailed how he migrated his blog 
with Play 2.0](http://nesbot.com/2011/11/22/now-running-on-play-2-beta). What struck me in particular was, not so much that he was using [Play!](http://www.playframework.org),
nor that he was now using the shiny 2.0 beta, but that he had built his site without any complex data-store on the back-end.

Suddenly: lightbulb! This triggered me to explore the ideas he so graciously shared with his public site code on github:
[https://github.com/briannesbitt/nesbot.com](https://github.com/briannesbitt/nesbot.com). Of particular note:

* His blog entries are simply Play! UI templates.
* The entire blog-entry history is loaded in memory at startup, and cross-indexed for the various navigation.
* The comment system is provided by [Disqus](http://www.disqus.com).
* Blog entries are committed to GitHub, and ostensibly pulled to his server where he then simply restarts the 
server to load the new article.

I didn't spend much time actually digesting his code, but I appreciate that he posted his approach online, as it
immediately got my gears turning. While I'm not quite ready to share the source of my site (more on this in a moment),
I will share some details on the approach I've taken:

* Each of my blog posts is a template file, and has a "meta" comment on the top. This defines things like the
title, a short summary, the publish date, tags (comma-separated), the category (journal or article),
and optionally some additional compatibility bits, like a legacy id (see below). The comment is YAML formatted
so I have some leniency as I'm authoring, and good error reporting if I get the formatting wrong.
* The file name represents the blog "slug".
* On startup, I spin through the entire post directory recursively, reading the header comments one-by-one, and
tossing them into a series of in-memory indexes for traversal. The collections are sorted in date descending order,
as that's the most generally useful ordering for the site.
* The re-load process can also be triggered by an administrative call through the HTTP interface. The reload is completely free of locks via some judicious use of atomic references.
* I'm also using [Disqus](http://www.disqus.com) for my comment system, like Brian. Importing from my
existing wordpress site was, for the most part painless. I simply installed the Disqus plugin into my WP install,
and did an export. The hardest part was supporting legacy identifiers. Turns out the IDs that
it uses are: `[wpid] http://[site-address]?p=[wpid]` - this is where the legacy ID entry comes in to play above.
* I have a private git repo on my server, and every time I write an article, I simply push to the git repo. The server then pulls from the git repo, and I run the administrative reload.
* I'm using [SyntaxHighlighter](http://alexgorbatchev.com/SyntaxHighlighter/) for code highlighting. The highlighters that are declared in the post are loaded in "on-demand", so the included JS is kept to a minimum.
* I have a few custom Play! tags to make writing blogs easier - they do things like reverse routes to other blog posts into anchor tags, create captioned image markup, and dynamically generate series blocks (as seen in my [Distilling JRuby](/tags/distilling-jruby) series).
* I'm using a Play profile ID on my server, and %prod. prefixes to support dev and prod properties concurrently
in the same file.
* I'm using a variant of [This Ubuntu up-start script](http://www.playframework.org/community/snippets/17) to bootstrap my Play! Framework runtime.
* Thanks to the expressiveness of the the Play routes system, I was able to effectively model my URLs without any
change in pathing to match the existing WordPress site. There are some exceptions, but they are only places where I
felt a change was for the best.

I have immediately noticed a complete lack of latency on page loads, which I find quite refreshing. Occasionally one of the Javascript chunks blocks the page-load a bit (whether it be Google Analytics, Disqus, the syntax highlighter, or the Twitter widget, I can't say). I am keeping my eye out for this, and hopefully will figure it out with Firebug, and I can squash it. Overall, this has been a minimalist's dream as compared to something chunky like WP.

Before I globally share the blog code, I really wanted to get a few things tidied up (call me vain):

* Right now the administrative reload is a bit of a shim. I'd really like this to be an automated "on-git-update" event, 
so my involvement is simply a matter of avoiding malformed posting, and pushing the content.
* My error handling is pretty weak-sauce right now - I was slapping this together pretty quickly; the majority of my 
time was in converting my blog entries (which I did by hand, like an idiot).
* Right now I'm using Play 1.2.4 - I want to move to 2.0, but unlike Brian, I plan to wait until it's final before 
converting.

Once I get through those hurdles, I'll throw out a follow-up, and hopefully some public mirrors so folks can get some
value (however small) out of my derivative journey. If you have any questions in the mean-time, [send me a note](/contact).
