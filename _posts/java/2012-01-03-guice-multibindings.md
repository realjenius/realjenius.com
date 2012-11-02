---
title: 'Micro Framework Plugin Architecture with Guice Multibindings'
summary: 'Google Guice (my favorite DI framework for Java) has a neat extension called "Multibindings" that allows you 
to auto-magically inject a collection of objects that implement a particular interface. This allows you to model SPI-style 
interfaces with ease.'
tags: java guice multibindings
categories: journal
legacydate: 01/03/2012 21:40:00
layout: post
---
   
Google Guice is a snazzy way to avoid all of the declarative configuration file noise and cruft, while still getting 
the modularity and de-coupling that you want from dependency-injection. Unlike Spring, Guice doesn't ship with the 
kitchen sink. When I first picked Guice up, I wasn't sure how (or if it was even possible out of the box) to do what 
I intended, which was to collect up all implementations of a particular type via injection. This is something that 
can be done probably in thirty five distinct ways in Spring (all of which require about fifteen XML files, if memory 
serves), so I was struggling to find the answer.

The answer, in fact, is straightforward: **[Multibindings](http://code.google.com/p/google-guice/wiki/Multibindings)**.

What is particularly neat about multi-bindings (aside from the fact they can inject a set of objects) is that they
will accept bindings from multiple modules into the final aggregate set that is injected. Both sets and maps can be injected in this way.
This example will use the MapBinder, as the base Guice example for Sets is fairly straightforward in its own right.
Consider a music decoding application, for example. You might have simple decoder API that looks like this:

{% highlight java %}
public interface AudioDecoder {
    String getAudioTypeName();
    void decode(InputStream encodedIn, OutputStream pcmOut);
}
{% endhighlight %}

Individual modules can inject their own audio decoding algorithm:

{% highlight java %}
public class Mp3AudioDecoder {
  public String getAudioTypeName() { "MP3"; }
  public void decode(InputStream mp3In, OutputStream pcmOut) {
    // Run through LAME (or similar Fraunhofer) decoding here.
  }
}

// ...

public class Mp3AudioModule extends AbstractModule {
  public void configure() {
    Multibinder<String,AudioDecoder> decoderBinder
      = MapBinder.newMapBinder(binder(), AudioDecoder.class);
      decoderBinder.addBinding("mp3").to(Mp3AudioDecoder.class);
  }
}
{% endhighlight %}

What this does is register a binding with the multibinder from `AudioDecoder` to `Mp3AudioDecoder`;
effectively registering that type as part of the total set of audio decoders. Consuming these via injection requires no special
sauce; simply declaring you want to receive the map is all that is required:

{% highlight java %}
public class RootModule extends AbstractModule {
  public void configure() {
    bind(AudioDecodingThingy.class);
  }
}
        
public class AudioDecodingThingy {

  private final Map<String,AudioDecoder> decoders;
    
  @Inject
  public AudioDecodingThingy(Map<String,AudioDecoder> decoders) {
    this.decoders = decoders;
  }

  public void run(String inputFile, String outputFile) {
    // Basic error handling.
    File in = new File(inputFile);
    File out = new File(outputFile);
    String extension = getExtension(inputFile);

    if(!in.exists()) throw new IllegalArgumentException("File " + inputFile + " not found.");
    if(!decoders.containsKey(extension)) throw new IllegalArgumentException("No decoder found for extension: " + extension);
    if(!out.exists() && !out.createNewFile()) throw new IllegalArgumentException("Unable to create output file: " + outputFile);

    AudioDecoder decoder = decoders.get(extension);

    // Do the decoding.
    try(
      InputStream in = new FileInputStream(in);
      OutputStream out = new FileOutputStream(out)) {
        decoder.decode(in, out);
    }
  }
}
        
public class Main {
  public static void main(String[] args) {
    Injector i = Guice.createInjector(
      new RootModule(), new Mp3Module(), new OggModule());

    AudioDecodingThingy thingy
      = i.getInstance(AudioDecodingThingy.class);

    thingy.run(args[0], args[1]);
  }
}
{% endhighlight %}

While this particular example (and the example on the Guice site) have the modules to snap-in defined directly in the code,
it is not a stretch of the imagination to envision the possible modules to install coming from:

* A configuration file
* The built-in JAR service-provider and ServiceLoader facilities.
* A scan for all particular annotated types; something like `@PluginModule`

As a final disclaimer (one that is reiterated on the Guice site) Multibindings are not a replacement for a full
modular architecture, like that which can be achieved with OSGi (in fact, Guice has
[support for OSGi](http://code.google.com/p/google-guice/wiki/OSGi) as well). However, sometimes 
OSGi can be a power-drill, when sometimes all you need is a plain ol' screwdriver.

Multibindings, which are an extension to Guice, are shipped as a separate integration JAR file. All of the official 
extensions are [available in the core Maven repositories](http://mvnrepository.com/artifact/com.google.inject.extensions) under `com.google.inject.extensions`.