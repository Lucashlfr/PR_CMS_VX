// ====================================================================
// event-press.js
// Komfort für "Pressemeldung" (#pressHtml):
// - Ctrl/Cmd+S speichert
// - Auto-Height & Unsaved-Indicator
// ====================================================================

(function () {
    const textarea = document.getElementById("pressHtml");
    if (!textarea) return;
    const form = textarea.closest('form[data-ajax="true"]');
    let dirty = false;

    const autoSize = (el) => {
        el.style.height = "auto";
        el.style.height = el.scrollHeight + "px";
    };
    autoSize(textarea);

    textarea.addEventListener("input", () => {
        dirty = true;
        autoSize(textarea);
    });

    document.addEventListener("keydown", (e) => {
        const isSave = (e.key === "s" || e.key === "S") && (e.ctrlKey || e.metaKey);
        if (isSave && form) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    window.addEventListener("beforeunload", (e) => {
        if (!dirty) return;
        e.preventDefault();
        e.returnValue = "";
    });

    const targetSelector = form?.dataset?.refreshTarget;
    if (targetSelector) {
        const targetEl = document.querySelector(targetSelector);
        if (targetEl) {
            const mo = new MutationObserver(() => { dirty = false; });
            mo.observe(targetEl, { childList: true, subtree: true });
        }
    }
})();
