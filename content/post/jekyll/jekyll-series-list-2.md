---
title: "Clean it Up: Article Series List With Jekyll, Part 2"
summary: 'I previously showed how you could build an article series list with Jekyll using Liquid. This time I use a custom Liquid tag instead of a Liquid script-let.'
tags: ["jekyll","ruby"]
date: 2012-11-04
---

Previously I showed how you could [build an article series list with Jekyll]({{< ref "/post/jekyll/jekyll-series-list.md" >}}) by scrapping together some Liquid scriptlets and some clever looping. The implementation certainly works, but it's a little bit ugly, inefficient, and hard to maintain. The main goal was to see how far we could stress it using only Liquid.

This time I'd like to show how you could achieve the same by actually implementing a proper Liquid tag; implementing some Ruby code to achieve the same goal.

There are a lot of ways you can extend Jekyll, including custom generators, filters, and converters. Another way is to add custom tags to Liquid, since it's the foundation for Jekyll. In fact, Jekyll adds a couple out of the box, like the `include` and `highlight` tags.

We'll create a new tag called `series`, and the end result would be that we can simply put it in our article like this:

```
Welcome to my article about Fish in the United States. This is the first entry in a series about fish throughout the world!

{% raw %}
{% series_list %}
{% endraw %}

Obesity rates in the Fish in US have hit epidemic proportions...
```

In practice, not a whole lot different than what we had in part one, but the code will be oh-so-much-more-rewarding. So how do we get there?

First, we need to implement a new Jekyll tag - we'll start by getting all of the declaration boiler-plate out of the way:

```ruby
module Jekyll

  class SeriesTag < Liquid::Tag
    def initialize(tag_name, params, tokens)
      super
    end

    def render(context)
      ""
    end
  end
end

Liquid::Template.register_tag('series_list', Jekyll::SeriesTag)
```

We can put this new class definition in our Jekyll `_plugins` folder as `_plugins/series_tag.rb`. In reality, you can put any Ruby code in the plugins folder that you want to load on Jekyll page-generation time; in practice it's going to extend Jekyll in some way, or there probably isn't much point to having it.

All we're doing here is creating a new `Liquid::Tag`, and registering it in Liquid as `series_list`. The render method is expected to generate replacement markup that will be processed by the next stages of the generation process (markdown/textile, and then final HTML output).

Now we need to figure out how to get a handle on a few things:

* Our current page so we know which post we're currently rendering.
* The series we're supposed to be rendering.
* All posts for the site, so we can generate the list.

Here's what that looks like:

```ruby
site = context.registers[:site]
page_data = context.environments.first["page"]
series_name = page_data['series']
if !series_name
	puts "Unable to find series name for page: #{page.title}"
    return "<!-- Error with series tag -->"
end
```

We get the site object out of the "context.registers" hash. The page is more cryptic - in this case we're collecting the page data out of the environments, and then simply fetch the series off of it. There isn't a lot of documentation about what is available where, but there are some good examples on the web already, and you always have access to the Jekyll source!

We also do a simple series value-set check, and fail fast if it's not there.

Next, we need to get our filtered list of posts, and make sure they're ordered as we need:

```ruby
all_entries = []
  site.posts.each do |p|
    if p.data['series'] == series_name
      all_entries << p
    end
  end

  all_entries.sort_by { |p| p.date.to_f }

```

Now that we have the subset of posts as well as our current post, it's really just a matter of looping and rendering a bunch of HTML:

```ruby
text = "<div class='seriesNote'>"
  list = "<ul>"
  all_entries.each_with_index do |post, idx|
    list += "<li><strong>Part #{idx+1}</strong> - "
    if post.data['title'] == page_data['title']
      list += "This Article"
      text += "<p>This article is <strong>Part #{idx+1}</strong> in a <strong>#{all_entries.size}-Part</strong> Series.</p>"
    else
      list += "<a href='#{post.url}'>#{post.data['title']}</a>"
    end
    list += "</li>"
  end
  text += list += "</ul></div>"
```

And that's it! We now have our finished HTML generating series list, fresh from Ruby code instead of a bunch of Liquid scripts.

*You can get the entire tag by forking or downloading from [Github](http://www.github.com/realjenius/site-samples/blob/master/2012-11-04-jekyll-series-list-2/series_tag.rb).*
