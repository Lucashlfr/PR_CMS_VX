// ====================================================================
// main.js
// ====================================================================

document.addEventListener("DOMContentLoaded", () => {
    try {
        // Collapses: einheitliches Init aus cms-collapses.js
        if (window.cmsCollapses && typeof window.cmsCollapses.init === "function") {
            window.cmsCollapses.init();
        }

        initAjaxForms();
        initModalBehavior();

        // Editoren + Feather einmalig beim Seitenstart
        if (window.cmsEditors && typeof window.cmsEditors.init === 'function') {
            window.cmsEditors.init(document);
        }

        console.info("[CMS] Base scripts initialisiert.");
    } catch (e) {
        console.error("[CMS] Initialisierung fehlgeschlagen:", e);
    }
});
