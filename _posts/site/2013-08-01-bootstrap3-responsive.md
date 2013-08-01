---
title: 'Bootstrap 3 Responsive Tools Primer'
summary: 'A quick primer for some of the Bootstrap 3 responsive tools as I used them on my site.'
tags:
- bootstrap
- site
- css
category: journal
layout: post
date: '2013-08-01 17:54:00'
---

Yesterday, I talked some about how Bootstrap 3 has changed and how that might impact you as you explore migrating. Today I want to talk very briefly about some of the experiences I've had with the revision to my layout on my site.

### Go With the Flow

In most cases, when you set out to make a gridded, responsive layout with Bootstrap, you are rewarded with a very natural collapsing of the layout that flows well into a reduced structure. For example, let's consider an individual "post row" on my site:

{% assign caption='A simple post row.' %}
{% assign src='/bootstrap3/postrowdesktop.png' %}
{% include image.html %}

The way this is laid out is actually like this:

	+-------------------------+
	|         TITLE           |
	+-------------------------+
	| DATE | SUMMARY | MORE-> |
	+-------------------------+

If we collapse it for mobile, this is what we're presented with:

{% assign caption='Stack-em up.' %}
{% assign src='/bootstrap3/postrowmobile.png' %}
{% include image.html %}

Which, as you can probably guess, lays out like this:

	+---------+
	| TITLE   |
	+---------+
	| DATE    |
	+---------+
	| SUMMARY |
	+---------+
	| MORE->  |
	+---------+

It's "tall", but it's making the most effective use of the space without removing any elements, and overall, I'm fairly happy with the end result. But what if this doesn't work? Well, you have a few options:

* **Hide It** - If it's an optional element, you can just get rid of it at certain media levels. I'll show this below.
* **Swap It** - Similarly, if it is better served in a different construct, you can deliver both, but flip which is visible based on different viewport size levels.
* **Re-order It** - Bootstrap has a (somewhat poorly documented) feature called [Column Ordering](http://getbootstrap.com/css/#grid-column-ordering) that may not be totally obvious at initial grasp, but allows you to adjust the order of elements when they share a column, but not when they get wrapped onto separate rows.

### Hiding in Practice

So I mentioned that my site uses hiding. I actually use it in two places, and you can see it in action simply by resizing your desktop browser.

The first is the big blue "more" buttons on the home page. If you look, you'll see that I have a well-aligned row of buttons under the three most recent posts:

{% assign caption='Big Blue Buttons' %}
{% assign src='/bootstrap3/desktoparticles.png' %}
{% include image.html %}

Unfortunately, to ensure that the more buttons share a visual row, I'm using the poor man's way out, and simply using separate rows in Bootstrap:

	+-----------------------------------+
	| ARTICLE 1 | ARTICLE 2 | ARTICLE 3 |
	+-----------------------------------+
	|  MORE 1   |  MORE 2   |  MORE 3   |
	+-----------------------------------+

This looks great, and since Bootstrap always uses consistent span sizes, it's no layout issue whatsover. However, when we get into re-flowing this for mobile, we'll quickly see the problem:

	+-----------+
	| ARTICLE 1 |
	+-----------+
	| ARTICLE 2 |
	+-----------+
	| ARTICLE 3 |
	+-----------+
	|  MORE 1   |
	+-----------+
	|  MORE 2   |
	+-----------+
	|  MORE 3   |
	+-----------+

Whoops! Now we have three stacked more buttons completely out of place!

_(Incidentally the equal height columns in CSS is a notorious problem, and has many potential "solutions", none of which I really wanted to deal with. [Here are a few examples](http://www.vanseodesign.com/css/equal-height-columns/))._

In my case those buttons add nothing unique (article navigation is delivered via title), and in fact take up a ton of space on the mobile layout that is entirely unnecessary. So all I do in this case is simply disable them from showing unless you're on a desktop-worthy screen size.

{% highlight html %}
<div class="col-lg-4 visible-lg">
	<a href="..." class="btn btn-large btn-block btn-primary">MORE &arr;</a>
</div>
{% endhighlight %}

{% assign caption='Bye-Bye Buttons' %}
{% assign src='/bootstrap3/mobilearticles.png' %}
{% include image.html %}

Now, you could just as easily have another element that was "visible-sm", and only showed up when on mobile, but since the title is already clickable, it seemed unnecessary to me.

The other part of my layout that is only visible on desktop is the large majority of the navigation. In fact, much of the navigation is largely reproduced on other pages anyway (like my about page) and things like atom feed links are far less likely to be useful on a mobile device; so they are left out.

{% assign caption='Lots of links' %}
{% assign src='/bootstrap3/desktop-nav.png' %}
{% include image.html %}

{% assign caption='Only what is necessary.' %}
{% assign src='/bootstrap3/mobile-nav.png' %}
{% include image.html %}

### Re-Order 

I don't have a particular case that shows off the column reordering, but you can certainly experience it via the Bootstrap documentation linked to above. My example with the buttons would not be solved by this since they had to be in different rows on the *desktop* to get the ensured "clearfix" benefits of a new row.

### Summary

I've only scratched the surface with Bootstrap 3's responsive layouts, and am certainly not digging into particularly elegant designs; simply hacking at mine until it works. That said, the amount of "work" on my part to get a good mobile layout was actually quite minimal.
