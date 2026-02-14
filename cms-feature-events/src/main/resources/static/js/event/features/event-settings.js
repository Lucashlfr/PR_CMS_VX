// ====================================================================
// event-settings.js
// Für Settings-Formulare, bei denen die GANZE Seite neu geladen werden soll,
// statt Fragment-Refresh (data-reload="true").
//  - Zeigt während des Requests einen Spinner am geklickten Submit-Button
//  - Deaktiviert alle Submit-Buttons während des Requests
// ====================================================================

(function () {

    function startLoading(form, submitter) {
        const clicked = submitter || form.querySelector('[type="submit"], button:not([type])');
        const allSubmits = Array.from(form.querySelectorAll('button[type="submit"], input[type="submit"], button:not([type])'));

        const prevDisabledMap = new Map();
        allSubmits.forEach(btn => {
            prevDisabledMap.set(btn, btn.disabled);
            btn.disabled = true;
        });

        let clickedPrev = null;
        if (clicked) {
            clickedPrev = {
                html: clicked.innerHTML,
                minWidth: clicked.style.minWidth || "",
                ariaBusy: clicked.getAttribute("aria-busy")
            };
            const w = Math.ceil(clicked.getBoundingClientRect().width);
            if (w > 0) clicked.style.minWidth = w + "px";
            clicked.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading…';
            clicked.setAttribute("aria-busy", "true");
        }

        return { clicked, clickedPrev, prevDisabledMap };
    }

    function stopLoading(state) {
        if (!state) return;
        const { clicked, clickedPrev, prevDisabledMap } = state;
        if (prevDisabledMap) {
            prevDisabledMap.forEach((wasDisabled, btn) => { btn.disabled = wasDisabled; });
        }
        if (clicked && clickedPrev) {
            clicked.innerHTML = clickedPrev.html != null ? clickedPrev.html : clicked.innerHTML;
            clicked.style.minWidth = clickedPrev.minWidth || "";
            if (clickedPrev.ariaBusy == null) clicked.removeAttribute("aria-busy");
            else clicked.setAttribute("aria-busy", clickedPrev.ariaBusy);
        }
    }

    const forms = document.querySelectorAll('form[data-ajax="true"][data-reload="true"]');
    if (!forms.length) return;

    forms.forEach((form) => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            e.stopImmediatePropagation(); // verhindert doppelten Handler aus cms-ajax.js

            // TinyMCE synchronisieren
            if (window.tinymce && typeof tinymce.triggerSave === 'function') {
                try { tinymce.triggerSave(); } catch (_) {}
            }

            const action = form.getAttribute("action");
            const method = (form.getAttribute("method") || "post").toUpperCase();
            const formData = new FormData(form);

            const state = startLoading(form, e.submitter);

            try {
                const response = await fetch(action, { method, body: formData });
                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                showToast("Einstellungen gespeichert. Seite wird aktualisiert …", "success");

                // Kleiner Delay, damit Toast sichtbar ist
                setTimeout(() => { window.location.reload(); }, 400);
            } catch (err) {
                console.error(err);
                showToast("Fehler beim Speichern der Einstellungen.", "error");
            } finally {
                stopLoading(state);
            }
        }, true);
    });
})();
