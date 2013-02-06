# RealJenius.com Site Source

## Overview

This is the [Jekyll](http://jekyllrb.com/) site source for http://realjenius.com.

Of particular note for lurkers:
* Custom category/tag index and feed generation within [_plugins](https://github.com/realjenius/realjenius.com/tree/master/_plugins).
* A few other custom plugins for generating post urls, and expanding URLs when generating feeds.
* Stylesheets and layout are all [Twitter Bootstrap](twitter.github.com/bootstrap/) based.
* Pygments cache in place to not make builds totally suck.

## On Deployments

Github builds are a branch mirror from my private Git repo. Deploys are automated to the site with a post-receive hook much like that documented here: [Jekyll Post-Receive Git Deployment](https://github.com/mojombo/jekyll/wiki/Deployment).
