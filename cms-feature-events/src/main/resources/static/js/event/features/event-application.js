// ====================================================================
// event-application.js
// Komfort für den "Anmeldetext"-Bereich (#applicationHtml)
// - Ctrl/Cmd+S speichert das Formular
// - Unsaved-Indicator & Auto-Height
// ====================================================================

(function () {
    const textarea = document.getElementById("applicationHtml");
    if (!textarea) return;

    const form = textarea.closest('form[data-ajax="true"]');
    let dirty = false;

    // Auto-Height
    const autoSize = (el) => {
        el.style.height = "auto";
        el.style.height = el.scrollHeight + "px";
    };
    autoSize(textarea);
    textarea.addEventListener("input", () => {
        dirty = true;
        autoSize(textarea);
    });

    // Ctrl/Cmd + S => Submit
    document.addEventListener("keydown", (e) => {
        const isSave = (e.key === "s" || e.key === "S") && (e.ctrlKey || e.metaKey);
        if (isSave && form) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    // Warnung beim Verlassen bei ungespeicherten Änderungen
    window.addEventListener("beforeunload", (e) => {
        if (!dirty) return;
        e.preventDefault();
        e.returnValue = "";
    });

    // Nach AJAX-Speichern Dirty-Flag zurücksetzen (über Beobachtung von DOM-Updates)
    const targetSelector = form?.dataset?.refreshTarget;
    if (targetSelector) {
        const targetEl = document.querySelector(targetSelector);
        if (targetEl) {
            const mo = new MutationObserver(() => { dirty = false; });
            mo.observe(targetEl, { childList: true, subtree: true });
        }
    }
})();
