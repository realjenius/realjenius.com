---
title: 'Building a Series List with Hugo Shortcodes'
tags: ["hugo"]
date: 2017-08-07
---

Several years ago I developed a couple articles on how to build a series list using Jekyll:

* [Building a Series List with Jekyll Scripts]({{< ref "post/jekyll/jekyll-series-list.md" >}})
* [Building a Series List with a Jekyll Plugin]({{< ref "post/jekyll/jekyll-series-list-2.md" >}})

Today I'm going to revisit the idea using Hugo shortcodes as the solution.

<!--more-->

I have a number of article series on my website including the [Distilling JRuby Series](/tags/distilling-jruby"), and in all of them I have an article list:

{{< figure src="/img/articles/hugo/article_list_sample.JPG" >}}

Maintaining these manually is a headache, and with a static site generation tool like Hugo or Jekyll you'd prefer if it could do the hard work for you.

For Hugo, that solution is definitely shortcodes.

In my first Jekyll article, you might recall that Jekyll supports "Liquid" tags (`{% ... %}`), which serve as a pseudo scripting language that can be embedded directly in your article source. Jekyll also supports the ability to include other files. I used these two concepts together to define a series-list-generation include that used all sorts of inferred context and bound variables to generate output.

In my second Jekyll article, you might recall I created a Jekyll plugin, which is able to create it's own Liquid tag that can take parameters to execute. The plugin was written in Ruby code internally.

The downside of Jekyll was that you either had to:

* Use Liquid tags so you only work in Jekyll site source, but have to hack things together with Liquid tags and use includes to ingest the logic (e.g. `{% include /includes/series.html %}`)
* Know Ruby code to build plugins that could run and generate HTML from Ruby directly (not ideal).

Further, Ruby plugin execution is disabled altogether on Github pages:

> Plugins on GitHub Pages
GitHub Pages is powered by Jekyll. However, all Pages sites are generated using the --safe option to disable custom plugins for security reasons. Unfortunately, this means your plugins won’t work if you’re deploying to GitHub Pages.
>
> You can still use GitHub Pages to publish your site, but you’ll need to convert the site locally and push the generated static files to your GitHub repository instead of the Jekyll source files.

This leaves you with only one option for Github pages hosting.

Hugo has the concept of ["Shortcodes"](https://gohugo.io/content-management/shortcodes/), which are much like "Liquid Tags" in Jekyll. Also like Jekyll, you can create custom shortcode tags. However, the major difference is that in Hugo you can create them without resorting to actually writing Go code - see [Create Your Own Shortcodes](https://gohugo.io/templates/shortcode-templates/). Because Hugo uses Go Templates for rendering the pages, shortcodes can use any and all [Go template functions inside of them](https://golang.org/pkg/text/template/#hdr-Functions), as well as [a whole list of custom Hugo functions added to help](https://gohugo.io/functions/). This makes it arguably more powerful than a liquid-template solution, but still in a template file that can be easily updated on the fly. This is of double benefit since, unlike Ruby, Go is a compiled language, and so deployment of a Go-based plugin might involve additional complex steps for your environment.

To get started with shortcodes, a "shortcodes" folder needs to be created, and a file named for the shortcode embedded in it:

{{< figure src="/img/articles/hugo/shortcodes_folder.JPG" >}}

Any file created here will be invoked every time syntax in the form `{{</* [shortcodename] */>}}` is called. As indicated in the docs, we can support input parameters by position or by name.

Ideally what we'd like as the finished version of the output would be this:


```html
<!-- A shortcode link to render the series list for the current article -->
{{</* series */>}}

<!-- A shortcode to render a series list unrelated to the current article -->
{{</* series distilling-jruby */>}}
```

As with the Jekyll solution, it would nice if the series for the current article could be determined via the YAML front-matter of the article, and that the date of the article would drive the ordering of the series list:

```yaml
title: "Some article"
series: "my-series" # The series for this article
date: 2017-08-07 # order the series by the date values
```

Custom properties are fully supported by Hugo - they are called [Page-level Parameters](http://gohugo.io/variables/page/#page-level-params), so we only need to learn how to access these from the enclosed shortcode, and then how to iterate.

First, if we want to support this concept of an optional input parameter with the default being the page parameters. There are a few ways to achieve this, but one approach is to use the Go template "or" function:

```go
{{ $series := or (.Get 0) $.Page.Params.series }}
```

This creates a variable named `$series` that is either specified as the first parameter (ordinal zero), or if that is not set, gets the parameter names "series" from the enclosing page.

Next, we need to determine how to find all articles in the order we care about. Hugo docs discuss the `.Site` variable being available to traverse page metadata freely. It includes a variety of sequences, including `.Site.Pages`. Every one of these sequences can be organized by different properties automatically, including by date.

If we put this all together with a go range template, we get this:

```go
{{ range $ind,$art := $.Site.Pages.ByDate }}
 ...
{{ end }}
```

In this case, `$art` is the article for each iteration through this range.

We also want to filter any pages not part of our series, so we can filter for articles by using the "eq" function:

```go
{{ if eq $art.Params.series $series }}
 ...
{{ end }}
```

Finally, if we find our own article, we want to omit making the article into a link. One way to do this is to match the permalink for the current page with the permalink for the page over which we are currently iterating, via the "eq" function:

```go
{{ if eq $art.Permalink $.Page.Permalink }}
  // our page
{{ else }}
  // another page
{{end}}
```

Putting it all together with some syntax to make the HTML palatable to view, here is an example `series.html`:

```html
{{ $series := or (.Get 0) $.Page.Params.series }}
<div class="well">
  <h4>Article Series</h4>
  <ol>
    {{ range $ind,$art := $.Site.Pages.ByDate }}
      {{ if eq $art.Params.series $series }}
        <li>
          {{ if eq $art.Permalink $.Page.Permalink}}
            {{ $art.Params.title }}
          {{ else }}
            <a href="{{ $art.Permalink }}">{{ $art.Params.title }}</a>
          {{ end }}
        </li>
      {{ end }}
    {{ end }}
  </ol>
</div>
```
### Bonus Tip

If you need to write an article in Hugo and you need to escape a shortcode so it doesn't attempt to render and confuse the template engine, you use comments (as follows):

{{< figure src="/img/articles/hugo/escape_shortcode.JPG" >}}

That emits this:

```html
{{</* series distilling-jruby */>}}
```
