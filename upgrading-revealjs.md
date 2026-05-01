# Upgrading Reveal.js sources

## kslides Repository

The reveal.js sources are located in `docs/revealjs`.

The upgrade steps for it are:

* Rename the `docs/revealjs` dir to `docs/revealjs-old`.
* Clone and install the latest version of Reveal.js from [GitHub](https://github.com/hakimel/reveal.js/).
* Create a new docs/revealjs dir.
* Copy the new `examples/assets`, `css`, `dist`, `js`, and `plugin` dirs to `docs/revealjs`.
* Copy the `docs/revealjs-old/plugin/copycode` and `docs/revealjs-old/plugin/menu` dirs to `docs/revealjs/plugin`.
* Delete the `docs/revealjs-old` dir.

## kslides-template Repository

The revealjs sources also need to be copied to `kslides-template/docs/revealjs`. Copy them with:

``` bash
make sync-revealjs
```

## Menu plugin

https://denehyg.github.io/reveal.js-menu/#/home

## Copycode plugin

https://martinomagnifico.github.io/reveal.js-copycode/demo.html
