---
title: "Why Aren't You Using Fish and Oh-My-Fish?""
date: 2020-05-30
tags: ['shells','fish','osx','linux','oh-my-fish','terminal','iterm']
draft: true
---
About three years ago, I asked the question [Using OSX? Why Aren't You Using iTerm, ZSH, and Prezto?]({{<ref "prezto.md">}}). In that article I highly suggested a combination of tools to help with development workflows on OSX (though, most of this is equally relevant that those of us that use Linux as well). Today I'm revising this article with a look towards Fish shell instead!

<!--more-->

The article I wrote nearly three years ago suggested using a variety of tools and configurations:

* Installing [iTerm2](https://www.iterm2.com/) *(OSX Only)*
* Switching to [ZSH Shell](https://www.zsh.org/)
* Installing [Prezto](https://github.com/sorin-ionescu/prezto) or [Oh My ZSH](https://ohmyz.sh/)
* Configuring a bunch of Prezto modules

Since then, my workflow has evolved iteratively. While I still use OSX at work, at home I'm 100% running on KDE Neon and Linux for both my workstation and my laptop. Despite that, I have still been using ZSH.

In the past couple weeks, however, I have moved to using Fish shell, and while the move hasn't been entirely painless, overall the end results have been very positive. Let's take a look at some of the tips for this setup.

Useful Plugins

Fish has a lot of built-in syntax-highlighting and auto-suggestion support out of the box. But there are still some useful plugins I have found.

* [foreign-env](https://github.com/oh-my-fish/plugin-foreign-env) - `omf install foreign-env`: This is super useful for replacing the ZSH `emulate` command as I called out as important in my [Missing Snap Applications in ZSH article]({{<ref "../linux/kde-neon-snap-apps-missing.md">}})
* [extract](https://github.com/oh-my-fish/plugin-extract) - `omf install extract`: A good "multi-format" replacement for the `archive` command in Prezto
* [fasd](https://github.com/oh-my-fish/plugin-fasd) - `omf install fasd`: This adds the aliases that are typically useful for `fasd` similar to the ZSH variant (`a`, `s`, `d`, `f`, `sd`, etc.)

    docker - Adds a ton of aliases and functions for working with Docker
    fasd - Creates a hotkey for switching the active directory based on most-recent/most-frequent semantics
    git - Adds a ton of aliases and functions for working with Git.
    homebrew - Adds a ton of aliases and functions for working with Homebrew
    node - Adds a ton of aliases and functions for working with Node
    osx - Adds a few commands for triggering behavior that is OSX specific, like working with the clipboard and interacting with the Finder.
    rsync - Adds a few aliases for the most common rsync actions
    ruby - Adds aliases and functions for common ruby work
    screen - If you use screen on remote systems, this is a series of useful aliases for working with it
    syntax-highlighting - Enables zsh-syntax-highlighting
