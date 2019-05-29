---
title: "Paperless Adulting on Linux with Evernote and NoteSlurp"
date: 2019-05-29
tags: ["paperless","personal","evernote","noteslurp","kotlin"]
---
Adulting is hard. Bills come and go, taxes have to be prepared, escrows have shortages, kids have activites, FSAs need proof of medical expenses, cars need to be registered.

The list of things an adult has to do seems to only grow larger over time, and one of the fundamental things that all adults seem to have to do is to track and file paperwork. I've gone "paperless" in my personal life (which I seem to have interpreted it to mean: "keep digital copies of everything, even if it was paper originally").

When it comes to adulting, barriers to doing the right thing (like actually getting stuff filed paperlessly) are bad, as you won't do them. So, recently I decided to make that just a little bit easier on myself with a new tool. Let me walk through the research I've done, what my process for paperless filing is, and where I've historically had problems, and how this new tool has helped me out.

<!--more-->

**TL;DR:** I wrote [NoteSlurp](https://github.com/realjenius/noteslurp) to help Linux and command-line users go paperless with Evernote. Check it out!

### Looking at Options

Years ago when I decided to try to go paperless, I made a trial run at a variety of options for electronic filing of documents, including:

* Dropbox
* Google Drive
* NAS at home
* Bespoke paperless filing companies in the wild (some have come and gone since I did my original research)

All of these worked fine as "backed up file storage", but as a software engineer, that was never really my problem - I could backup files a million different places. Instead, I wanted a solution that actually provided benefit over just throwing stuff in a filing cabinet, and that meant improved organization and reachability of my documents.

In the end, I landed on [Evernote Premium](https://evernote.com/) as my filing platform of choice.

{{< figure src="/img/articles/personal/evernote_logo.png">}}

Despite the monthly cost, there were a variety of reasons that this eventually won out:

* Evernote's OCR is unmatched. If someone refers to account number "13924123" for a medical visit three years ago, I can search for that number and immediately find every attached document for that office.
* The tagging system is very flexible.
* Evernote has more features than just file storage. Several documents can be grouped on a single note, and additional text, tables, and links can be associated as well, allowing me to annotate paperless filings.
* The Web and Mobile support both work quite well
* Evernote has two-factor authentication and a transparent definition of how they handle security and resiliency in their note storage system.

Overall, for the past three years, Evernote has served me quite well... at least, once I get my attachments and documents in. To understand this problem a little better, it's worth talking a bit about my home setup, and process.

### My Home Filing Setup

At home, I'm a Linux desktop user most days (useful for my profession), but I also have a Macbook Pro laptop. My phone is Android, and I have a reasonably good automatic document feed (ADF) scanner on my all-in-one printer that can scan directly to a USB drive.

With Evernote, I borrowed some ideas from [Bryan Kramer's Peek into how he uses Evernote](https://www.bryankramer.com/a-peek-inside-how-i-use-evernote-to-organize-my-life/).

Specifically in Evernote, I have two primary notebooks:

* Inbox - This is where everything goes before it has been categorized and filed.
* Cabinet - This is the permanent home of my paperless contents (I think of it as a digital filing cabinet in the basement). Stuff only gets moved here once I've tagged it the way I want.

Periodically, I will go through my inbox, and every note gets my attention by being tagged appropriately. Once I'm happy with the categorization, the note is moved into the cabinet (out of my central eyeline).

As for tagging, I take two primary approaches:

1. I have tags organized by year-month (e.g. `2019-04`). For years, prior to going to paperless, my primary filing scheme was by month and by year, and each year got a box in the basement. This is useful if I know roughly when something happened or I want to see what our Electricity cost was in May of 2017.
2. I also have important concepts captured as tags. Things like: `Medical`, `Charity`, `Taxes`, `Work`, `<childname>-School`, `Recipes`, etc. This is the first real big advantage over paper filing organization, because it lets me find things in other key ways and see a cross-cut of things by a different angle. With paper filing you have to choose the home.

These two approaches mean I can tag documents for `2018-12` so I can casually scan a month of files, but I can also tag a file as `Car` and `Insurance` at the same time, giving me multiple places to quickly go if I need to find something related to the most recent car insurance declarations or costs.

This model has worked extremely well for me for the past few years, and has made it very easy to find quick answers to questions that may have otherwise involved trips to the basement and a lot of flipping through papers.

### Getting Documents In: The Source Matters

While this organization process has been great, the hard part for me is actually doing the filing **in** to Evernote.

With Evernote, I have learned that the source of stuff to file really matters when it comes to the complexity. Some things are easy, and some things take more work.

#### E-Mails

Generally, E-mails I might want to file in Evernote fall into two categories:

* All of the contents are in text form - an example of this might include emails from my child's teacher that have curriculum details I might want to review later.
* The contents are mostly in an included attachment - for example: invoice emails from companies that directly attach the statement as a PDF on the email

For both of these cases, Evernote has the ability to email notes directly into your inbox. So when one of these comes in, I simply forward it to my "Evernote Account", and then delete the email. This sends the email directly into Evernote, and then I will have to go in and categorize it.

{{< figure src="/img/articles/personal/evernote_send_email.png">}}

#### Websites

Often times a website will have the contents I want, but they are on on the site itself. This may be a recipe I want to save, or it may be a "please print this page for your records" type of post-payment receipt (perhaps for charity filing for taxes).

In these cases there is generally one of two options I will choose:

1. The [Evernote Web Clipper](https://help.evernote.com/hc/en-us/articles/209125877-Quick-start) - This browser extension can easily snip selections or full contents of web-pages to store directly as a note. It usually works reasonably well
2. Print the page using "Save as PDF".

{{< figure src="/img/articles/personal/evernote_webclipper.png">}}

When using the web clipper, (as with emails) the note is sent directly to Evernote, so the only thing I have left to do is categorize it later.

However, sometimes a PDF is better because a site has a printable form that cannot be clipped easily or something.

With the PDF I now have to get the actual file in to Evernote. This leads to coping with things that are not already digital, and that's the hardest part for us "non Windows" users.

#### PDFs and Scanned Documents in the Wild

By far the most common type of document that I want to get into Evernote is typically in the form of a PDF. These come into my life in three forms:

1. As mentioned above, the "Save to PDF" feature of modern print dialogs
2. Downloaded statements from various companies and utilities where I have already gone "paperless".
3. Scans of paper mail, things from my childrens' school, receipts of importance, lab results from doctors, etc. All of the other stuff still done by paper in the world.

As mentioned above, to get things into PDF form that are on paper, I typically just use my scanner, which directly scans to a PDF with a name like `EPSON001.PDF`. Overall, it's no big deal to scan the week's paper stuff, and then copy the files from USB stick to my computer.

To accomodate knowing all of the "need to file" stuff on my computer, I've typically had a folder named "sync" or "evernote-sync" that allows me to keep track of the stuff that needs to be filed (e.g. `/home/realjenius/Documents/sync`).

#### Coping with "The Attachment Gap"

The biggest problem I've consistently had with Evernote is efficiently getting PDFs and other attachments ingested. There are a few different ways to do this out of the box:

* If you are on Windows, the Windows Evernote desktop app still has the "Import Folders" feature, which will monitor a folder and import all attachments as new notes. This works OK, if you are on Windows. Sadly, I'm not a Windows user if I can avoid it.
* Windows and Mac users can drag and drop individual files onto their notebook of choice one by one, and just as with "Import Folders", it will automatically create a note for each dropped attachment. While I have a Mac, it's not my primary machine. And even if it was, dragging and dropping individual files can be very tedious.
* Mobile users can choose to use the Mobile camera as their scanner instead of an ADF. I find that this is a great tool in a pinch (scanning something I can't take with me, for example), but it is too fiddly and inconsistent to use in place of my ADF. Additionally, this does nothing to help with PDFs downloaded from other sources; it only helps with actual paper content.
* Web users have to resort to manually creating and naming a document, pressing the "Attach" button, and then using a File Chooser to find the relevant attachment. This is a pain. I'll admit I have waited for a couple of years for the modern Evernote web UI to have support for drag and drop or similar, but even then it feels a bit like a barrier to use.

#### Rube Goldberg Would Be Proud

Since I've been a Linux user for years and have struggled with this part of my process, I originally invented a process that, while effective, was a contraption of which I wasn't proud. To get attachments up "automatically", I would:

1. Share my local "evernote-sync" directory to DropBox
1. Link [Zapier](https://zapier.com/) to my DropBox account via the [Dropbox Integration](https://zapier.com/apps/dropbox/integrations) at that specific directory, and check that shared directory every 10 minutes for new files
1. When a new file is found, create a note with that as the attachment using the [Zapier Evernote](https://zapier.com/apps/evernote/integrations/) integration

This was in perfect keeping with the spirit of internet-of-things: "why use one tool when you can use three, connected with tape and bailing wire?" Obviously, this has a number of problems: like free limits on services like DropBox and Zapier, and also security: now three companies are handling my precious (and sometimes sensitive) documents, instead of the one I agreed to entrust and lock-down.

Sadly, this also required babysitting. I had to clean out the attachments periodically lest it get confused and forget about a file and re-upload it or similar.

Clearly, this was not ideal and I wasn't a huge fan of this setup, but it "worked" and kept me from failing completely at paperless for a time.

### Categorizing: Another Hurdle

So, even if now documents are in Evernote, they are pretty much universally in the Inbox, untagged, and waiting for categorization.

This process can easily become a problem, such that before long my inbox is dominated by documents, and just the effort of tagging them all is overwhelming. Unfortunately, it means revisitng every document all over again - those I just stored into a folder previously.

A tip for many things in life that are unpleasant: it should be made as easy as possible so you don't neglect it. This was stopping me.

## I Built a Thing

It turns out that for a living I'm actually a software developer, and Evernote has an API that can be used to build things.

As a result, I wrote a tool called [NoteSlurp](https://github.com/realjenius/noteslurp). NoteSlurp is a command line tool for automating the ingestion of documents in to Evernote and for helping with categorization.

### What is It?

NoteSlurp is, at its heart, a tool for automatically scanning and tagging Evernote documents. In short, you create a manifest of tag keywords and regex patterns to help it with automatically tagging documents in Evernote. Then, you set it up to periodically scan a directory on your machine, and you simply drop PDFs in with appropriate names.

Once in a while you can then use NoteSlurp to review your inbox and make sure things are tagged correctly (or correct them) before filing them in cold storage.

### How Do I Use It?

The [README](https://github.com/realjenius/noteslurp/blob/master/README.md) provides detailed usage instructions, but in general here is my workflow for filing now that this tool is in place.

1. Any time I get a paper document, I scan it with my ADF and create a PDF.
2. I collect all PDFs (including those downloaded from various sites) into a folder on my local machine `/home/realjenius/Documents/sync`
3. Every PDF dropped there is given a name by me that includes zero-to-many fancy keywords and patterns that are picked up automatically to pre-tag the document when inserting into Evernote (such as `car_insurance_renewal_05_2019.pdf`) -- this might produce "Insurance", "Car", and "2019-05" as valid tags
4. The tool runs every 5 minutes using the `run` command, pulling documents into Evernote on my behalf
5. Periodically I also execute the `file-notes` command to quickly walk through and verify notes to be filed away for good.

### How Can You Use It?

My recommendation would be to README the readme on Github and provide feedback where you find it lacking. Additionally, there is a [Release](https://github.com/realjenius/noteslurp/releases) with a pre-packaged JAR ready for consumption.

## Two Months In: An Assessment

I started writing NoteSlurp at the start of April, and then finished a usable version in the 2nd week. Since then I've been using and slightly tweaking it to further meet my workflow.

Overall, it has made a huge impact on getting through my backlog of paper filing on Linux. I'm now more productive than I was at any point before, and find that I rarely have a lot of paper stacks anywhere at this point.

Further, reviewing my inbox is now much easier. I went through almost 800 documents I had yet to verify in the span of about 30 minutes or so, and cleaned up my life much further.
