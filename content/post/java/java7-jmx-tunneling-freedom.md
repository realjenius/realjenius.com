---
title: The Madness of Tunneling JMX is Over!
summary: 'Accessing JMX interfaces has been phenomenally difficult over the years due to some limitations. With recent Java 7 builds, that is a thing of the past.'
tags: ["java","jmx","rmi","firewall"]
date: 2012-11-21
---
I work with an immense number of clients running our software, and in turn, an immense number of servers in the wild. Generally speaking, I am playing a role of support and debugging, and one of the facilities we leverage to the hilt in our platform to help us here is JMX.

MBeans give us a tremendous amount of access into the running system, and are a good standardized way to publish the management tools. Unfortunately, they are a tremendous pain in the ass to get access to in a secure system.

Generally what I am given for any system we connect to is my own SSH key or SSH credentials, and possibly one or two ports popped open with pinholes for my IP address. The latter I can provide for myself with SSH tunnelling if I need to. Unfortunately, this isn't enough for JMX.

JMX runs on top of RMI, and as such, there are two ports that JMX utilizes:

* The JMX connect port.
* The (infamously) roaming RMI data port.

In all recent builds of Oracle Java, you can affix the first port via this system property when starting the Java program:

```java
-Dcom.sun.management.jmxremote.port=1099
```

Unfortunately, the second port has no mechanism for assignment. This means that you either need (a) the entire firewall cracked wide open for your IP address, or (b) you need to run a dynamic socks proxy and route your particular JMX tool through the proxy. The latter is generally the only real option. You then have the added complexity that the hostname you connect to needs to match the value of `java.rmi.server.hostname`, which in cloud solutions that do complex things with IP addresses (I'm looking at your EC2), only makes it even harder to get right.

Thankfully (and I do mean thankfully), not anymore for systems running Java 7u4 or higher. [Marcus Hirt](http://hirt.se/blog/) has blogged about [the new RMI binding property](http://hirt.se/blog/?p=289) that has come to Java since the merger with JRockit.

>Now, one handy but often overlooked feature that entered the 7u4 JDK as part of the JRockit convergence, was the ability to specify the port of the RMI server. By setting the port used by the RMI registry and the RMI server to the same port, tunneling will be much easier. Now, the name of the property for setting the port of the RMI Server was slightly changed from the JRockit implementation, and is now called com.sun.management.rmi.port, instead of com.sun.management.rmiserver.port. Here is an example of how to enable the external management agent with the same RMI registry and RMI server port on the command line, in JDK 7u4 and later:
>
>	java -Dcom.sun.management.jmxremote.port=7091 -Dcom.sun.management.jmxremote.rmi.port=7091 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

I cannot stress enough how much this should help those of us dealing with firewalled systems and JMX. Kudos!
