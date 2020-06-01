---
title: "Why Aren't You Using Fish and Oh-My-Fish?"
date: 2020-05-30
tags: ['shells','fish','osx','linux','oh-my-fish','terminal','iterm']
---
About three years ago, I asked the question [Using OSX? Why Aren't You Using iTerm, ZSH, and Prezto?]({{<ref "prezto.md">}}). In that article I highly suggested a combination of tools to help with development workflows on OSX (though, most of this is equally relevant that those of us that use Linux as well). Today I'm revising this article with a look towards Fish shell instead. Let's take a look.

<!--more-->

The article I wrote nearly three years ago suggested using a variety of tools and configurations:

* Installing [iTerm2](https://www.iterm2.com/) *(OSX Only)*
* Switching to [ZSH Shell](https://www.zsh.org/)
* Installing [Prezto](https://github.com/sorin-ionescu/prezto) or [Oh My ZSH](https://ohmyz.sh/)
* Configuring a bunch of Prezto modules

Since then, my workflow has evolved iteratively. While I still use OSX at work, at home I'm 100% running on KDE Neon and Linux for both my workstation and my laptop. Despite that, I have still been using ZSH.

In the past couple weeks, however, I have moved to using [the Fish shell](https://fishshell.com/), and while the move hasn't been entirely painless, overall the end results have been very positive. Here are a few of the reasons why:

* Fish has a ton of powerful built-ins that are add-ons and extra configuration with something like ZSH
* Scripting for Fish is easier than Bash or ZSH
* Fish is built in a way that makes it [easy to use shared, committed dotfiles](https://www.freecodecamp.org/news/dive-into-dotfiles-part-2-6321b4a73608/)
* There are sophisticated tools and good community support like [Oh My Fish](https://github.com/oh-my-fish/oh-my-fish)
* Overall, Fish is very fast - it starts fast, it stays fast, and it uses minimal memory, especially as compared to a fully decked out `Oh My ZSH` install

{{<figure src="/img/articles/fish/fish.png">}}

## Installing "Fish"

Fish is easy to install out of the box.

* A lot of distros have it available directly out of their package managers.
* On OSX you can use [Homebrew](https://brew.sh/) ala `brew install fish`
* On Ubuntu they have a PPA that can be used:
  ```
  sudo apt-add-repository ppa:fish-shell/release-3
  sudo apt-get update
  sudo apt-get install fish
  ```

Best recommendation is to check the [Fish Github README](https://github.com/fish-shell/fish-shell) for up-to-date instructions.

Once installed, Fish can be made the default shell for your user in different ways based on your OS:

* On Linux you can use `chsh`
* On OSX you can still use `System Preferences > Users and Groups > Unlock > Advanced Options > User Shell`

## Dotfile Structure

* Fish puts all dotfiles under `.config/fish`, with the root file being `.config/fish/config.fish`.
* Additionally, customization per host is captured in a file called `fishd.<hostname>`.
* Individual configurations can be organized into separate files under `conf.d`
* Custom functions can be placed in `functions`
* Custom variables can be placed in `fish_variables`

## Installing "Oh My Fish"

While a new Fish installation is ripe for customization and tweaking, I personally find it best to jump right into the community customization. As with ZSH before it, Fish has a community-managed plugin system called [Oh My Fish](https://github.com/oh-my-fish/oh-my-fish). Like "Oh My ZSH", this system injects itself directly into your fish dotfiles. 

Installing Oh-My-Fish is straightforward:

```
curl -L https://get.oh-my.fish | fish
```

(Again, check the Github README for the latest details)

Oh-My-Fish injects itself at the top of the Fish `config.fish` and creates a separate series of dotfiles. Once installed Oh-My-Fish provides an `omf` command that can be used for customizing. One of the first things I chose to do was pick my theme. Themes can be previewed using Installing themes automatically configures them as the default. The [options available can be browsed here](https://github.com/oh-my-fish/oh-my-fish/blob/master/docs/Themes.md).

I personally am using `bobthefish` via the command: `omf install bobthefish`:

![Bobthefish](https://cloud.githubusercontent.com/assets/53660/18028510/f16f6b2c-6c35-11e6-8eb9-9f23ea3cce2e.gif "Bobthefish")

## Useful Plugins

Fish has a lot of built-in syntax-highlighting and auto-suggestion support out of the box. But there are still some useful plugins I have found.

* [foreign-env](https://github.com/oh-my-fish/plugin-foreign-env) - `omf install foreign-env`: This is super useful for replacing the ZSH `emulate` command as I called out as important in my [Missing Snap Applications in ZSH article]({{<ref "../linux/kde-neon-snap-apps-missing.md">}})
* [extract](https://github.com/oh-my-fish/plugin-extract) - `omf install extract`: A good "multi-format" replacement for the `archive` command in Prezto
* [fasd](https://github.com/oh-my-fish/plugin-fasd) - `omf install fasd`: This adds the aliases that are typically useful for `fasd` similar to the ZSH variant (`a`, `s`, `d`, `f`, `sd`, etc.)

## Sourcing `/etc/profile`

As mentioned in the [Missing Snap Applications in ZSH]({{<ref "../linux/kde-neon-snap-apps-missing.md" >}}) article, KDE Neon and Snap assuming that `/etc/profile` will be loaded for things to work correctly. This would be just as broken with Fish as with ZSH.

Unlike ZSH, however, Fish does not have the `emulate` command available. It does, however, have the `foreign-env` plugin via Oh-My-Fish. With this installed, it's a simple addition to the `conf.d` to source this file on fish load:

### `~/.config/fish/config.fish`

```
## After the Oh-My-Fish inclusion line:
fenv source /etc/profile
fenv source ~/.profile
```

## Aliases as Functions

Aliases are very common for advanced shell use. For example, I regularly use [LSD](https://github.com/Peltoche/lsd) as a replacement for the vanilla `ls` command.

The best way to do this with Fish is, unsurprisingly with the `alias` command. However, it's recommended to put them in the `functions` folder so they can appropriately load lazily and keep shell startup fast. For example:

### `~/.config/fish/functions/ls.fish`

```
alias ls=lsd
```

As indicated in the Fish documentation, Alias is actually just a shorthand for a function that wraps another command: https://fishshell.com/docs/current/cmds/alias.html - This is why we place this in the functions folder.

## A First Real Function: `up`

I had a variety of aliases and functions in my ZSH install. One that I've grown accustomed to using is `up <dircount>`, which moves up a certain number of directories. I've developed a muscle memory for typing `up 3` rather than using a `cd` command. Thankfully, this function is quite easy with fish:

### `~/.config/fish/functions/up.fish`

```
function up
  for count in (seq $argv)
    cd ..
  end
end
```

This function is a naive implementation but works fine:

* `$argv` is the user input
* `seq` turn it into a sequence of numbers
* `count` is a variable for the iteration/number, but is unused
* Each `for` iteration we do a `cd ..` to go up 1 directory

## Concluding Thoughts and Disclaimers

Oh-My-ZSH and Prezto both have huge established communities, and an absurd amount of customizations and tweaks for every flavor. Fish, by comparison is a smaller community.

Similarly, Fish, being more different from Bash than ZSH is, has to do a little more work to function cleanly in a bash-heavy world. Tools like `foreign-env` are increasingly useful when tools like the `gcloud` CLI want to self-install into your dotfiles and they don't natively support Fish.

I have hit a (perhaps totally expected) number of hiccups to get through various hurdles, and hopefully this article will help you have a few less in your workflow if you try Fish.
Overall I've been ecstatic with the general performance, quality of auto-complete and suggestions, and color support that Fish ships out of the box. It feels more polished in general post-install, and I'm h
