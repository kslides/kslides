 /*****************************************************************
 *
 * reveal.js-copycode for Reveal.js 
 * Version 1.4.2
 * 
 * @link
 * https://github.com/martinomagnifico/reveal.js-copycode
 * 
 * @author: Martijn De Jongh (Martino), martijn.de.jongh@gmail.com
 * https://github.com/martinomagnifico
 *
 * @license 
 * MIT
 * 
 * Copyright (C) 2026 Martijn De Jongh (Martino)
 *
 ******************************************************************/


function H(o) {
  return o && o.__esModule && Object.prototype.hasOwnProperty.call(o, "default") ? o.default : o;
}
var x, E;
function B() {
  if (E) return x;
  E = 1;
  var o = function(i) {
    return e(i) && !t(i);
  };
  function e(n) {
    return !!n && typeof n == "object";
  }
  function t(n) {
    var i = Object.prototype.toString.call(n);
    return i === "[object RegExp]" || i === "[object Date]" || u(n);
  }
  var r = typeof Symbol == "function" && Symbol.for, s = r ? /* @__PURE__ */ Symbol.for("react.element") : 60103;
  function u(n) {
    return n.$$typeof === s;
  }
  function y(n) {
    return Array.isArray(n) ? [] : {};
  }
  function f(n, i) {
    return i.clone !== !1 && i.isMergeableObject(n) ? C(y(n), n, i) : n;
  }
  function g(n, i, c) {
    return n.concat(i).map(function(h) {
      return f(h, c);
    });
  }
  function p(n, i) {
    if (!i.customMerge)
      return C;
    var c = i.customMerge(n);
    return typeof c == "function" ? c : C;
  }
  function a(n) {
    return Object.getOwnPropertySymbols ? Object.getOwnPropertySymbols(n).filter(function(i) {
      return Object.propertyIsEnumerable.call(n, i);
    }) : [];
  }
  function l(n) {
    return Object.keys(n).concat(a(n));
  }
  function w(n, i) {
    try {
      return i in n;
    } catch {
      return !1;
    }
  }
  function T(n, i) {
    return w(n, i) && !(Object.hasOwnProperty.call(n, i) && Object.propertyIsEnumerable.call(n, i));
  }
  function v(n, i, c) {
    var h = {};
    return c.isMergeableObject(n) && l(n).forEach(function(d) {
      h[d] = f(n[d], c);
    }), l(i).forEach(function(d) {
      T(n, d) || (w(n, d) && c.isMergeableObject(i[d]) ? h[d] = p(d, c)(n[d], i[d], c) : h[d] = f(i[d], c));
    }), h;
  }
  function C(n, i, c) {
    c = c || {}, c.arrayMerge = c.arrayMerge || g, c.isMergeableObject = c.isMergeableObject || o, c.cloneUnlessOtherwiseSpecified = f;
    var h = Array.isArray(i), d = Array.isArray(n), A = h === d;
    return A ? h ? c.arrayMerge(n, i, c) : v(n, i, c) : f(i, c);
  }
  C.all = function(i, c) {
    if (!Array.isArray(i))
      throw new Error("first argument should be an array");
    return i.reduce(function(h, d) {
      return C(h, d, c);
    }, {});
  };
  var b = C;
  return x = b, x;
}
var F = B();
const N = /* @__PURE__ */ H(F);
let j = null;
const W = () => {
  if (j) return j;
  const o = typeof window < "u", e = typeof document < "u";
  let t = !1;
  try {
    const s = new Function('return typeof module !== "undefined" && !!module.hot')(), u = new Function('return typeof import.meta !== "undefined" && !!import.meta.hot')();
    t = s || u;
  } catch {
  }
  let r = !1;
  try {
    r = new Function('return typeof import.meta !== "undefined" && import.meta.env?.DEV === true')();
  } catch {
  }
  return j = {
    isDevelopment: t || r,
    hasHMR: t,
    isViteDev: r,
    hasWindow: o,
    hasDocument: e
  }, j;
};
class k {
  defaultConfig;
  pluginInit;
  pluginId;
  mergedConfig = null;
  userConfigData = null;
  /** Public data storage for plugin state */
  data = {};
  // Create a new plugin instance
  constructor(e, t, r) {
    typeof e == "string" ? (this.pluginId = e, this.pluginInit = t, this.defaultConfig = r || {}) : (this.pluginId = e.id, this.pluginInit = e.init, this.defaultConfig = e.defaultConfig || {});
  }
  // Initialize plugin configuration by merging default and user settings
  initializeConfig(e) {
    const t = this.defaultConfig, r = e.getConfig()[this.pluginId] || {};
    this.userConfigData = r, this.mergedConfig = N(t, r, {
      arrayMerge: (s, u) => u,
      clone: !0
    });
  }
  // Get the current plugin configuration
  getCurrentConfig() {
    if (!this.mergedConfig)
      throw new Error("Plugin configuration has not been initialized");
    return this.mergedConfig;
  }
  // Get plugin data if any exists
  getData() {
    return Object.keys(this.data).length > 0 ? this.data : void 0;
  }
  get userConfig() {
    return this.userConfigData || {};
  }
  // Gets information about the current JavaScript environment
  getEnvironmentInfo = () => W();
  // Initialize the plugin
  init(e) {
    if (this.initializeConfig(e), this.pluginInit)
      return this.pluginInit(this, e, this.getCurrentConfig());
  }
  // Create the plugin interface containing all exports
  createInterface(e = {}) {
    return {
      id: this.pluginId,
      init: (t) => this.init(t),
      getConfig: () => this.getCurrentConfig(),
      getData: () => this.getData(),
      ...e
    };
  }
}
const R = (o) => {
  const e = document.querySelector(
    `script[src$="${o}.js"], script[src$="${o}.min.js"], script[src$="${o}.mjs"]`
  );
  if (e?.src) {
    const t = e.getAttribute("src") || "", r = t.lastIndexOf("/");
    if (r !== -1)
      return t.substring(0, r + 1);
  }
  try {
    if (typeof import.meta < "u" && import.meta.url)
      return import.meta.url.slice(0, import.meta.url.lastIndexOf("/") + 1);
  } catch {
  }
  return `plugin/${o}/`;
}, I = "data-css-id", _ = (o, e) => new Promise((t, r) => {
  const s = document.createElement("link");
  s.rel = "stylesheet", s.href = e, s.setAttribute(I, o);
  const u = setTimeout(() => {
    s.parentNode && s.parentNode.removeChild(s), r(new Error(`[${o}] Timeout loading CSS from: ${e}`));
  }, 5e3);
  s.onload = () => {
    clearTimeout(u), t();
  }, s.onerror = () => {
    clearTimeout(u), s.parentNode && s.parentNode.removeChild(s), r(new Error(`[${o}] Failed to load CSS from: ${e}`));
  }, document.head.appendChild(s);
}), M = (o) => document.querySelectorAll(`[${I}="${o}"]`).length > 0, z = (o) => new Promise((e) => {
  if (t())
    return e(!0);
  setTimeout(() => {
    e(t());
  }, 50);
  function t() {
    if (M(o)) return !0;
    try {
      return window.getComputedStyle(document.documentElement).getPropertyValue(`--cssimported-${o}`).trim() !== "";
    } catch {
      return !1;
    }
  }
}), D = async (o) => {
  const { id: e, cssautoload: t = !0, csspath: r = "", debug: s = !1 } = o;
  if (t === !1 || r === !1) return;
  if (M(e) && !(typeof r == "string" && r.trim() !== "")) {
    s && console.log(`[${e}] CSS is already loaded, skipping`);
    return;
  }
  M(e) && typeof r == "string" && r.trim() !== "" && s && console.log(`[${e}] CSS is already loaded, also loading user-specified path: ${r}`);
  const u = [];
  typeof r == "string" && r.trim() !== "" && u.push(r);
  const y = R(e);
  if (y) {
    const g = `${y}${e}.css`;
    u.push(g);
  }
  const f = `plugin/${e}/${e}.css`;
  u.push(f);
  for (const g of u)
    try {
      await _(e, g);
      let p = "CSS";
      r && g === r ? p = "user-specified CSS" : y && g === `${y}${e}.css` ? p = "CSS (auto-detected from script location)" : p = "CSS (standard fallback)", s && console.log(`[${e}] ${p} loaded successfully from: ${g}`);
      return;
    } catch {
      s && console.log(`[${e}] Failed to load CSS from: ${g}`);
    }
  console.warn(`[${e}] Could not load CSS from any location`);
};
async function U(o, e) {
  if ("getEnvironmentInfo" in o && e) {
    const t = o, r = t.getEnvironmentInfo();
    if (await z(t.pluginId) && !(typeof e.csspath == "string" && e.csspath.trim() !== "")) {
      e.debug && console.log(`[${t.pluginId}] CSS is already imported, skipping`);
      return;
    }
    if ("cssautoload" in t.userConfig ? e.cssautoload : !r.isDevelopment)
      return D({
        id: t.pluginId,
        cssautoload: !0,
        csspath: e.csspath,
        debug: e.debug
      });
    r.isDevelopment && console.warn(
      `[${t.pluginId}] CSS autoloading is disabled in bundler environments. Please import the CSS manually, using import.`
    );
    return;
  }
  return D(o);
}
class V {
  // Flag to enable/disable all debugging output
  debugMode = !1;
  // Label to prefix all debug messages with
  label = "DEBUG";
  // Tracks the current depth of console groups for proper formatting
  groupDepth = 0;
  // Initializes the debug utility with custom settings.
  initialize(e, t = "DEBUG") {
    this.debugMode = e, this.label = t;
  }
  // Creates a new console group and tracks the group depth. 
  // Groups will always display the label prefix in their header.
  group = (...e) => {
    this.debugLog("group", ...e), this.groupDepth++;
  };
  // Creates a new collapsed console group and tracks the group depth.
  groupCollapsed = (...e) => {
    this.debugLog("groupCollapsed", ...e), this.groupDepth++;
  };
  // Ends the current console group and updates the group depth tracker.
  groupEnd = () => {
    this.groupDepth > 0 && (this.groupDepth--, this.debugLog("groupEnd"));
  };
  // Formats and logs an error message with the debug label. 
  // Error messages are always shown, even when debug mode is disabled.
  error = (...e) => {
    const t = this.debugMode;
    this.debugMode = !0, this.formatAndLog(console.error, e), this.debugMode = t;
  };
  // Displays a table in the console with the pluginDebug label.
  // Special implementation for console.table to handle tabular data properly.
  // @param messageOrData - Either a message string or the tabular data
  // @param propertiesOrData - Either property names or tabular data (if first param was message)
  // @param optionalProperties - Optional property names (if first param was message)
  table = (e, t, r) => {
    if (this.debugMode)
      try {
        typeof e == "string" && t !== void 0 && typeof t != "string" ? (this.groupDepth === 0 ? console.log(`[${this.label}]: ${e}`) : console.log(e), r ? console.table(t, r) : console.table(t)) : (this.groupDepth === 0 && console.log(`[${this.label}]: Table data`), typeof t == "object" && Array.isArray(t) ? console.table(e, t) : console.table(e));
      } catch (s) {
        console.error(`[${this.label}]: Error showing table:`, s), console.log(`[${this.label}]: Raw data:`, e);
      }
  };
  // Helper method that formats and logs messages with the pluginDebug label.
  // @param logMethod - The console method to use for logging
  // @param args - Arguments to pass to the console method
  formatAndLog = (e, t) => {
    if (this.debugMode)
      try {
        this.groupDepth > 0 ? e.call(console, ...t) : t.length > 0 && typeof t[0] == "string" ? e.call(console, `[${this.label}]: ${t[0]}`, ...t.slice(1)) : e.call(console, `[${this.label}]:`, ...t);
      } catch (r) {
        console.error(`[${this.label}]: Error in logging:`, r), console.log(`[${this.label}]: Original log data:`, ...t);
      }
  };
  // Core method that handles calling console methods with proper formatting.
  // - Adds label prefix to messages outside of groups
  // - Skips label prefix for messages inside groups to avoid redundancy
  // - Always adds label prefix to group headers
  // - Error messages are always shown regardless of debug mode
  // @param methodName - Name of the console method to call
  // @param args - Arguments to pass to the console method
  debugLog(e, ...t) {
    const r = console[e];
    if (!this.debugMode && e !== "error" || typeof r != "function") return;
    const s = r;
    if (e === "group" || e === "groupCollapsed") {
      t.length > 0 && typeof t[0] == "string" ? s.call(console, `[${this.label}]: ${t[0]}`, ...t.slice(1)) : s.call(console, `[${this.label}]:`, ...t);
      return;
    }
    if (e === "groupEnd") {
      s.call(console);
      return;
    }
    if (e === "table") {
      t.length === 1 ? this.table(t[0]) : t.length === 2 ? typeof t[0] == "string" ? this.table(t[0], t[1]) : this.table(t[0], t[1]) : t.length >= 3 && this.table(
        t[0],
        t[1],
        t[2]
      );
      return;
    }
    this.groupDepth > 0 ? s.call(console, ...t) : t.length > 0 && typeof t[0] == "string" ? s.call(console, `[${this.label}]: ${t[0]}`, ...t.slice(1)) : s.call(console, `[${this.label}]:`, ...t);
  }
}
const G = (o) => new Proxy(o, {
  get: (e, t) => {
    if (t in e)
      return e[t];
    const r = t.toString();
    if (typeof console[r] == "function")
      return (...s) => {
        e.debugLog(r, ...s);
      };
  }
}), S = G(new V()), m = {
  button: "always",
  display: "text",
  text: {
    copy: "Copy",
    copied: "Copied!"
  },
  plaintextonly: !0,
  timeout: 1e3,
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
  window: !1,
  tooltip: !0,
  iconsvg: {
    copy: "",
    copied: ""
  },
  cssautoload: !0,
  csspath: ""
}, Z = (o) => {
  if (!o || o === "transparent")
    return "white";
  const e = o.match(/\d+(\.\d+)?/g);
  if (!e || e.length < 3)
    return "white";
  const t = parseFloat(e[0]), r = parseFloat(e[1]), s = parseFloat(e[2]);
  return Math.sqrt(0.299 * (t * t) + 0.587 * (r * r) + 0.114 * (s * s)) > 127.5 ? "black" : "white";
}, K = async (o, e) => {
  if (e.plaintextonly) {
    let t;
    const r = o.querySelector("table.hljs-ln");
    r ? t = Array.from(r.querySelectorAll("td.hljs-ln-code")).map((s) => s.textContent).join(`
`) : t = o.innerText.replace(/^\s+|\s+$/g, ""), await navigator.clipboard.writeText(t);
  } else
    try {
      const t = o.cloneNode(!0);
      let r;
      const s = t.querySelector("table.hljs-ln");
      if (s) {
        const a = s.querySelectorAll("td.hljs-ln-numbers");
        for (const l of a)
          l.style.display = "none";
        r = Array.from(s.querySelectorAll("td.hljs-ln-code")).map((l) => l.textContent).join(`
`);
      } else
        r = t.innerText;
      const y = `
				<style>${Array.from(document.styleSheets).flatMap((a) => {
        try {
          return Array.from(a.cssRules);
        } catch {
          return [];
        }
      }).filter((a) => a.cssText.includes(".hljs") || a.cssText.includes("pre code") || a.cssText.includes("code[class") || a.cssText.includes(".language-") || a.cssText.includes("pre") || a.cssText.includes("code")).map((a) => a.cssText).join(`
`)}pre, code, .hljs, .hljs-ln-code { white-space: pre !important; font-family: monospace !important;}</style>
				<div>${t.outerHTML}</div>
			`, f = new Blob([y], { type: "text/html" }), g = new Blob([r], { type: "text/plain" }), p = new ClipboardItem({
        "text/html": f,
        "text/plain": g
      });
      await navigator.clipboard.write([p]);
    } catch (t) {
      console.error("Rich text clipboard error:", t), await navigator.clipboard.writeText(o.innerText);
    }
}, Y = (o, e) => {
  const t = o.querySelectorAll(".codeblock button[data-cc]:not(.code-copy-button)");
  for (const r of t)
    r.addEventListener("click", async () => {
      const s = r, y = s.closest(".codeblock")?.querySelector("code");
      if (!y || !(y instanceof HTMLElement)) {
        S.error("Could not find code element");
        return;
      }
      try {
        await K(y, e), J(s, e);
      } catch (f) {
        S.error("Error copying code:", f);
      }
    });
}, J = (o, e) => {
  o.textholder && (o.dataset.textOriginal = o.textholder.innerHTML, o.textholder.innerHTML = o.dataset.ccCopied || e.text.copied), o.setAttribute("disabled", "true"), setTimeout(() => {
    o.textholder && (o.dataset.ccDisplay !== "icons" || !o.dataset.ccDisplay) && (o.textholder.innerHTML = o.dataset.textOriginal || ""), delete o.dataset.textOriginal, o.removeAttribute("disabled");
  }, e.timeout);
}, O = {
  copy: '<svg aria-hidden="true" height="16" width="16" viewBox="0 0 16 16" version="1.1"><path d="M0 6.75C0 5.784.784 5 1.75 5h1.5a.75.75 0 0 1 0 1.5h-1.5a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-1.5a.75.75 0 0 1 1.5 0v1.5A1.75 1.75 0 0 1 9.25 16h-7.5A1.75 1.75 0 0 1 0 14.25Z"></path><path d="M5 1.75C5 .784 5.784 0 6.75 0h7.5C15.216 0 16 .784 16 1.75v7.5A1.75 1.75 0 0 1 14.25 11h-7.5A1.75 1.75 0 0 1 5 9.25Zm1.75-.25a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-7.5a.25.25 0 0 0-.25-.25Z"></path></svg>',
  copied: '<svg aria-hidden="true" height="16" viewBox="0 0 16 16" version="1.1" width="16"><path d="M15.7,2.8c0.4,0.4,0.4,1,0,1.4L6,13.9c-0.4,0.4-1,0.4-1.4,0L0.3,9.6c-0.4-0.4-0.4-1,0-1.4c0.4-0.4,1-0.4,1.4,0l3.6,3.6l9-9C14.7,2.4,15.3,2.4,15.7,2.8z"/></svg>'
}, Q = (o, e) => {
  const t = e.iconsvg?.copy && e.iconsvg.copy !== "" ? e.iconsvg.copy : O.copy, r = e.iconsvg?.copied && e.iconsvg.copied !== "" ? e.iconsvg.copied : O.copied, s = o.dataset.ccDisplay || e.display;
  s === "icons" || s === "both" ? (o.innerHTML = "<span></span>", o.textholder = o.querySelector("span"), o.insertAdjacentHTML("afterbegin", r), o.insertAdjacentHTML("afterbegin", t), o.dataset.ccDisplay === "icons" && e.tooltip && (o.textholder.style.display = "flex")) : (o.innerHTML = "<span></span>", o.textholder = o.querySelector("span")), o.textholder && (o.textholder.textContent = o.dataset.ccCopy ? o.dataset.ccCopy : e.text?.copy || "Copy");
}, X = (o, e) => {
  const t = (r, s) => r === void 0 || r === "" && String(s) === "0" ? !1 : String(r) !== String(s);
  t(e.style.copybg, m.style.copybg) && o.style.setProperty("--cc-copy-bg", e.style.copybg), t(e.style.copiedbg, m.style.copiedbg) && o.style.setProperty("--cc-copied-bg", e.style.copiedbg), t(e.style.copycolor, m.style.copycolor) && o.style.setProperty("--cc-copy-color", e.style.copycolor), t(e.style.copiedcolor, m.style.copiedcolor) && o.style.setProperty("--cc-copied-color", e.style.copiedcolor), t(e.style.scale, m.style.scale) && o.style.setProperty("--cc-scale", String(e.style.scale)), t(e.style.offset, m.style.offset) && o.style.setProperty("--cc-offset", String(e.style.offset)), t(e.style.radius, m.style.radius) && o.style.setProperty("--cc-radius", String(e.style.radius)), t(e.style.copyborder, m.style.copyborder) && e.style.copyborder !== "" && o.style.setProperty("--cc-copyborder", e.style.copyborder), t(e.style.copiedborder, m.style.copiedborder) && e.style.copiedborder !== "" && o.style.setProperty("--cc-copiedborder", e.style.copiedborder);
}, ee = async (o, e) => {
  const t = o.getRevealElement(), r = 'pre:not([data-cc="false"]) > code', u = document.querySelector("[name=generator]")?.getAttribute("content")?.includes("quarto") ?? !1;
  let y = [];
  if (t && (X(t, e), y = Array.from(t.querySelectorAll(r)).map((f) => f.parentElement).filter((f) => f instanceof HTMLPreElement)), y.length > 0 && t) {
    S.log(`${y.length} code blocks found`, y), t.insertAdjacentHTML("beforeend", '<pre><code class="hljs"></code></pre>');
    const f = t.lastElementChild, g = getComputedStyle(f.firstElementChild).backgroundColor;
    if (f.remove(), g) {
      const p = Z(g);
      t.style.setProperty("--cc-code-bg", g), t.style.setProperty("--cc-window-title-color", p);
    }
    for (const p of y) {
      let a = null, l = null;
      const w = p.parentElement;
      if (u && w?.matches(".sourceCode") ? (a = w, l = a, w.dataset.did = "quartoblock") : (l = p, w?.classList.contains("codeblock") || (a = document.createElement("div"), w?.insertBefore(a, p))), a && l && (a.classList.add("codeblock"), a.appendChild(p), (e.display === "icon" || e.display === "icons" || e.display === "both") && l && (l.dataset.ccDisplay = e.display), p.classList.contains("fragment") && (a.classList.add("fragment"), p.classList.remove("fragment")), p.querySelectorAll("code")[0]?.innerText)) {
        const v = l.getAttribute("data-cc-button") || (l.dataset.cc !== "false" ? l.dataset.cc : null), C = v === "false";
        let b = null;
        if (!C) {
          b = document.createElement("button"), b.dataset.cc = "true", b.title = "Copy to Clipboard";
          const c = v || e.button;
          c !== "always" && (b.dataset.cc = c);
          const h = ["ccCopy", "ccCopied", "ccDisplay"];
          for (const d of h)
            l.dataset[d] && (b.dataset[d] = l.dataset[d], delete l.dataset[d]);
          Q(b, e);
        }
        delete l.dataset.ccButton, l.dataset.cc !== "false" && delete l.dataset.cc;
        const n = l.getAttribute("data-cc-window");
        if (n !== "false" && (n !== null || e.window !== !1) && l.dataset.cc !== "false") {
          const c = l.dataset.ccWindow, h = typeof e.window == "object" ? e.window.title : void 0, d = l.getAttribute("data-cc-window-title") ?? (c && c !== "true" ? c : h ?? ""), A = l.getAttribute("data-cc-window-controls") || (typeof e.window == "object" ? e.window.controls : "color"), P = l.getAttribute("data-cc-window-controls-opacity") || (typeof e.window == "object" ? e.window.controlsOpacity?.toString() ?? "1" : "1"), q = l.getAttribute("data-cc-window-padding") || (typeof e.window == "object" ? e.window.padding : void 0) || "0.5rem";
          a.classList.add(`cc-controls-${A}`), a.style.setProperty(
            "--cc-window-controls-opacity",
            P
          ), a.style.setProperty("--cc-window-padding", q), delete l.dataset.ccWindow, delete l.dataset.ccWindowTitle, delete l.dataset.ccWindowControls, delete l.dataset.ccWindowControlsOpacity, delete l.dataset.ccWindowPadding;
          const $ = document.createElement("div");
          $.className = "cc-window-bar", $.innerHTML = `
							<div class="cc-window-left"><div class="cc-controls"><span></span><span></span><span></span></div></div>
							<div class="cc-window-title">${d}</div>
							<div class="cc-window-right"></div>
						`, b && $.querySelector(".cc-window-right")?.appendChild(b), a.dataset.ccWindow = "true", a.insertBefore($, p);
        } else
          b && l.dataset.cc !== "false" && a.insertBefore(b, p);
      }
    }
    Y(t, e);
  }
}, L = "copycode", te = async (o, e, t) => {
  S && t.debug && S.initialize(!0, L), await U(o, t), await ee(e, t);
}, oe = () => new k(L, te, m).createInterface();
export {
  oe as default
};
