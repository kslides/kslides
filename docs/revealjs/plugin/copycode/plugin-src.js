const Plugin = () => {

    const getCodeBlocks = function (codeblocks, options) {

        const styleButton = function (button, codeblock) {
            let codeblockData = codeblock.dataset
            button.textContent = codeblockData.ccCopy ? codeblockData.ccCopy : options.copy;
            button.setAttribute("data-cc-copied", codeblockData.ccCopied ? codeblockData.ccCopied : options.copied);

            ["data-cc-copy", "data-cc-copied"].forEach(attribute => codeblock.removeAttribute(attribute));

            button.style.backgroundColor = options.copybg;
            button.style.color = options.copycolor;
        }

        const buildStructure = function (codeblock) {
            let wrapper = document.createElement("div");
            wrapper.classList.add("codeblock");
            codeblock.parentNode.insertBefore(wrapper, codeblock);
            wrapper.appendChild(codeblock);
            if (codeblock.classList.contains("fragment")) {
                wrapper.classList.add("fragment");
                codeblock.classList.remove("fragment")
            }
            let button = document.createElement("button");
            styleButton(button, codeblock);
            wrapper.insertBefore(button, codeblock);
        }

        codeblocks.forEach(codeblock => {
            if (codeblock.getAttribute("data-cc") && codeblock.getAttribute("data-cc") === "false") {
                return
            } else {
                if (!codeblock.parentNode.classList.contains("codeblock")) {
                    buildStructure(codeblock);
                }
            }
        });

        let clipboard = new ClipboardJS(".codeblock > button", {
            target: function (trigger) {
                return trigger.nextElementSibling.firstChild;
            }
        });

        function plaintextClipboard(copied) {
            const listener = function (ev) {
                let text = copied.replace(/^\s*\n/gm, "")
                ev.preventDefault();
                if (ev.clipboardData && ev.clipboardData.getData) {// Standards Compliant FIRST!
                    ev.clipboardData.setData('text/plain', text);
                } else if (window.clipboardData && window.clipboardData.getData) {// IE
                    window.clipboardData.setData('text/plain', text);
                }
            };
            document.addEventListener('copy', listener);
            document.execCommand('copy');
            document.removeEventListener('copy', listener);
        }


        clipboard.on("success", function (e) {

            if (options.plaintextonly == true) {
                plaintextClipboard(e.text);
            }

            let button = e.trigger;
            e.clearSelection();

            button.setAttribute("data-text-original", button.innerHTML);
            button.innerHTML = button.getAttribute("data-cc-copied");
            button.style.backgroundColor = options.copiedbg;
            button.style.color = options.copiedcolor;
            button.setAttribute("disabled", true);

            setTimeout(function () {
                button.style.backgroundColor = options.copybg;
                button.style.color = options.copycolor;
                button.innerHTML = button.getAttribute("data-text-original");
                button.removeAttribute("disabled");
            }, options.timeout);
        });
    }

    const init = function (deck) {

        let defaultOptions = {
            plaintextonly: true,
            timeout: 1000,
            copy: "Copy",
            copied: "Copied!",
            copybg: "orange",
            copiedbg: "green",
            copycolor: "black",
            copiedcolor: "white"
        };

        const defaults = function (options, defaultOptions) {
            for (let i in defaultOptions) {
                if (!options.hasOwnProperty(i)) {
                    options[i] = defaultOptions[i];
                }
            }
        }

        let options = deck.getConfig().copycode || {};
        defaults(options, defaultOptions);

        if (typeof ClipboardJS === "function") {
            let codeblocks = deck.getRevealElement().querySelectorAll("pre");

            if (codeblocks.length > 0) {
                getCodeBlocks(codeblocks, options);
            }
        } else {
            console.log("Clipboard.js did not load");
        }

    };

    return {
        id: 'copycode',
        init: init
    };
};

export default Plugin;