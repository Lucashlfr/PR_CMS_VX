// ====================================================================
// cms-ajax.js
// AJAX-Formularverarbeitung mit data-Attributen
//  - Zeigt während des Requests einen Spinner am geklickten Submit-Button
//  - Deaktiviert alle Submit-Buttons während des Requests
//  - Schließt optional ein Collapse / Modal
//  - Aktualisiert optional ein Fragment und reinitialisiert UI
// ====================================================================

(function () {

    function startLoading(form, submitter) {
        const clicked = submitter || form.querySelector('[type="submit"], button:not([type])');
        const allSubmits = Array.from(form.querySelectorAll('button[type="submit"], input[type="submit"], button:not([type])'));

        // Merke Disabled-Status aller Submit-Buttons
        const prevDisabledMap = new Map();
        allSubmits.forEach(btn => {
            prevDisabledMap.set(btn, btn.disabled);
            btn.disabled = true;
        });

        // Layout-Stabilisierung für den geklickten Button
        let clickedPrev = null;
        if (clicked) {
            clickedPrev = {
                html: clicked.innerHTML,
                minWidth: clicked.style.minWidth || "",
                ariaBusy: clicked.getAttribute("aria-busy")
            };
            // Breite fixieren, damit kein Layout-Sprung entsteht
            const w = Math.ceil(clicked.getBoundingClientRect().width);
            if (w > 0) clicked.style.minWidth = w + "px";

            // Spinner + Text (belässt bestehende Klassen wie "btn btn-yellow")
            clicked.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading…';
            clicked.setAttribute("aria-busy", "true");
        }

        return { clicked, clickedPrev, prevDisabledMap };
    }

    function stopLoading(state) {
        if (!state) return;
        const { clicked, clickedPrev, prevDisabledMap } = state;

        // Submit-Buttons zurücksetzen
        if (prevDisabledMap) {
            prevDisabledMap.forEach((wasDisabled, btn) => {
                btn.disabled = wasDisabled;
            });
        }

        // Geklickten Button zurücksetzen
        if (clicked && clickedPrev) {
            clicked.innerHTML = clickedPrev.html != null ? clickedPrev.html : clicked.innerHTML;
            clicked.style.minWidth = clickedPrev.minWidth || "";
            if (clickedPrev.ariaBusy == null) clicked.removeAttribute("aria-busy");
            else clicked.setAttribute("aria-busy", clickedPrev.ariaBusy);
        }
    }

    function isJsonResponse(res) {
        const ct = res.headers.get('content-type') || '';
        return ct.includes('application/json');
    }

    function reinitScope(scopeSelector) {
        if (!scopeSelector) return;
        // Editoren & Icons im ersetzten Bereich wieder initialisieren
        if (window.cmsEditors && typeof window.cmsEditors.reinit === 'function') {
            window.cmsEditors.reinit(scopeSelector);
        }
        // Collapse-State im Scope wiederherstellen (delegierte Listener nötig)
        if (window.cmsCollapses && typeof window.cmsCollapses.restoreIn === 'function') {
            window.cmsCollapses.restoreIn(scopeSelector);
        }
        // Charts optional
        if (window.cmsCharts && typeof window.cmsCharts.init === 'function') {
            window.cmsCharts.init(scopeSelector);
        }
    }

    function getCsrf() {
        const metaToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const metaHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        const inputToken = document.querySelector('input[name="_csrf"]')?.value;
        return { header: metaHeader || 'X-CSRF-TOKEN', token: metaToken || inputToken || null };
    }

    window.initAjaxForms = function initAjaxForms() {
        document.querySelectorAll('form[data-ajax="true"]').forEach(form => {
            form.addEventListener("submit", async (e) => {
                e.preventDefault();

                // TinyMCE-Inhalte synchronisieren
                if (window.tinymce && typeof tinymce.triggerSave === 'function') {
                    try { tinymce.triggerSave(); } catch (_) {}
                }

                const action = form.getAttribute("action");
                const method = (form.getAttribute("method") || "post").toUpperCase();

                const successMsg   = form.dataset.success || "Erfolgreich gespeichert.";
                const refreshTarget= form.dataset.refreshTarget;
                const refreshUrl   = form.dataset.refreshUrl;
                const collapse     = form.dataset.collapse;
                const modal        = form.dataset.modal;

                const headers = { 'X-Requested-With': 'XMLHttpRequest' };
                const csrf = getCsrf();
                if (csrf.token) headers[csrf.header] = csrf.token;

                const formData = new FormData(form);

                // Loading-UI starten (mit Spinner am geklickten Button)
                const state = startLoading(form, e.submitter);

                try {
                    const res = await fetch(action, { method, body: formData, headers });
                    if (!res.ok) {
                        const msg = isJsonResponse(res) ? (await res.json()).message : await res.text();
                        showToast(msg || "Speichern fehlgeschlagen.", "error");
                        return;
                    }

                    let ok = true, msg = successMsg;
                    if (isJsonResponse(res)) {
                        const data = await res.json();
                        ok  = data?.ok !== false;         // default: true
                        msg = data?.message || msg;
                    }

                    if (!ok) {
                        showToast(msg || "Speichern fehlgeschlagen.", "error");
                        return;
                    }

                    showToast(msg, "success");

                    // Collapse gezielt schließen
                    if (collapse) {
                        const collapseEl = document.querySelector(collapse);
                        if (collapseEl) {
                            let bsCollapse = bootstrap.Collapse.getInstance(collapseEl);
                            if (!bsCollapse) bsCollapse = new bootstrap.Collapse(collapseEl, { toggle: false });
                            try { bsCollapse.hide(); } catch (_) {}
                        }
                    }

                    // Modal schließen
                    if (modal) {
                        const modalEl = document.querySelector(modal);
                        if (modalEl) {
                            let bsModal = bootstrap.Modal.getInstance(modalEl);
                            if (!bsModal) bsModal = new bootstrap.Modal(modalEl, { backdrop: true });
                            try { bsModal.hide(); } catch (_) {}
                        }
                    }

                    // Fragment-Refresh
                    if (refreshTarget && refreshUrl) {
                        const html = await fetch(refreshUrl, { headers: { 'X-Requested-With': 'XMLHttpRequest' } }).then(r => r.text());
                        const targetEl = document.querySelector(refreshTarget);
                        if (targetEl) {
                            targetEl.innerHTML = html;
                            reinitScope(refreshTarget);
                        }
                    }

                } catch (error) {
                    console.error("AJAX-Fehler:", error);
                    showToast("Fehler beim Speichern. Bitte erneut versuchen.", "error");
                } finally {
                    stopLoading(state);
                }
            });
        });
    };

})();
