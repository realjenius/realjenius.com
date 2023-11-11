---
title: 'Multiple JDKs and NixOS'
summary: 'Handling Multiple JDK installations on NixOS without Suffering'
tags: ["nixos", "java"]
date: 2023-11-10
draft: true
---

I've been using NixOS for several months, and overall have found it to be an excellent dev platform, but one of the first challenges I had that gave me trouble was having access to multiple JDKs for Intellij, Gradle, and the rest of my Java workflow.

<!--more-->

## The Binary Path Problem
 
The feats that NixOS pulls off with respect to portable versioned binaries are pretty remarkable, and the benefits that come from a fully declarative OS setup are hard to fully illustrate. However, there are some downsides.

Most Java developers are probably familiar with the various JDK management tools that have grown up over the last few years to cope with a variety of Java versions. Tools like Jabba, JEnv, and especially for desktop environments, [SDKMan!](https://sdkman.io/). Most of these tools help with "environment" management, but additionally, many of them have support for auto-downloading and installing JDKs for your operating system.

Unfortunately, that way lies trouble for NixOS. If you were to install SDKMan, and ask it to install a JDK, it will work fine. However, if you try to run commands, such as `java`, `javac`, or similar, you will be confronted with a cryptic and counterintuitive error, most likely:

```
$ cd <jdk bin dir>
$ ls
java javac ... [and all the others]
$ ./java -version
No such file or directory
```

Despite the file *clearly* being there, your shell says that there is no such file or directory.

What is actually happening here is that the Java binary is assuming a certain standard format for the Linux OS pathing for other binaries for which it is dependent, and is failing with that error due to a dependency missing. To learn more about this, you can read about [NixOS and pre-packaged Binaries](https://nixos.wiki/wiki/Packaging/Binaries).

> Downloading and attempting to run a binary on NixOS will almost never work. This is due to hard-coded paths in the executable. 

This is one of the major downsides of the NixOS model: proprietary binaries or binaries that are not built from source specifically for NixOS will likely not work. For most other Linux distributions, this is hardly a problem, so long as they follow fairly standard global library pathing standards for all the things required. As a result, SDKMan can make many-a-Linux user happy by installing "portable" pre-built SDK binaries, but NixOS users will have no such luck.

The linked article actually shows how you can use `patchelf` to fix hardcoded paths for binaries, but honestly that's not a reasonably effective solution given all of the binaries at play with a single Java SDK.

## Multiple JDK Access

{{< alert >}}
Note: I do not currently use Nix Flakes, so advice here is based on using NixOS and Home Manager (though the advice is the same even if you don't use home manager at all).

With Flakes, there are a few more knobs available to developers, and if I migrate to flakes, I may amend this article to include those details.
{{< /alert >}}

The NixOS package does [ship a variety of builds of `OpenJDK`](https://search.nixos.org/packages?channel=unstable&from=0&size=50&sort=relevance&type=packages&query=jdk), including (as of the time of this writing) `jdk8`, `jdk11`, `jdk17`, `jdk20`, and `jdk21`.

While that isn't all JDKs (nor the variety of vendors) shipped by SDKMan, it's enough to cover a majority of developer use-cases. However, if you naively try to install all of these, you will quickly get an error. For example, if you had something like this:

```
{ config, pkgs, ... }:

{
  # ...
  home.packages = with pkgs; [
      # ...
      jdk11
      jdk17
      jdk21
    ];
  };
  # ...
}
```

The error will look like this:

```
> error: collision between `/nix/store/[...]-openjdk-11.0.19+7/include/jni_md.h' 
  and `/nix/store/[...]-openjdk-21+35/include/jni_md.h'
```

This is, 

Per directory environment:
https://tonyfinn.com/blog/nix-from-first-principles-flake-edition/nix-8-flakes-and-developer-environments/
## 

# Multiple Java Versions

# Intellij

# Gradle Toolchains

