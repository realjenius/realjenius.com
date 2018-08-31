---
title: Highlighting Lines in Hugo Code Snippets
tags: ["hugo"]
date: 2018-08-30
---

Hugo makes it quite simple to share code snippets with syntax highlighting on your site, but did you know you can highlight individual lines in code-snippets as well?

<!--more-->

**Note:** Recent Hugo versions use a syntax highlighting engine called [Chroma](https://github.com/alecthomas/chroma), whereas older Hugo versions used [Pygments](http://pygments.org/). However, whether you are using Chroma or Pygments, this feature still works.

When using markdown, the default/simplest approach for highlighting code is simply to use a fenced code block:

{{< highlight md >}}
``` kotlin
class Person {
  var name: String = ""
  var age: Int = 0

  fun component1() = name
  fun component2() = age
}
```
{{< / highlight >}}

This results in clean highlighted code with a minimum of ceremony.

```kotlin
class Person {
  var name: String = ""
  var age: Int = 0

  fun component1() = name
  fun component2() = age
}
```

However, these code fences only support language parameterization. Hugo also allows you to get to more specific features of the highlighter by using the "highlight" shortcode. Specifically, you can request that individual lines be highlighted:

```md
{{</* highlight kotlin "hl_lines=5-6" */>}}
class Person {
  var name: String = ""
  var age: Int = 0

  fun component1() = name
  fun component2() = age
}
{{</* /highlight */>}}
```

This new syntax results in pretty line highlights targeting a code section of interest:

{{< highlight kotlin "hl_lines=5-6" >}}
class Person {
  var name: String = ""
  var age: Int = 0

  fun component1() = name
  fun component2() = age
}
{{< /highlight >}}
