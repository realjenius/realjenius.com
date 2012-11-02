---
title: 'JRuby and Sinatra in 2 Minutes'
summary: A lightning fast walkthrough of using the Sinatra micro-web-framework with JRuby as the platform.
tags: jruby bundler sinatra glassfish ruby
category: journal
legacydate: 7/17/2010 11:53:00
legacyId: 591
layout: post
---

While at [RubyMidwest](http://www.rubymidwest.com) I decided to explore [Sinatra](http://www.sinatrarb.com/) in more detail. I've spent a lot of time with [Rails](http://www.rubyonrails.org), and while I love it, there is something alluring about the simplicity of Sinatra (and, well... ooh shiny). Being a recovering Java developer (Hi, I'm R.J., and I haven't developed in Java for 18 hours) I have a server that runs Java, and would like to be able to use Sinatra to build my fancy-awesome web-apps. On those lines, I want all of the shiny benefits of [JRuby's](http://www.jruby.org) multi-threading awesome-ness, as opposed to just trying to use WEBrick, which does not a powerful server make. So here is a 2 minute tutorial (well, depending on the performance of your computer, and how fast you type) startup with [Sinatra](http://www.sinatrarb.com), [JRuby](http://www.jruby.org), [Bundler](http://www.gembundler.com), and [Glassfish](http://wiki.glassfish.java.net/Wiki.jsp?page=JRuby)

I'm cheating already by assuming you already have JRuby installed as your default Ruby installation. No? [Go get it](http://www.jruby.org)!

Next step is to get bundler:

{% highlight bash %}
gem install bundler
{% endhighlight %}

Now we need to make a home for our application, and prep it for Bundler:

{% highlight bash %}
mkdir testapp
cd testapp
edit Gemfile
{% endhighlight %}

Here I'm creating a new file in testapp called 'Gemfile' in your favorite editor. This is where we will sketch out our dependencies for Bundler to do all the hard work for us - here are the contents for this example:

{% highlight ruby %}
source :rubygems
gem "sinatra"
gem "glassfish"
{% endhighlight %}

Frankly, that's it. We tell Bundler to look for gems in RubyGems core repo, and then we ask it to make sure we have Sinatra and Glassfish. Now we can create the program - create the file 'hello.rb', and use these contents:

{% highlight ruby %}
require "rubygems"
require "bundler"
Bundler.setup

require "sinatra"

get '/hi' do
	"Hello World!"
end
{% endhighlight %}

So what's special for JRuby? Absolutely nothing. We do have special sauce for Bundler, (by calling Bundler.setup prior to the require for 'sinatra') but trust me - you'll be happy you used it. You'll also make [@wycats](http://twitter.com/wycats) happy.

And - that's it! Now, if you were to start this file the standard (well, bundler-standard) way, we'll see this:

{% highlight bash %}
realjenius$ bundle exec hello.rb
== Sinatra/1.0 has taken the stage on 4567 for development with backup from WEBrick
[2010-07-17 11:24:46] INFO  WEBrick 1.3.1
[2010-07-17 11:24:46] INFO  ruby 1.8.7 (2010-06-06) [java]
[2010-07-17 11:24:46] INFO  WEBrick::HTTPServer#start: pid=44490 port=4567
{% endhighlight %}

...and we can visit this URL: [http://localhost:4567/hi](http://localhost:4567/hi). But, recall that our goal was to work with Glassfish, not WEBrick. All that has to change (and for folks who has done Glassfish/Rails before, this won't be a surprise) is to run this startup instead

{% highlight bash %}
realjenius$ bundle exec glassfish
Log file /Users/realjenius/Projects/testapp/log/development.log does not exist. Creating a new one...
Starting GlassFish server at: 0.0.0.0:3000 in development environment...
Writing log messages to: /Users/realjenius/Projects/testapp/log/development.log.
Press Ctrl+C to stop.

Running sinatra
{% endhighlight %}

This time, we'll visit this URL: [http://localhost:3000/hi](http://localhost:3000/hi), and if all worked as desired, Sinatra will be crooning away. Boom goes the dynamite.