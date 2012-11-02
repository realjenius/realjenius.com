---
title: 'My Experience with a Media Temple (ve)'
summary: 'Some thoughts on the new Media Temple (ve) hosting solution.'
tags:
- hosting
- media temple
- (ve)
category: journal
legacydate: 8/4/2010 21:51
legacyId: 597
layout: post
---

I just switched RealJenius.com (and a whole crap-ton of other sites I host) from a [MediaTemple (dv) 3.5](http://mediatemple.net/webhosting/dv/) to one of their new [MediaTemple (ve)](http://mediatemple.net/webhosting/ve/) servers. This was kind of an ideal move for me, as I've been using Ubuntu as my primary development desktop for months, and with my recent career change, I've been doing a lot more system administration work (on Ubuntu VMs none-the-less).

If you're as lured by the (ve) as I was, let me give you some details from my experience:

* Provisioning is lightning-quick. I ordered the server, and it was available within 5 minutes.
* It is truly as they say: just a Linux box with SSH. For my install, I was given a stripped down Ubuntu 10.04 server instance and told "Go Play!".
* For that reason I would recommend, if you are migrating from somewhere else, that you practice the migration on a VM somewhere, like with [VirtualBox](http://www.virtualbox.org). Since you're responsible for the "whole shootin' match", it's in your best interest to have everything inline to make your transition as seamless as possible.
* Post-install configurations are everything! I have been using pre-packaged VPS solutions for so long, I forgot that they spend a lot of time tweaking and tuning software to fit in a box the size they choose. Software like PHP, Apache, MySQL, Java, Tomcat, etc - all comes with default values and selections that, I guarantee you, are different than most VPS's are running.
* Aptitude is your friend! I chose Ubuntu because I knew how blindingly simple the package manager was to use. The installation of Apache, PHP and MySQL support (while not tuned) was literally only bound by the time to download the packages. My job was to sit and watch it work.

I chose to do a number of things to tune my installation after I got it up and running. There are a lot of things to consider when you are your own sys-admin!

## Tuning Apache Memory

First - tell Apache to calm down on memory usage. The default Apache install will use *massive* amounts of room in each thread stack (mostly because Linux tells it to). In reality, unless you're expecting to handle some pretty impressive load while shipping some pretty impressive HTML, you probably don't need 8MB *per stack*. To detune Apache in Ubuntu 10.04 to use less memory, you can edit '/etc/init.d/apache2' and put a stack-ulimit adjustment at the top of the file:

	ulimit -s 256

Lowering my stack for Apache to 256k (which coincidentally is Java's default stack size), lowered my memory usage for Apache and whatever else was on the box at the time (running standard pre-fork) from 750MB, to 280MB. Now, that includes virtual memory and everything else - but when you've only got 1GB to work with, that's a pretty significant savings.

## Switching Apache's Multi-Processing Module

The next thing I did to trim the Apache fat was to switch to mpm-worker threads instead of mpm-prefork. Now, most folks may comment here that mpm-worker actually uses more memory at idle, and while that's technically true, it is much more predictable and scales much better from a memory perspective (think threads vs. processes if you are a Java or Ruby developer). It also generally happens to be faster, which is a nice benefit.

Unfortunately, when you move away from pre-fork, you also move away from easy PHP installations. To get PHP to work with mpm-workers, you have to use something like FastCGI, which acts as a PHP worker process pool that runs outside of the Apache process. Now - I made this a lot harder on myself by trying to host sites outside of /var/www - I wanted all of the folks sites I host to be under their respective home folder: /home/guy-who-mooches/guys-site.com/yadaydayada (just kidding people who I host!). I'll point you to several articles on the net about it:

* [http://www.howtoforge.com/how-to-set-up-apache2-with-mod_fcgid-and-php5-on-ubuntu-8.10](http://www.howtoforge.com/how-to-set-up-apache2-with-mod_fcgid-and-php5-on-ubuntu-8.10)
* [http://www.chriswiegman.com/2010/06/running-apachefastcgisuexec-in-ubuntu-10-04-without-varwww/](http://www.chriswiegman.com/2010/06/running-apachefastcgisuexec-in-ubuntu-10-04-without-varwww/)
* [http://www.unixguru.biz/howto-apache2-suexec-php5-and-fastcgi-for-virtual-domains/](http://www.unixguru.biz/howto-apache2-suexec-php5-and-fastcgi-for-virtual-domains/)
* [http://www.linode.com/forums/viewtopic.php?t=2982](http://www.linode.com/forums/viewtopic.php?t=2982)

All of these sites have different (albeit similar) approaches. The key things to double check in this effort are:

* Install `apache-suexec-custom` (the 'custom' is key!). Some sites suggest that you have to compile your own apache-suexec install; this is no longer true (that's what the custom is for).
* Leave Apache running (or make Apache run) as `www-data` - I made the mistake of switching it to the user apache/apache, and getting suexec-custom to work with that user was a fool's errand for me.
* With 10.04, you have to edit the file in `/etc/init.d/apache2/suexec` called `www-data` and change the first line from `/var/www` to wherever you want suexec to be allowed to run from (for me it had to be `/home`).
* You'll need to create a wrapper script for each user. Don't use symbolic links. This wrapper script must have that user as the owner/group (whatever user is going in the suexec directive, anyway).
* Pay attention to the properties you set in the wrapper script! They have drastic impacts on both performance and resource utilization. You need to have a feel for your site's intended usage to be able to tune these.
* The suxec directive and FCGI wrapper directives in the Virtual Host file vary in every article I read about this (there is more than one way to skin a cat) - I tried both the Action/AddHandler combo approach, and the FCGIWrapper approach. FCGIWrapper is simpler, but Action/AddHandler is more flexible. I'd start with FCGIWrapper to get it working, and when you are feeling more confident, try action/addhandler if you think you need it.

Anyway - rather than rehash a full walk-through, I felt it would be better to provide some bullet-points. Until I embraced understanding how FastCGI actually worked, I found I was struggling to properly implement it. So take your time, read the articles, try to understand the scripts, and come back here and read my tips. I think you'll be better off for it!

## Best Apache Tuning Tip

Many folks say the best Apache tuning tip is to use [Nginx](http://nginx.org/). That may be true - I wanted to move fast, and I know very little about administering Nginx. I know that I will have to do some .htaccess file conversion... so maybe in a couple weeks.

## User Management

I highly recommend creating a non-root user for yourself. Giving that non-root user sudo access gives you enough of a barrier to be careful, but still gives you the flexibility you need to administer. In Ubuntu, sudo-ing requires that your user be in the "sudo" group. That's as simple as:

	usermod -G sudo [username]

## Use Identity Files

This is a huge boon in my opinion. Identity files are much more secure than passwords, and can make your login process faster (if you're willing to trade-off the security of a passphrase). Here is a great article about it: [http://www.csua.berkeley.edu/~ranga/notes/ssh_nopass.html](http://www.csua.berkeley.edu/~ranga/notes/ssh_nopass.html)</p>

## Learn to SSH Tunnel

SSH tunneling is secure. If you know how to tunnel, there is no reason to want to install PHP My Admin for example. It's a security risk (don't believe me? scan the Apache access logs of any decently popular domain, and you will see hack-attempts against dozens of phpmyadmin common URLs), and it's not a very awesome administration tool. Meanwhile, MySQL administrator and MySQL Query Browser are awesome and just need an HTTP port. Here is one way to do it:

	ssh -L 1234:127.0.0.1:3306 myuser@myfakeserver.com

This forwards the port 1234 on your machine to port 3306 on your server (which is the default port for MySQL). Then, when you open MySQL Administrator/Query Browser, just try to connect to 127.0.0.1 on port 1234. Now you have full administrative access to MySQL (bells and whistles abounding) and you can be comfortable that the traffic is encrypted.

Tunneling with -D (dynamic SOCKS proxy) is also an excellent way to monitor a Java instance as well, using VisualVM and JMX ([http://stackoverflow.com/questions/1609961/visualvm-over-ssh](http://stackoverflow.com/questions/1609961/visualvm-over-ssh)). Again, totally secure monitoring of a running JVM - hard to beat.

## Conclusion

Media-Temple's (ve) offering is excellent in my estimation. The servers are fast, the memory #s are phenomenal (especially considering you can run whatever you want), and you have total unadulterated control. Two-thumbs-up MT!