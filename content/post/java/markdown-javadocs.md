---
title: "Proposed for Java 23: Markdown Javadoc Support"
tags: ['java']
date: 2024-04-28
---
An interesting JEP is in-progress currently on OpenJDK to target Java 23: [Markdown Comments](https://openjdk.org/jeps/467). Markdown support would enable Javadocs to generate HTML through the use of embedded Markdown, rather than through the historically supported HTML+`@tag` support of the Javadoc tool today.

<!--more-->

The proposal suggests, to enable Markdown parsing for a comment rather than the existing Javadoc parser, the comment should use a triple slash: `///`, and must be positionally placed where a regular Javadoc comment would be supported (e.g. as a prologue to a class/package member).

Summarizing the other interesting details from the JEP, all of which match choices made in other languages:

* Unordered and ordered lists move from `<ul>` and `<ol>` to Markdown `-`, `*` and `#.` syntax
* `{@code ...}` is replaced by the regular Markdown backticks: ```...```
* `{@link ...}` is replaced by unqualified Markdown link syntax, e.g. `[java.util.HashMap]`
* Other inline style elements map accordingly, such as `<b>...</b>` becoming `**...**` 
* Line breaks naturally create paragraph tags, as per normal Markdown

As an example, here is a simple HTML-based Javadoc comment:

```java
/**
 * Enables configuration via the use of a {@link java.util.Map} input
 * <p>
 * This method supports configuration by a map with certain restrictions:
 * <ul>
 * <li>All entries must be strings
 * <li>Null values will be treated as unset
 * <li>Null keys are not supported
 * </ul>
 * <p>
 * Example: {@code configure(Map.of("host", "localhost")) }
 */
void configure(Map<String, String> configuration) { ... }
```

Here is the Markdown variant based on the proposal:

```java
///
/// Enables configuration via the use of a [java.util.Map] input
///
/// This method supports configuration by a map with certain restrictions:
///
/// - All entries must be strings
/// - Null values will be treated as unset
/// - Null keys are not supported
///
/// Example: `configure(Map.of("host", "localhost"))`
void configure(Map<String, String> configuration) { ... }
```

There is open discussion on the JEP on a few other key aspects of the proposal:

* As an implementation detail, the JEP indicates the use of the CommonMark Java library as the third-party library for support, ensuring reference compatibility with CommonMark.
* The JEP leaves open the idea of support for syntax highlighting via a tool like [Prism](https://prismjs.com/), as well as inline diagram rendering with [Mermaid](https://mermaid.js.org/) (using the expected code fence naming, e.g. ```mermaid ... ```)
* The proposal discusses the reasoning for using a different comment prologue sentinel for Markdown Javadocs, and what the consequences would be for supporting Markdown in traditional `/** */` tags - notably that it would almost always mangle some form of existing markdown
* The future work section has a few interesting ideas including: semantic discovery of tag values (like a `# Returns` heading section mapping to the `@returns` tag)

Aside from the inevitable lagging support this will introduce for IDEs, tree-sitter, and other Java syntax systems, this feels like a very welcome change. Many other languages use Markdown (or a simplified variant of Markdown) as the default markup syntax for comments, including: [Kotlin KDocs](https://kotlinlang.org/docs/kotlin-doc.html#inline-markup), [Golang Godocs](https://tip.golang.org/doc/comment), and [Rust Doc Comments](https://doc.rust-lang.org/rust-by-example/meta/doc.html) (to name a few). Supporting Markdown would help modernize Java in context to other popular current language platforms. As an additional anecdote, the `///` prefix matches those used by Rust to distinguish doc comments, as well.

