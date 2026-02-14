// ====================================================================
// event-messages.js
// Meldungs-Modal (#edit) UX:
// - Modal beim Öffnen zurücksetzen
// - Titel Pflichtfeld validieren (HTML5 required übernimmt das, aber wir zeigen extra Hinweis)
// - Nach erfolgreichem AJAX-Submit schließt cms-ajax bereits das Modal (data-modal="#edit")
// ====================================================================

(function () {
    const modalEl = document.getElementById("edit");
    if (!modalEl) return;

    const titleInput = modalEl.querySelector("#title");
    const form = modalEl.querySelector('form[data-ajax="true"], form'); // Fallback

    // Reset bei Modal-Open
    modalEl.addEventListener("show.bs.modal", () => {
        form?.reset();
        if (titleInput) titleInput.classList.remove("is-invalid");
    });

    // Zusätzliche Validierung
    form?.addEventListener("submit", (e) => {
        if (titleInput && !titleInput.value?.trim()) {
            e.preventDefault();
            titleInput.classList.add("is-invalid");
            titleInput.focus();
            showToast("Bitte Titel eingeben.", "warning");
        }
    });
})();
