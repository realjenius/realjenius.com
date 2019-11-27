---
title: "Distroless and Jib: Lightweight Java Container Images"
summary: "Both Distroless and Jib make Java containers a lot easier to build and deploy"
tags: ['java','docker','containers','jib','distroless']
date: 2019-11-27
---

The server world has moved to containers. And rightfully so: they isolate application concerns, they unify deployment, they are easy to host, and they make big complex systems like Kubernetes possible. Unfortunately, Java has been slow to adapt to the container world. Thankfully, tools are starting to become prevalent that make Java in containers easy and effective.  [Distroless](https://github.com/GoogleContainerTools/distroless) and [Jib](https://github.com/GoogleContainerTools/jib) are two of those tools.

<!--more-->

# Overview

Historically, Java developers have suffered in creating effective container images for their applications. There have been a variety of reasons for this:

* The JVM has been hard to package in containers: binary dependencies, package complexity, and a lack of official support for docker images from Oracle. Making big several hundred MB container images is hard on servers that need to move them around, and is also hard on your image registry of choice.
* The Java runtime was historically not capable of understanding the constraints of the container in which it was hosted, making dynamic heap memory allocation inefficient at best, dangerous and foolhardy at worst.
* Java build tools have not historically had great support for interfacing with the Docker daemon, so building a docker image was harder than just building a JAR.
* Java is often slow to start, and making big several-hundred MB containers only made this even worse. Most container deployment technologies generally expect containers to start and get healthy fast, and small containers help get there.

Thankfully Java has finally started to evolve as people and companies have faced these challenges head-on. There are several articles worth writing on how to optimize for containers. Today we'll look at two of these challenges:

1. [Distroless](https://github.com/GoogleContainerTools/distroless) will help us with container size and complexity
1. [Jib](https://github.com/GoogleContainerTools/jib) will help with making our image builds fast and portable

# Exploring Distroless Java

To this day one of the most popular base images for a "Java-capable" docker image is the [official OpenJDK base image](https://hub.docker.com/_/openjdk). The default variant (such as `FROM openjdk:8`) is built on Debian. As a result, it's a big container immediately.

*If you want to play along, I have created this [sample repository at Github](https://github.com/realjenius/distroless-jib-101) with each variant, and will share the results here.*

For a very simple project, an openjdk Dockerfile may [look like this](https://github.com/realjenius/distroless-jib-101/blob/master/Dockerfile-openjdk8):

```
FROM openjdk:8
COPY dockertest.jar .
CMD ["java", "-jar", "dockertest.jar"]
```

We can see that after building this, we get an image that is almost 500MB in size:

```bash
~/P/distroless-jib-101 ❯❯❯ docker build -f Dockerfile-openjdk8 -t dockertest-openjdk8 .
[...]
Successfully built 31d08499edff
Successfully tagged dockertest-openjdk8:latest

~/P/distroless-jib-101 ❯❯❯ docker image inspect dockertest-openjdk8 --format='{{.Size}}'
488206083
```

For the most capable general purpose linux glibc JVM, this may be what you need to go with. However, most devs are doing fairly mundane things that can work on a JVM anywhere (dealing with network sockets to shuttle HTTP and talk to databases and such). As a result, there are also `alpine` variants of the OpenJDK instance. Alpine is a smaller Linux OS that ships a lot fewer things and uses musl libc instead. Chances are good it will work for most projects.

We can also build with the alpine variants, which will reduce image size decently. [All that needs to be changed is the base image](https://github.com/realjenius/distroless-jib-101/blob/master/Dockerfile-openjdk8-alpine):

```
FROM openjdk:8-alpine
COPY dockertest.jar .
CMD ["java", "-jar", "dockertest.jar"]
```

We can build this and see if it helps:

```bash
~/P/distroless-jib-101 ❯❯❯ docker build -f Dockerfile-openjdk8-alpine -t dockertest-openjdk8-alpine .
[...]
Successfully built 60a4f331a471
Successfully tagged dockertest-openjdk8-alpine:latest

~/P/distroless-jib-101 ❯❯❯ docker image inspect dockertest-openjdk8-alpine --format='{{.Size}}'
337540868
```

Over a 30% reduction - not bad!

[Distroless](https://github.com/GoogleContainerTools/distroless) takes a different approach. Instead of shipping the lightest OS possible inside the container, Distroless just doesn't ship an OS at all - instead it ships the bare minimum to run the JVM for us - none of the other stuff that exists just to make the OS happy.

As a result, the image will be much smaller, *and* the image will be more secure, as there are far fewer exploit vectors shipped in the image.

However, the Java application should not make any OS assumptions, internally. As with the transition to Alpine, the Java program must be written portably to avoid hitting edges that will not work. If you need more moving pieces and parts at the OS-level, this may not be for you (or maybe you should [possibly consider sidecars...](https://www.oreilly.com/library/view/designing-distributed-systems/9781491983638/ch02.html))

We can build our example again, this time with distroless. This time [we have to change the command being executed](https://github.com/realjenius/distroless-jib-101/blob/master/Dockerfile-distroless8), as `distroless` *does not ship a shell*, so the CMD is oriented around executing language-specific files (in this case Java JAR files):

```
FROM gcr.io/distroless/java:8
COPY dockertest.jar .
CMD ["dockertest.jar"]
```

We can build and inspect size as usual:

```bash
~/P/distroless-jib-101 ❯❯❯ docker build -f Dockerfile-distroless8 -t dockertest-distroless8 .
[...]
Successfully built f4290b8f8e34
Successfully tagged dockertest-distroless8:latest

~/P/distroless-jib-101 ❯❯❯ docker image inspect dockertest-distroless8 --format='{{.Size}}'
125222612
```

As you can see, with distroless we have gained a 75% reduction in image size. We can also do a quick bench test to see that startup times are effectively the same for all images:

```bash
~/P/distroless-jib-101 ❯❯❯ for i in {1..5}; do time docker run dockertest-openjdk8; done
Hello!
docker run dockertest-openjdk8  0.04s user 0.02s system 3% cpu 1.696 total
Hello!
docker run dockertest-openjdk8  0.04s user 0.02s system 2% cpu 1.836 total
Hello!
docker run dockertest-openjdk8  0.04s user 0.02s system 3% cpu 1.718 total
Hello!
docker run dockertest-openjdk8  0.04s user 0.02s system 3% cpu 1.808 total
Hello!
docker run dockertest-openjdk8  0.03s user 0.02s system 2% cpu 1.870 total

~/P/distroless-jib-101 ❯❯❯ for i in {1..5}; do time docker run dockertest-openjdk8-alpine; done
Hello!
docker run dockertest-openjdk8-alpine  0.04s user 0.02s system 2% cpu 1.953 total
Hello!
docker run dockertest-openjdk8-alpine  0.04s user 0.03s system 3% cpu 1.719 total
Hello!
docker run dockertest-openjdk8-alpine  0.04s user 0.02s system 3% cpu 1.705 total
Hello!
docker run dockertest-openjdk8-alpine  0.04s user 0.02s system 3% cpu 1.738 total
Hello!
docker run dockertest-openjdk8-alpine  0.03s user 0.02s system 3% cpu 1.767 total

~/P/distroless-jib-101 ❯❯❯ for i in {1..5}; do time docker run dockertest-distroless8; done
Hello!
docker run dockertest-distroless8  0.04s user 0.03s system 3% cpu 1.776 total
Hello!
docker run dockertest-distroless8  0.04s user 0.02s system 3% cpu 1.845 total
Hello!
docker run dockertest-distroless8  0.03s user 0.02s system 3% cpu 1.758 total
Hello!
docker run dockertest-distroless8  0.04s user 0.02s system 3% cpu 1.874 total
Hello!
docker run dockertest-distroless8  0.04s user 0.03s system 3% cpu 1.900 total
```

## Caveats

First: Distroless is built specifically to run Java code. While you can customize details about how the JVM runs your code, you are not going to be able to set up various other things in the OS of your image.

Second: Distroless versions are built along with LTS java versions. As a result there is only a Java 8 and a Java 11 image currently (and presumably there will be a Java 14 base image). Additionally, the Java 11 version being published lagged behind the Java 11 release date. That is not to say you couldn't build your own Distroless base image, but that obviously means more work.

# Easy Docker Images with Jib

The chocolate to the distroless peanut butter has to be [Jib](https://github.com/GoogleContainerTools/jib). Jib solves another common problem with building Java containers: portably and easily building the images.

The Jib README explains it as well as I could:

> Goals:
>
>    Fast - Deploy your changes fast. Jib separates your application into multiple layers, splitting dependencies from classes. Now you don’t have to wait for Docker to rebuild your entire Java application - just deploy the layers that changed.
>
>    Reproducible - Rebuilding your container image with the same contents always generates the same image. Never trigger an unnecessary update again.
>
>    Daemonless - Reduce your CLI dependencies. Build your Docker image from within Maven or Gradle and push to any registry of your choice. No more writing Dockerfiles and calling docker build/push.

In other words, Jib can build a docker image without ever leaving your friendly build tool, and it can consider your class file changes when building to significantly improve build time efficiency.

In fact, Jib doesn't even **use a Dockerfile**. The configuration of Jib in your build script replaces the need for a Dockerfile, and as a result you should probably realize that Jib is not as powerful or general purpose as a Dockerfile -- it is meant to just add a layer of Java code to a fully functional base image.

Using Jib is straightforward if you have Maven or Gradle as your build tool. For my example I'm using Gradle. [Here is a simple build script that enables Java compilation and Jib image construction](https://github.com/realjenius/distroless-jib-101/blob/master/build.gradle.kts).

*Note: I'm using the Kotlin DSL here, but you could easily use the Groovy syntax instead*:

```kotlin
plugins {
  java
  id("com.google.cloud.tools.jib") version "1.8.0"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

jib {
  from.image = "gcr.io/distroless/java:8"
}
```

Jib has two main actions available:

* `jib` - This builds the image and uploads it to the container registry of your choice. For this to work you do have to help Jib with authentication details, and it can easily use something like [docker-credential-gcr](https://github.com/GoogleCloudPlatform/docker-credential-gcr) to look up gcloud based authentication credentials for uploading to Google Container Registry.
* `jibDockerBuild` - This can build the image and upload it to your local docker daemon.

We can switch to using our gradle build now instead of using the docker command line:

```bash
~/P/distroless-jib-101 ❯❯❯ ./gradlew jibDockerBuild --image=dockertest-jib

Containerizing application to Docker daemon as dockertest-jib...
Using base image with digest: sha256:a13ac1ce516ec5e49ae9dfd3b8183e9e8328180a65757d454e594a9ce6d1e35d

Container entrypoint set to [java, -cp, /app/resources:/app/classes:/app/libs/*, realjenius.dockertest.HelloWorld]

Built image to Docker daemon as dockertest-jib
Executing tasks:
[==============================] 100.0% complete
```

You can see that I provided the `--image` command line parameter to control the target image name. This can also be specified within the Gradle script.

We can now inspect and run this image just like our other examples:

```bash
~/P/distroless-jib-101 ❯❯❯ docker image inspect dockertest-jib --format='{{.Size}}'
125222117

~/P/distroless-jib-101 ❯❯❯ for i in {1..5}; do time docker run dockertest-jib; done
Hello!
docker run dockertest-jib  0.04s user 0.02s system 3% cpu 1.995 total
Hello!
docker run dockertest-jib  0.03s user 0.02s system 3% cpu 1.879 total
Hello!
docker run dockertest-jib  0.03s user 0.02s system 3% cpu 1.843 total
Hello!
docker run dockertest-jib  0.04s user 0.02s system 3% cpu 1.924 total
Hello!
docker run dockertest-jib  0.04s user 0.02s system 3% cpu 1.761 total
```

As you can see, it's just as small as it was when building distroless with the traditional docker daemon. The beauty of this is that it can be run on any command line that can run Gradle -- it does not require Docker to be running at all.

## The Relationship between Jib and Distroless

While Jib is designed to work quite well with distroless, you can actually use any base image you want, including OpenJDK or Alpine, or even your own extension of one of these images -- it just needs a Java command line to exist.

Many people (understandably) confuse and conflate Jib and Distroless, however they are totally separate tools. As illustrated here, you can use each without the other if you desire.

# Summary

For small and portable Java docker image construction using traditional class files, Jib and Distroless are an excellent choice. If you are running on platforms like [Google Cloud Run](https://cloud.google.com/run/) or [Kubernetes](https://kubernetes.io/) it might be a very useful tool for you.
