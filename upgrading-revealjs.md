# Upgrading Reveal.js sources

The reveal.js sources are located in `docs/revealjs` and `kslides-core/src/main/resources/revealjs`.
One dir is for static slides and the other is for slides served via HTTP.

The steps to upgrade both are:

* Rename the `docs/revealjs` dir to `docs/revealjs-old`.
* Download the latest version of Reveal.js from [GitHub](https://github.com/hakimel/reveal.js/releases).
* Copy the new `assets`, `css`, `dist`, `js``, and `plugins` dirs to `docs/revealjs`.
* Copy the `docs/revealjs-old/plugin/copycode` and `docs/revealjs-old/plugin/menu` dirs to `docs/revealjs/plugins`.
* Delete the `docs/revealjs-old` dir.
* Delete the `kslides-core/src/main/resources/revealjs` dir.
* Copy the `docs/revealjs` dir to `kslides-core/src/main/resources/revealjs`.

