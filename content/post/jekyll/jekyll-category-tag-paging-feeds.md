---
title: 'Jekyll: Category and Tag Paging and Feeds'
summary: 'I show how with some Jekyll plugin work you can create full fledged category and tag homepages for your site in Jekyll'
tags: ["jekyll","ruby","site"]
date: 2012-12-01
---

[Jekyll](http://www.jekyllrb.com) is a very popular and very powerful static blog generator. Out of the box it's able to generate sophisticated site structures, and has a ton of configurability. One of the areas where I feel that Jekyll lacks some sophistication is around the handling of categories and tags; these are two data-sets that are core to Jekyll, but there isn't a lot of functionality actually built around them. This is in contrast to dynamic blogging platforms like [WordPress](http://www.wordpress.com), or possibly [Drupal](http://www.drupal.org), where these two data points are used to drive a lot of central navigation for the site.

To be fair, Jekyll is really intended to be a framework for expansion into larger degrees of customization and sophistication, and thankfully it has a very powerful plugin model. Higher-level frameworks like [Octopress](http://www.octopress.org) and [Jekyll Bootstrap](http://www.jekyllbootstrap.com) have shown what you can do with a little extra tweaking - as have the long list of [Jekyll plugins](https://github.com/mojombo/jekyll/wiki/Plugins).

When I set out to move my site over to Jekyll, one of my key goals was to still support all of the key navigation my site was capable of with my custom platform code, and Wordpress before it. That pretty much amounts to:

* A date descending paging root for all blog entries `/index.html`.
* A matching Atom feed for the root index.
* Static pages like `/about.html` and `/contact.html`
* Individual blog pages (I suppose this one is obvious).
* Date desceding paging indexes for all categories and tags I use (for example: `/category/article/` and `/tag/jruby/`.
* Matching atom feeds for each of the paging indexes above (for example: `/category/article/atom.xml` and `/tag/jruby/atom.xml`.

It was the last two where I hit a hurdle when converting over. Jekyll simply doesn't have built-in support for this. What surprised me was that I couldn't find an open source plugin that did it either. The closest I found was over at [marran.com](http://www.marran.com), where Keith Marran [built category pagination](http://www.marran.com/tech/category-pagination-in-jekyll/) using a Jekyll plugin. It wasn't exactly what I was looking for, but by far the closest - so props to the author for leading me down the path to understanding a little better.

So long story short, I chose to build my own - and it turns out to be quite simple to do, with a significant amount of added navigation as the end result.

Let's recap a bit what we want to achieve:
* A page in the form `/category/[category name]/index.html` for every category, with a Jekyll style `paginator` available to the page at render time.
* Subsequent pages in the form `/category/[category name]/pageN/index.html` for every extra page of entries for that category, again with a `paginator` available at render time.
* A top-ten feed list for the category at `/category/[category name]/atom.xml`.
* Similar support for every tag via `/tag/[tag name]/index.html` and `/tag/[tag name]/pageN/index.html`.

The first step to generating new pages that Jekyll will output is to extend the Jekyll `Generator` class. We can do this in the Jekyll module so we have access to all of the context we need:

```ruby
module Jekyll
  class CatsAndTags < Generator
    def generate(site)
      # Generate pages here!
    end
  end
end
```

Next, we simply need to orient ourselves and actually iterate over what we want to generate. Turns out that's quite easy - the passed in `site` object has everything we need:

```ruby
def generate(site)
  site.categories.each do |category|
    build_subpages(site, "category", category)
  end

  site.tags.each do |tag|
    build_subpages(site, "tag", tag)
  end
end

# Do the actual generation.
def build_subpages(site, type, posts)
  posts[1] = posts[1].sort_by { |p| -p.date.to_f }     
  atomize(site, type, posts)
  paginate(site, type, posts)
end

def atomize(site, type, posts)
  # TODO
end

def paginate(site, type, posts)
  # TODO
end
```

So here you can see we're iterating over all of the sites categories and tags, and for each one calling the new method `build_subpages`. What the site actually has on it for each category and tag is a two-position array, with position 0 being the category or tag name, and position 1 being the posts associated with that category or tag.

Now we can narrow down into what the subpage generation actually looks like - as you can see we have three main things we do: sort the pages by descending date, call `atomize`, and then call `paginate`.

Creating the atom pages is a little bit easier, so we'll start there:

```ruby
def atomize(site, type, posts)
  path = "/#{type}/#{posts[0]}"
  atom = AtomPage.new(site, site.source, path, type, posts[0], posts[1])
  site.pages << atom
end

class AtomPage < Page
  def initialize(site, base, dir, type, val, posts)
    @site = site
    @base = base
    @dir = dir
    @name = 'atom.xml'

    self.process(@name)
    self.read_yaml(File.join(base, '_layouts'), "group_atom.xml")
    self.data[type] = val
    self.data["grouptype"] = type
    self.data["posts"] = posts[0..9]
  end
end
```

Let's break this down:
* We generate a path for the new page based on the passed in type, and position-0 of the posts array. This will generate our `/category/[cat name]` and `/tag/[tag name]` as desired.
* We create a new `AtomPage`, which is a custom class we'll get to momentarily.
* We add the new atom page to the site's `pages` collection.

The custom atom page we have created has a few specific goals. Since it's not backed by an actual file (like most Jekyll pages would be), we need to mix and match pieces and parts to generate our page into existence:
* The file name is hard-coded (if you were to use/extend this you may wish to change this or pull it from the site config)
* The YAML front-matter is read from a layout file we expect to find at `_layouts/group_atom.xml` (again, the location of this atom layout could be something that comes from the site config if you desire).
* We bind the tag or category name (`val`) to the page's data hash, so the page source can know what it actually represents.
* We bind the type of group we're dealing with ("category" or "tag") to the `grouptype` data element.
* We bind the actual list of posts we want to render to the output - in this case the first 10 items in the list. How many this actually renders could also come from the site configuration if desired.

Now we just need to look at the actual atom layout page itself - `group_atom.xml`:

```xml
{% raw %}
---
title: nil
---
<?xml version="1.0" encoding="UTF-8" ?>

<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
   {% if page.grouptype == 'tag' %}
   	<title>RealJenius.com - Tag: {{page.tag}}</title>
   {% elsif page.grouptype == 'category' %}
    <title>RealJenius.com - Category: {{page.category}}</title>
   {% else %}
    <title>RealJenius.com</title>
   {% endif %}
   <link>http://realjenius.com</link>
   <description>I'm a software developer in the game industry, and have been (for better or worse) coding on the Java platform for the last decade. I also do all my own stunts.</description>
   <language>en-us</language>
   <managingEditor>R.J. Lorimer</managingEditor>
   <atom:link href="rss" rel="self" type="application/rss+xml" />

    {% for post in page.posts %}
	  <item>
        <title>{{ post.title }}</title>
        <link>http://realjenius.com{{ post.url }}</link>
		<author>R.J. Lorimer</author>
		<pubDate>{{ post.date | date_to_xmlschema }}</pubDate>
		<guid>http://realjenius.com{{ post.url }}</guid>
		<description><![CDATA[
		   {{ post.content | expand_urls : site.url }}
		]]></description>
	  </item>
    {% endfor %}
  </channel>
</rss>
{% endraw %}
```

*(Yes, I know this is RSS 2.0 and not Atom 1.0 - so sue me. I plan to fix it some day!)*

In this case, all we do is layout the main feed layout, and then iterate over the posts attached to the data set and print out item content for each.

Note that the title is switched based on the value of the `page.grouptype` element.

*Note: This snippet uses the `expand_urls` Liquid filter, which is a custom filter to make URLs absolute in the body of the post since feeds are not rendered on your site, so relative links won't work. I'm not going to go into detail about it here, but it's available in my site source, and also available in a varied form in the Octopress platform.*

So that's it for generating our feeds for every category and tag - not too tricky! Next, we need to look at how we do the paged indexes in the `paginate` method above:

```ruby
def paginate(site, type, posts)
  pages = Pager.calculate_pages(posts[1], site.config['paginate'].to_i)
  (1..pages).each do |num_page|
    pager = Pager.new(site.config, num_page, posts[1], pages)
    path = "/#{type}/#{posts[0]}"
    if num_page > 1
      path = path + "/page#{num_page}"
    end
    newpage = GroupSubPage.new(site, site.source, path, type, posts[0])
    newpage.pager = pager
    site.pages << newpage

  end
end

class GroupSubPage < Page
	def initialize(site, base, dir, type, val)
	  @site = site
	  @base = base
	  @dir = dir
	  @name = 'index.html'

	  self.process(@name)
	  self.read_yaml(File.join(base, '_layouts'), "group_index.html")
	  self.data[type] = val
	end
end
```

This is borrowed closely from Keith Marran's plugin. Basically it does this:
* Ask the paging system to count our pages based on the core pagination configuration value.
* For each page build a new `Pager` object and calculate a base path.
* For all pages but the base page, append an additional element to the path so we get that `pageN` in the URL.
* All sub-pages will be called "index.html", and will use the file in the `_layouts` folder with the name `group_index.html` (look familiar to the atom stuff?? It should!)

I won't bore you with a full wall of HTML for my article list, but here are the relevant bits for doing the output in `group_index.html`.

```html
{% raw %}
{% if page.grouptype == 'tag' %}
 <!-- Print tag title here -->
{% elsif page.grouptype == 'category' %}
 <!-- Print category title here -->
{% endif %}

{% for post in paginator.posts %}
  <!-- Print post entry short stuff -->
{% endfor %}

{% if paginator.next_page %}
  <!-- Render next page link -->
{% endif %}

{% if paginator.previous_page %}
  <!-- Render previous page link -->
{% endif %}

{% endraw %}
```

If you're familiar with doing a normal paginated index in Jekyll this should look pretty familiar; all of the mechanics are identical at the Liquid+HTML level.

That's about it for the custom page generation. Not too bad!

If you'd like to see the implementation I use on my actual size, you can see it over on GitHub here: [RealJenius.com - cat_and_tag_generator.rb](https://github.com/realjenius/realjenius.com/blob/master/_plugins/cat_and_tag_generator.rb).
