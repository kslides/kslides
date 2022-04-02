# kslides

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/701fc37a847042d2ae2cd6e80075ff6f)](https://www.codacy.com/gh/pambrose/kslides/dashboard?amp;utm_medium=referral&amp;utm_content=pambrose/kslides&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/pambrose/kslides.svg?branch=master)](https://travis-ci.org/pambrose/kslides)
[![Release](https://jitpack.io/v/pambrose/kslides.svg)](https://jitpack.io/#pambrose/kslides)
[![Kotlin](https://img.shields.io/badge/%20language-Kotlin-red.svg)](https://kotlinlang.org/)

**kslides** is a Kotlin DSL wrapper for the incredible [reveal.js](https://revealjs.com) presentation framework. 
It is meant for people who prefer working with an IDE rather than PowerPoint. 
It works particularly well for presentations with code snippets and slides
authored in Markdown/HTML.

[![kslides screenshot](https://pambrose.github.io/kslides/img/kslides-screenshot.png)](https://pambrose.github.io/kslides/)

[This](kslides-examples/src/main/kotlin/Readme.kt) presentation is served statically from
[Netlify](https://kslides-readme.netlify.app)
and [Github Pages](https://pambrose.github.io/kslides/).
It is also running dynamically on [Heroku](https://kslides-readme.herokuapp.com).

## Getting Started

[![Template](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fpambrose%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ftemplate.json)](https://github.com/pambrose/kslides-template/generate)
[![Fork](https://img.shields.io/endpoint?color=%232A9EEE&logo=github&style=flat&url=https%3A%2F%2Fraw.githubusercontent.com%2Fpambrose%2Fkslides%2Fmaster%2Fdocs%2Fshields%2Ffork.json)](https://github.com/pambrose/kslides-template/fork)

[Create a presentation repo](https://github.com/pambrose/kslides-template/generate) with
the [kslides-template](https://github.com/pambrose/kslides-template) repo.

Must be logged in to see "Use this template" button

Speaker Notes do not work properly when running locally.

## Heroku Notes

* Add a Config Var: `GRADLE_TASK=-Pprod=true uberjar`

## MarkDown Slide Notes

* When a `markdownSlide` is in a `verticalSlides` section and references an external file, the string "---"
  is interpreted as a vertical page separator and "--- " (with a space suffix) is rendered as a markdown horizontal
  line.
* 
## Third Party Plugins

* https://github.com/Martinomagnifico/reveal.js-copycode
* https://github.com/denehyg/reveal.js-menu

