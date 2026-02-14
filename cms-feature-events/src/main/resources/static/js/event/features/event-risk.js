// ====================================================================
// event-risk.js
// UX für das Risikoanalyse-Formular:
// - Scrollt bei Validierungsfehlern zum ersten invaliden Feld
// - Ctrl/Cmd+S speichert
// ====================================================================

(function () {
    const form = document.querySelector('#collapse-risk form[data-ajax="true"]');
    if (!form) return;

    // Ctrl/Cmd + S
    document.addEventListener("keydown", (e) => {
        const isSave = (e.key === "s" || e.key === "S") && (e.ctrlKey || e.metaKey);
        if (isSave) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    // Fokus auf erstes invalides Feld
    form.addEventListener("submit", (e) => {
        if (!form.checkValidity()) {
            e.preventDefault();
            e.stopPropagation();
            const firstInvalid = form.querySelector(":invalid");
            if (firstInvalid) {
                firstInvalid.focus({ preventScroll: true });
                firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
                showToast("Bitte Pflichtfelder prüfen.", "warning");
            }
        }
    }, true);
})();
