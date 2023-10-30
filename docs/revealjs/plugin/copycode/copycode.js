
/*****************************************************************
 * @author: Martijn De Jongh (Martino), martijn.de.jongh@gmail.com
 * https://github.com/Martinomagnifico
 *
 * CopyCode.js for Reveal.js 
 * Version 1.1.2
 * 
 * @license 
 * MIT licensed
 *
 * Thanks to:
 *  - Hakim El Hattab, Reveal.js 
 *  - Zeno Rocha for ClipboardJS
 ******************************************************************/



(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global = typeof globalThis !== 'undefined' ? globalThis : global || self, global.CopyCode = factory());
})(this, (function () { 'use strict';

	const Plugin = () => {
	  let options = {};

	  const isObject = item => {
	    return item && typeof item === 'object' && !Array.isArray(item);
	  };

	  const loadResource = (url, type, callback) => {
	    let head = document.querySelector('head');
	    let resource;

	    if (type === 'script') {
	      resource = document.createElement('script');
	      resource.type = 'text/javascript';
	      resource.src = url;
	    } else if (type === 'stylesheet') {
	      resource = document.createElement('link');
	      resource.rel = 'stylesheet';
	      resource.href = url;
	    }

	    const finish = () => {
	      if (typeof callback === 'function') {
	        callback.call();
	        callback = null;
	      }
	    };

	    resource.onload = finish;

	    resource.onreadystatechange = function () {
	      if (this.readyState === 'loaded') {
	        finish();
	      }
	    };

	    head.appendChild(resource);
	  };

	  const mergeDeep = function (target) {
	    for (var _len = arguments.length, sources = new Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
	      sources[_key - 1] = arguments[_key];
	    }

	    if (!sources.length) return target;
	    const source = sources.shift();

	    if (isObject(target) && isObject(source)) {
	      for (const key in source) {
	        if (isObject(source[key])) {
	          if (!target[key]) Object.assign(target, {
	            [key]: {}
	          });
	          mergeDeep(target[key], source[key]);
	        } else {
	          Object.assign(target, {
	            [key]: source[key]
	          });
	        }
	      }
	    }

	    return mergeDeep(target, ...sources);
	  };

	  const getPreBlocks = (preblocks, options, deck) => {
	    const generator = document.querySelector('[name=generator]');
	    let quarto = generator && generator.content.includes("quarto") ? true : false;
	    let revealEl = deck.getRevealElement();
	    revealEl.style.setProperty('--cc-copy-bg', options.copybg || options.style.copybg);
	    revealEl.style.setProperty('--cc-copy-color', options.copycolor || options.style.copycolor);
	    revealEl.style.setProperty('--cc-copied-bg', options.copiedbg || options.style.copiedbg);
	    revealEl.style.setProperty('--cc-copied-color', options.copiedcolor || options.style.copiedcolor);
	    revealEl.style.setProperty('--cc-scale', options.scale || options.style.scale);
	    revealEl.style.setProperty('--cc-offset', options.offset || options.style.offset);
	    revealEl.style.setProperty('--cc-radius', options.radius || options.style.radius);
	    revealEl.style.setProperty('--cc-copyborder', options.copyborder || options.style.copyborder);
	    revealEl.style.setProperty('--cc-copiedborder', options.copiedborder || options.style.copiedborder);

	    const styleButton = button => {
	      let originalIconsvg = [];
	      originalIconsvg.copy = '<svg aria-hidden="true" height="16" width="16" viewBox="0 0 16 16" version="1.1"><path d="M0 6.75C0 5.784.784 5 1.75 5h1.5a.75.75 0 0 1 0 1.5h-1.5a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-1.5a.75.75 0 0 1 1.5 0v1.5A1.75 1.75 0 0 1 9.25 16h-7.5A1.75 1.75 0 0 1 0 14.25Z"></path><path d="M5 1.75C5 .784 5.784 0 6.75 0h7.5C15.216 0 16 .784 16 1.75v7.5A1.75 1.75 0 0 1 14.25 11h-7.5A1.75 1.75 0 0 1 5 9.25Zm1.75-.25a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-7.5a.25.25 0 0 0-.25-.25Z"></path></svg>';
	      originalIconsvg.copied = '<svg aria-hidden="true" height="16" viewBox="0 0 16 16" version="1.1" width="16"><path d="M15.7,2.8c0.4,0.4,0.4,1,0,1.4L6,13.9c-0.4,0.4-1,0.4-1.4,0L0.3,9.6c-0.4-0.4-0.4-1,0-1.4c0.4-0.4,1-0.4,1.4,0l3.6,3.6l9-9C14.7,2.4,15.3,2.4,15.7,2.8z"/></svg>';
	      let copysvg = options.iconsvg.copy != "" ? options.iconsvg.copy : originalIconsvg.copy;
	      let copiedsvg = options.iconsvg.copied != "" ? options.iconsvg.copied : originalIconsvg.copied; // If the button needs to be shown at all

	      if (button.dataset.cc != false && button.dataset.cc != "false") {
	        // If icons are needed
	        let theDisplay = button.dataset.ccDisplay || options.display;

	        if (theDisplay == "icons" || theDisplay == "both") {
	          button.innerHTML = `<span></span>`;
	          button.textholder = button.getElementsByTagName("SPAN")[0];
	          button.insertAdjacentHTML("afterbegin", copiedsvg);
	          button.insertAdjacentHTML("afterbegin", copysvg); // If tooltip is wanted or not (global setting only)

	          if (button.dataset.ccDisplay && button.dataset.ccDisplay == "icons" && options.tooltip) {
	            button.textholder.style.display = "flex";
	          }
	        } // If custom text, else the global text


	        button.textholder.textContent = button.dataset.ccCopy ? button.dataset.ccCopy : options.copy || options.text.copy;
	      }
	    };

	    const buildStructure = preblock => {
	      let codeblock = null;
	      let dataHolder = preblock;

	      if (quarto && preblock.parentNode.matches(".sourceCode")) {
	        // Running in Quarto
	        codeblock = preblock.parentNode;
	        dataHolder = codeblock;
	      } else {
	        // Not running in Quarto
	        if (!preblock.parentNode.classList.contains("codeblock")) {
	          codeblock = document.createElement("div");
	          preblock.parentNode.insertBefore(codeblock, preblock);
	        }
	      }

	      codeblock.classList.add("codeblock");

	      if (dataHolder.dataset.cc && dataHolder.dataset.cc == "false") {
	        return;
	      } // Put the pre inside the wrapper


	      codeblock.appendChild(preblock);

	      if (options.display == "icons" || options.display == "both") {
	        dataHolder.dataset.ccDisplay = options.display;
	      }

	      if (preblock.classList.contains("fragment")) {
	        codeblock.classList.add("fragment");
	        preblock.classList.remove("fragment");
	      }

	      let button = document.createElement("button");
	      button.title = "Copy to Clipboard";
	      button.textholder = button;

	      if (options.button != "always") {
	        button.dataset["cc"] = options.button;
	      }

	      let possibleAttributes = ["cc", "ccCopy", "ccCopied", "ccDisplay"];
	      possibleAttributes.forEach(attribute => {
	        if (dataHolder.dataset[attribute]) {
	          button.dataset[attribute] = dataHolder.dataset[attribute];
	          delete dataHolder.dataset[attribute];
	        }
	      });
	      styleButton(button); // Insert the button

	      codeblock.insertBefore(button, preblock);
	    };

	    preblocks.forEach(preblock => buildStructure(preblock));
	    let clipboard = options.plaintextonly == true ? new ClipboardJS(".codeblock > button", {
	      text: function (trigger) {
	        return trigger.nextElementSibling.firstChild.innerText.replace(/^\s*\n/gm, "");
	      }
	    }) : new ClipboardJS(".codeblock > button", {
	      target(_ref) {
	        let {
	          nextElementSibling
	        } = _ref;
	        return nextElementSibling.firstChild;
	      }

	    });
	    clipboard.on("success", e => {
	      let button = e.trigger;
	      e.clearSelection();
	      button.dataset["textOriginal"] = button.textholder.innerHTML;
	      button.textholder.innerHTML = button.dataset["ccCopied"] ? button.dataset["ccCopied"] : options.copied || options.text.copied;
	      button.setAttribute("disabled", true);
	      setTimeout(() => {
	        if (button.dataset.ccDisplay && button.dataset.ccDisplay != "icons" || !button.dataset.ccDisplay) {
	          button.textholder.innerHTML = button.getAttribute("data-text-original");
	        }

	        button.removeAttribute("data-text-original");
	        button.removeAttribute("disabled");
	      }, options.timeout);
	    });
	    clipboard.on('error', e => {
	      console.error('There was an error copying the code: ', e.action);
	    });
	  };

	  const init = deck => {
	    let es5Filename = "copycode.js";
	    let defaultOptions = {
	      button: "always",
	      display: "text",
	      text: {
	        copy: "Copy",
	        copied: "Copied!"
	      },
	      plaintextonly: true,
	      timeout: 1000,
	      style: {
	        copybg: "orange",
	        copiedbg: "green",
	        copycolor: "black",
	        copiedcolor: "white",
	        copyborder: "",
	        copiedborder: "",
	        scale: 1,
	        offset: 0,
	        radius: 0
	      },
	      tooltip: true,
	      iconsvg: {
	        copy: '',
	        // User can paste <svg>…</svg> code here
	        copied: '' // User can paste <svg>…</svg> code here

	      },
	      csspath: "",
	      clipboardjspath: ""
	    };
	    const originalOptions = JSON.parse(JSON.stringify(defaultOptions));
	    options = deck.getConfig().copycode || {};
	    options = mergeDeep(defaultOptions, options);
	    options.defaultOptions = originalOptions;

	    const pluginPath = () => {
	      let path;
	      let pluginScript = document.querySelector(`script[src$="${es5Filename}"]`);

	      if (pluginScript) {
	        path = pluginScript.getAttribute("src").slice(0, -1 * es5Filename.length);
	      } else {
	        path = (typeof document === 'undefined' && typeof location === 'undefined' ? new (require('u' + 'rl').URL)('file:' + __filename).href : typeof document === 'undefined' ? location.href : (document.currentScript && document.currentScript.src || new URL('copycode.js', document.baseURI).href)).slice(0, (typeof document === 'undefined' && typeof location === 'undefined' ? new (require('u' + 'rl').URL)('file:' + __filename).href : typeof document === 'undefined' ? location.href : (document.currentScript && document.currentScript.src || new URL('copycode.js', document.baseURI).href)).lastIndexOf('/') + 1);
	      }

	      return path;
	    };

	    let ClipboardJSPath = options.clipboardjspath != "" ? options.clipboardjspath : "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.11/clipboard.min.js";
	    let CopyCodeStylePath = options.csspath ? options.csspath : `${pluginPath()}copycode.css` || 'plugin/copycode/copycode.css';
	    let preblocks = deck.getRevealElement().querySelectorAll("pre");

	    if (typeof ClipboardJS != "function") {
	      loadResource(ClipboardJSPath, 'script', () => {
	        if (typeof ClipboardJS === "function") {
	          if (preblocks.length > 0) {
	            loadResource(CopyCodeStylePath, 'stylesheet', () => {
	              getPreBlocks(preblocks, options, deck);
	            });
	          }
	        } else {
	          console.log("Clipboard.js did not load");
	        }
	      });
	    } else {
	      if (preblocks.length > 0) {
	        loadResource(CopyCodeStylePath, 'stylesheet', () => {
	          getPreBlocks(preblocks, options, deck);
	        });
	      }
	    }
	  };

	  return {
	    id: 'copycode',
	    init: init
	  };
	};

	return Plugin;

}));
