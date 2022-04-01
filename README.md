# kslides

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/pambrose/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=pambrose/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/pambrose/kslides.svg?branch=master)](https://travis-ci.org/pambrose/kslides)
[![Release](https://jitpack.io/v/pambrose/kslides.svg)](https://jitpack.io/#pambrose/kslides)
[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-red.svg)](https://kotlinlang.org/)

**kslides** is a Kotlin DSL wrapper for the incredible [reveal.js](https://revealjs.com) presentation framework. 
It is meant for people who prefer working in an IDE rather than PowerPoint. 
It works particularly well for presentations with code snippets and slides
authored in Markdown/HTML.

[![Screenshot](https://pambrose.github.io/kslides/kslides-screenshot.png)](https://pambrose.github.io/kslides/)

## Example

[This](kslides-examples/src/main/kotlin/Readme.kt) presentation is available statically from
[Netlify](https://kslides-readme.netlify.app)
and [Github Pages](https://pambrose.github.io/kslides/).
It is also running dynamically on 
[Heroku](https://kslides-readme.herokuapp.com).

## Getting Started

[![Template](https://img.shields.io/badge/kslides-template-blue?logo=github)](https://github.com/pambrose/kslides-template/generate)

[Create a presentation repo](https://github.com/pambrose/kslides-template/generate) using
the [kslides-template](https://github.com/pambrose/kslides-template) repo.

## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu


## Heroku Notes

* Add a Config Var for `GRADLE_TASK=-Pprod=true uberjar`

## MarkDown Slide Notes

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.

* https://stackoverflow.com/questions/49267811/how-can-i-escape-3-backticks-code-block-in-3-backticks-code-block
