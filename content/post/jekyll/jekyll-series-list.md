---
title: "Dirty Tricks: Building an Article Series List With Jekyll"
summary: 'I show how, with Jekyll and Liquid and some clever-ness, you can build a dynamic article series list for sites.'
tags: ["jekyll","ruby","site","hacks"]
date: 2012-11-03
---

Jekyll is one of the most popular "static blogging" tools available right now, and is the foundation for a number of popular tools at a more sophisticated level, including [OctoPress](http://www.octopress.org), and [Jekyll Bootstrap](http://www.jekyllbootstrap.com). Since the end result of a built Jekyll site is plain, vanilla HTML, it allows for fairly complex sites to be built with a bare minimum of hosting requirements, and it's also pretty easy to secure and make perform in the process!

That said, there are some features Jekyll just doesn't have that big dynamic content management systems do; but that doesn't mean they can't be built. For the more sophisticated enhancements, you will likely need to look at implementing custom Jekyll plugins, tags, and filters -- but for some features, you can get away with wrangling [Liquid](https://github.com/Shopify/liquid) scripts into the shape you need. Liquid is a templating engine, and like many templating engines, it has a little bit of programming support mixed in with its ability to generate dynamic markup, and sometimes you can leverage that to hit your goal.

One of the features that RealJenius.com has is a "series" list - you can see this on my [Distilling JRuby](/tags/distilling-jruby) articles:

{{< figure src="/img/articles/jekyll/series_block.png" caption="An article count, an ordered list, and links to all neighbors." >}}

Obviously, if you're running a site with server code behind it, you can simply fetch by an index in the database, or possibly iterate over relevant entries in memory, or something similar when you're rendering the blog entry, and easily generate something like this. But if you're generating a blog at build time, how can you fill this in?

It turns out it's not all that different in Jekyll. One of the things Jekyll makes available at the global level when performing blog generation is a list of all posts, ordered by their post time in descending order, and with those you can get all sorts of data about the post. From that, I imagine you can probably see how to do this with a plugin to Jekyll; just:

* Iterate the post list with some Ruby code
* Pluck out the right entries
* Sort them
* ... and render some HTML

But let's say we want to take the role of "site designer", and not write any Ruby code. How can we translate this raw list of posts into a series list, with all of the information above?

First off we need to identify the posts. When you write a post in Jekyll, you have to fill in the YAML front-matter. It's just a block of YAML leading the entry that has property-like details for the post, like the title, the page layout to use, etc. You can add any custom fields you want as well. So let's tag our articles as belonging to a particular series:

```yaml
---
title: The Fish of the United States
layout: post
series: fish-series
---
{% endhighlight %}

Next, we need to create a reusable chunk of Liquid+HTML for the logic. We can simply use an include in the `_includes` directory for this:

**`_includes/series.html`**:

```html
<div class="seriesNote">
<!-- A series block will go here -->
</div>
```

Now, in our article, we can simply include the series like this:

```
Welcome to my article about Fish in the United States. This is the first entry in a series about fish throughout the world!

{% raw %}
{% include series.html %}
{% endraw %}

Obesity rates in the Fish in US have hit epidemic proportions...
```

Finally, we just need to implement our series block. There are a number of things we need to render the example above:

* A count of all of the articles.
* An index for the current article.
* A list of posts in chronological order for the series.
* The URL for each post.

Here is a big wall of hack-y liquid tags to achieve just that.

```html
{% raw %}
{% assign count = '0' %}
{% assign idx = '0' %}
{% for post in site.posts reversed %}
	{% if post.series == series %}
		{% capture count %}{{ count | plus: '1' }}{% endcapture %}
		{% if post.url == page.url %}
			{% capture idx %}{{count}}{% endcapture %}
		{% endif %}
	{% endif %}
{% endfor %}

<div class="seriesNote">
	<p>This article is <strong>Part {{ idx }}</strong> in a <strong>{{ count }}-Part</strong> Series.</p>
	<ul>
	{% assign count = '0' %}
	{% for post in site.posts reversed %}
	{% if post.series == series %}
		{% capture count %}{{ count | plus: '1' }}{% endcapture %}
		<li>Part {{ count }} -
		{% if page.url == post.url %}
			This Article
		{% else %}
			<a href="{{post.url}}">{{post.title}}</a>
		{% endif %}
		</li>
	{% endif %}
	{% endfor %}
	</ul>
</div>

{% assign count = nil %}
{% assign idx = nil %}
{% endraw %}
```

*You can download or fork this source directly from [Github](https://github.com/realjenius/site-samples/blob/master/2012-11-03-jekyll-series-list/series.html).*

Let's work through this. The script is roughly broken into three parts:

1. The first part iterates over the total post list counting all entries that match the series key, and also finds the index of the post.
2. The second part generates the series summary text, and then iterates over the total post list again, generating links for each post.
3. Finally will clear out any variables we used since they all float around in a global namespace.

A few interesting things to note:

* Properly incrementing and tracking a count when you're doing filtering in a for loop is pretty nasty in liquid. This is done by using the "plus" filter and recapturing the value: `{% raw %}{% capture count %}{{ count | plus: '1' }}{% endcapture %}{% endraw %}`. This is effectively like saying `count = count + 1`.
* We use the special `for` loop keyword `reversed` because the posts in Liquid are reverse-chronological, and we want to list them in true chronological order.
* We have to iterate twice because the series summary text comes before the actual list, and there aren't any liquid constructs (that I know of) for creating a new array off of the first iteration.

And that's it! Could this be done cleaner and probably easier via some judicious use of Jekyll plugins? Absolutely! And what would be the fun of that?
