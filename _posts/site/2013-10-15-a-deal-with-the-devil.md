---
title: 'Media Temple Makes a Deal with the Devil'
summary: 'Media Temple is being acquired by GoDaddy. Allow me to begin jerking my knee in reaction.'
tags: personal realjenius.com mediatemple
category: journal
layout: post
---
Today, Media Temple announced that they are being acquired by GoDaddy.

Here's a tip for Media Temple: if you have to spend your entire FAQ trying to convince folks that the acquisition isn't a bad idea... maybe **it actually is a bad idea.**

But surely, that's not enough reason to switch hosts. It seems silly to move, just because of an acquisition that Media Temple has said so strongly [won't affect their service](http://weblog.mediatemple.net/2013/10/15/faqs-about-the-godaddy-acquisition/):

> #### What about GoDaddy’s reputation in the tech community?
> GoDaddy has been transformed in recent months and is essentially a new company. If we did not like what we have seen, we would not have joined the GoDaddy family. They have overhauled their leadership team and attracted tech talent from the best-of-the-best. We love “the new GoDaddy” that CEO Blake Irving and his team have created, especially their new approach with advertising, product focus and UX.
>
> Though neither GoDaddy’s brand nor operations are being integrated into ours, we are excited to be a positive influence on them with how to make even more improvements to better serve the Web pro community.
>
> #### Will Media Temple be merging with GoDaddy?
> No, we keep operating independently. The strategy to keep the two companies operating independently was critical for us. While we are excited about GoDaddy CEO Blake Irving’s vision to build bridges between GoDaddy and the Web developer community, the commitment to maintain (mt)’s independent operations is what sealed the deal for us.
>
> #### How does this affect me, as an (mt) customer?
> It doesn’t. (mt) and GoDaddy continue to operate independently, so it’s ‘business as usual.’ We’ll keep doing what we do best: serving you and helping you succeed online.

Me believing any of these answers is predicated by me "trusting" the author; my web-host. While I have implicitly trusted the intentions of Media Temple over the years I have used their service, I have absolutely no trust for Go Daddy. And the reason is simple: They have shown time and again to have no integrity.

Integrity is a key element in your relationship with your web-host, as you are giving them a lot of trust in the execution and support of your personal and business ventures. Sebastiaan de With has summarized perfectly how GoDaddy has shown they don't have the type of integrity I'm interested in my host having:

<div>
	<blockquote class="twitter-tweet"><p>“<a href="https://twitter.com/Scobleizer">@Scobleizer</a>: <a href="https://twitter.com/sdw">@sdw</a> <a href="https://twitter.com/mediatemple">@mediatemple</a> why switch? What are you looking for that you don&#39;t currently have?”&#10;&#10;Integrity. <a href="http://t.co/DEL6nu9rK8">pic.twitter.com/DEL6nu9rK8</a></p>&mdash; Sebastiaan de With (@sdw) <a href="https://twitter.com/sdw/statuses/390172367856496640">October 15, 2013</a></blockquote>
	<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
</div>

And so, with that, I must choose to vote with the only voice I have: my business. Which means I'm looking at the alternative hosts for the first time in many years. I currently run "unmanaged" VPS-style hosting via a DV-Developer at Media Temple, so my interests are specifically in finding a competitive and well-respected alternative in that space. If you are a Grid-Service or DV-Managed user; this list may not be useful for you.

For my analysis, I'm explicitly targeting the "$50/month" price point in unmanaged servers (tier 2 at MT). Let's enumerate what I get at Media Temple for that price:

* 16 cores; un-documented "fast-enough" CPU speeds.
* 2 GB of memory.
* 40 GB of RAID-10 storage.
* 650 GB bandwidth.
* Root + SSH access.
* Old but current-enough OpenVZ kernel capable of running Ubuntu 13.04.
* Parallels power panel
* Good top-level tools for re-provisioning, restoring in emergencies, upgrading on-the-spot, and managing DNS.
* Basic real-time server statistics
* Good tech support
* No built-in backups.

Now, let's look at some competition (in no particular order)!

{:.table .table-bordered}
|-+-+-+-+-+-+-+-|
|Host|Price|CPU|Memory|Disk|Bandwidth|Other Details|Thoughts|
|:-|:-:|:-:|:-:|:-:|:-:|:-|:-|
|[ATUM](http://www.atum.com/vps-hosting/unmanaged-vps/)|$49 (Bronze)|1@2.9Ghz|1GB|50GB|2TB|10K RPM Drives,Raid 5.|Not a fan of the single core, and their docs on their VM tech and kernel level is absent. Have to contact for OS details.|
|[Linode](https://www.linode.com)|$40 (2GB)|8@2x priority|2GB|96GB|4TB|Xen,HA,Data Center Choices,[Image Config Tools](https://www.linode.com/linodes/)|Linode looks very powerful, and I like their backup pricing and very configurable tools. Xen hypervisor is very nice. 8 cores is good.|
|[Digital Ocean](https://www.digitalocean.com/pricing)|$40 (Tier 4)|2@2-3Ghz|4GB|60GB|4TB|SSD,KVM,2-factor,custom admin,multiple data centers|Only complaint on specs is cores. Price is good. Popular with the "hip" crowd. Custom admin looks nice. No documentation on kernel/OS options on site without registering. Competitive snapshots and backups for pricing. [Awesome community docs](https://www.digitalocean.com/community).|
|[RimuHosting](http://rimuhosting.com/)|$50 (slider configurable)|2@?|2.5GB|48GB|100GB|Xen,Market pricing,slider config,multi data center,dated 12.04 Ubuntu only.|I tried RimuHosting previously. It's a good company and they have decent tools, but their core count is low, and their pricing isn't super competitive. Also, their Ubuntu options are old.|
|[Rackspace](http://www.rackspace.com/cloud/servers/)|$43.80 (Tier 2)|1 "vCPU"|1GB|40GB|.12c/GB|...|I'm not particularly a fan of the EC2-ish-ness of the RackSpace offering for my personal hosting. It's an OpenStack platform, so it's in many ways just like EC2. Probably good for horizontal scaling, but not really right for me.|
|[RocketVPS](http://www.rocketvps.com/features.html)|$44.99 (RS 4)|6@2Ghz|2GB|75GB|3.5TB|OpenVZ,SolusVM admin,Raid 10,[full server specs](http://www.rocketvps.com/features.html)|The pricing is competitive on RocketVPS, and I like full specs. Not a huge fan of OpenVZ, and SolusVM admin looks "just ok". Appears to be 6 quite fast cores.|
|[BurstNet](http://www.burst.net/linvps.php)|$49.95 (VPS 6)|1@4Ghz|4GB|250GB|4TB|RAID,1 IP,OpenVZ,vePortal,6 data centers|A very highly rated host, but also very enterprise scale which may make impersonal. They are very competitive on price and scale. Only complaint is CPU, and that may just be bad docs. OS choices are pretty geriatric, unfortunately.|
|[A Small Orange](http://asmallorange.com/hosting/cloud/)|$50 (Tier 4)|2@2.5Ghz|2GB|40GB|1TB|cPanel,Free Backups,KVM,Ubuntu 12.04 max,SSD,2 Dedicated IP|Interesting mom-and-pop style. The extra IP address is nice, as is free backups, and SSD. 2 cores is a little light, and bandwidth and disk are a little lower than some others.|
|-+-+-+-+-+-+-+-|

Here are options I explicitly excluded for one reason or another:

* EC2 and GCE - Biggest issue here is the commodity disks and the weird ephemeral IP nonsense. I've done a lot of hosting with EC2 through my previous life, and have no interest in bringing that into my personal world.
* Heroku - I'm actually a big fan of Heroku, but only if I had a desire to scale out, which I don't. I'd rather have one efficient-as-possible VPS, as opposed to the Heroku sandbox. I also need root access.
* HostGator - Hello 1998. No thank you.
* SoftLayer - SoftLayer has a lot of good enterprise-level hosting offerings (and their dedicated is way cheaper than cloud in many cases), but it's just not priced well for where I'm coming in. Feels targeted to bigger fish than I.
* DreamHost - I had nothing but bad experiences with the snarky, always ironic DreamHost "support". I have no interest to go back and feel like I'm in a hipster coffee shop all day.

### Summary

Right now I'm exploring hosts in this option:

1. Digital Ocean - This looks really compelling at the moment. Pricing is great; tools look great, and they have a good reputation right now.
1. Linode - Some folks simply swear by Linode, and I like some of their features such as HA proxy options. Pricing is good.
1. A Small Orange - These folks have a lot of street cred, and while their pricing isn't totally competitive everywhere, their deployment looks pretty sharp all told.

So did I miss anybody you'd suggest?