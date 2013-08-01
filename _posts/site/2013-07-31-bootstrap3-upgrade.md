---
title: 'Bootstrap 3 Upgrade: What I Learned'
summary: 'I upgraded my site to Bootstrap 3 and along the way learned a few things about the differences.'
tags:
- bootstrap
- site
- css
category: journal
layout: post
---

Since the [Bootstrap folks](http://getbootstrap.com/) have decided to [promote Bootstrap 3 RC1 as their default version](http://blog.getbootstrap.com/2013/07/27/bootstrap-3-rc1/), I decided to use it to upgrade my site. Along the way I chose to finally fix the responsive layout issues I've had since, supposedly, Bootstrap 3 does the whole responsive thing far better than Bootstrap 2 did.

This [pull request](https://github.com/twbs/bootstrap/pull/6342) contains a good summary of the changes, so I won't renumerate them blindly here; but be sure to check them out. I will review some things to keep in mind, however.

### Grid System

The grid system has been changed completely. The class names are totally different, and they behave differently now too; especially when considering responsiveness. You really should consider revisiting all of your site scaffolding, as directly porting over may not do as you expect.

That said, if you were using ~unresponsive~ Bootstrap, the closest migration is to use `col-# col-lg-#` everywhere you previously had `span#`. This tells Bootstrap to use the same gridding for both the smallest and largest devices.

I fought the new model for a while, and I suspect you might too, but once you can understand it, it can be quite powerful.

### Button Defaults

Aside from the obvious color changes, buttons no longer fallback to default styling when left as just "btn". You need to now say "btn-default" if you have nothing else. Yes, this will probably require Regex to find on your site everywhere.

### Other Responsive Details

There is now a whole series of "responsive" utilty classes that allow you to show/hide different page elements depending on certain page layouts. This is useful for those cases where the natural responsive re-flow is just not cutting it. Examples include `visible-sm` (only show this element on the smallest container model), and `hidden-lg` (show this on all but the largest/desktop container model).

### Form Changes

The form system now uses 100% of the parent container. This is a significant change from Bootstrap 2, and will impact you in a non-trivial way. Most forms will have to be revisited entirely. For example, I had several forms that used `help-inline`. That's now gone!

### Summary

Overall I think that Bootstrap 3 is a move in the right direction for the framework. I do think that it will be a painful transition for many, however. My site is extremely simple on the public side, and it still took quite a while to make the move. More complex sites have a non-negligible migration path in front of them. Like the move from Bootstrap 1 to Bootstrap 2, you need to be prepared for some pain!