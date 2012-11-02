---
title: 'What Am I Downloading Today, Eclipse?'
summary: 'As it pertains to system updates, Eclipse has a long way to go in the "inform your users" department.'
tags: java eclipse ides
category: journal
date: 10/08/2009 19:41
legacyId: 379
layout: post
---

With the release of Eclipse 3.5, the plugin installation and update manager was completely revisited once again; the update process was reorganized, and the strengths of the p2 provisioning framework were surfaced. It's nice to be able to hop in, download updates, and go.

However, I think Eclipse, as a product, still has a way to go. There are a number of products I use everyday that frequently require or recommend updates and upgrades. Some good examples include:

* [Mozilla Firefox](http://www.mozilla.com/en-US/firefox/firefox.html)
* [Android and Android Applications](http://www.android.com)
* [WordPress](http://www.wordpress.org)
* Windows
* [Mac OS X](http://www.apple.com/macosx/)
* [Songbird](http://www.getsongbird.com/)
* [Eclipse](http://www.eclipse.org)

...and, of course, the list goes on. All of these apps have different ways to handle notifying and installing these upgrades.

Of course, both Windows and Mac OS X have built in update mechanisms, the former being the ever-controversial "Windows Update" tool, and the latter being Mac's Apple Software Update (which has also recently [been pissing people off](http://arstechnica.com/apple/news/2009/09/apple-pushes-unwanted-enterprise-tool-to-windows-users.ars)). Firefox and Songbird use their own update mechanism that's part of the Gecko platform. Most Mac users are also probably aware of [Sparkle](http://sparkle.andymatuschak.org/) (or at least have seen it in action), as many Mac software distributions (like [TextWrangler](http://www.barebones.com/products/TextWrangler/)) use it as their update mechanism.

Your mileage may vary with the different implementations; some are more informative than others, and some are more reliable than others. But one thing can generally be said: each of these mechanisms tie in with documentation explaining what was updated, corrected, or otherwise tweaked. Some tools, like Sparkle, embed the update information in the notification dialog. Other tools, like the Firefox notifier, provide a link to the content that takes you to a rich and user-friendly webpage.

{% assign caption='Firefox has thorough documentation online' %}
{% assign src='misc/firefox.png' %}
{% include image.html %}

Today when I came in to the office I decided to perform an update check in Eclipse, and was presented with a number of plug-ins that had newer replacements. At the time, I didn't grab a screenshot, but here is an example from a brand-new download of 3.5, which has been since supplanted by 3.5.1:

{% assign caption='So is this a good thing?' %}
{% assign src='eclipse-updates/eclipse_updates.png' %}
{% include image.html %}

Unfortunately, this doesn't show off the obscure list of plug-ins my installation did earlier today, but there is still plenty I can pick on. So, what's wrong with this picture?

* I have no information on what is being changed or improved in this update.
* The 'Details' section is suspiciously empty.
* The version number is unnecessarily cryptic for end users.
* The 'More' button simply gives me the description, copyright, and license for the plugin/feature being shown, which in this case is basically empty boxes.
* The version number is cryptic for an end user.

Now, as an Eclipse-enthusiast, I have no problem deciphering what is going on here (particularly not since I had to contrive this picture for the blog entry), however as an end-user product, this could use some work. More information could be provided, condensing what was actually improved or fixed in this release (any information at all would be nice).

I understand that Eclipse is a platform first, and a product second, however I think this is one area that could use improvement.