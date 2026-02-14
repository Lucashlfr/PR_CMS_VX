/**
 * AJAX-Form-Handling mit kontextbezogenen Toasts – ohne automatisches Schließen.
 * Schließt Collapses/Modals nur, wenn *explizit* data-close="true" gesetzt ist.
 */
(function () {
    function toJSONSafe(text) {
        try { return JSON.parse(text); } catch { return null; }
    }

    function extractDomain(form, submitBtn) {
        const btnDomain = submitBtn?.dataset?.domain?.trim();
        if (btnDomain) return btnDomain;
        const formDomain = form?.dataset?.domain?.trim();
        if (formDomain) return formDomain;

        const header = form.closest('.card')?.querySelector('.card-header');
        if (header) {
            const raw = header.innerText || header.textContent || '';
            return raw.replace(/\s+/g, ' ').trim();
        }
        return '';
    }

    function pickSuccessMessage({ json, form, submitBtn }) {
        if (json?.message) return json.message;
        const btnSuccess = submitBtn?.getAttribute('data-success');
        if (btnSuccess) return btnSuccess;
        const formSuccess = form?.dataset?.success;
        if (formSuccess) return formSuccess;

        const domain = extractDomain(form, submitBtn);
        if (domain) {
            const dom = domain.replace(/["'«»]/g, '').trim();
            return `${dom} gespeichert.`;
        }
        return 'Änderungen gespeichert.';
    }

    function pickErrorMessage({ json, rawText, resp }) {
        if (json?.message) return json.message;
        if (json?.error)   return json.error;
        const txt = (rawText || '').toString().trim();
        if (txt) return txt;
        if (resp?.status) return `Fehler (${resp.status})`;
        return 'Speichern fehlgeschlagen.';
    }

    async function submitAjaxForm(form, submitBtn) {
        const method = (form.getAttribute('method') || 'POST').toUpperCase();
        const action = form.getAttribute('action') || location.href;
        const fd = new FormData(form);

        if (submitBtn?.name) fd.append(submitBtn.name, submitBtn.value || 'on');

        const origHtml = submitBtn ? submitBtn.innerHTML : null;
        const origDis  = submitBtn ? submitBtn.disabled  : false;
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
        }

        try {
            const resp = await fetch(action, {
                method,
                body: fd,
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                credentials: 'same-origin'
            });

            const ct = resp.headers.get('content-type') || '';
            const isJson = ct.includes('application/json');
            const raw   = await resp.text();
            const json  = isJson ? toJSONSafe(raw) : null;

            if (resp.ok && (json?.ok !== false)) {
                const msg = pickSuccessMessage({ json, form, submitBtn });
                if (window.personToast?.success) personToast.success(msg);

                // NUR schließen, wenn EXPLIZIT gewünscht:
                const shouldClose = (submitBtn?.dataset?.close || '').toLowerCase() === 'true';

                if (shouldClose) {
                    // Collapse optional schließen, wenn Selector vorhanden
                    const collapseSel = submitBtn?.dataset?.collapse;
                    if (collapseSel) {
                        try {
                            const el = document.querySelector(collapseSel);
                            if (el) {
                                const inst = bootstrap?.Collapse?.getOrCreateInstance(el, { toggle: false });
                                inst ? inst.hide() : el.classList.remove('show');
                            }
                        } catch (e) { console.warn('[person-ajax] collapse close failed', e); }
                    }

                    // Modal optional schließen
                    const modalSel = submitBtn?.dataset?.modal;
                    if (modalSel) {
                        try {
                            const el = document.querySelector(modalSel);
                            if (el) {
                                const m = bootstrap?.Modal?.getInstance(el) || new bootstrap.Modal(el);
                                m?.hide?.();
                            }
                        } catch (e) { console.warn('[person-ajax] modal close failed', e); }
                    }
                }

                // Nach Erfolg: offene Collapses bleiben offen (persistiert durch person-collapses.js)
                // Ein leichter Restore hilft nach partiellen DOM-Updates.
                if (window.personCollapses?.restoreIn) {
                    window.personCollapses.restoreIn(document);
                }
                return;
            }

            const err = pickErrorMessage({ json, rawText: raw, resp });
            if (window.personToast?.error) personToast.error(err); else alert(err);

        } catch (e) {
            const msg = 'Netzwerkfehler beim Speichern.';
            if (window.personToast?.error) personToast.error(msg); else alert(msg);
        } finally {
            if (submitBtn) { submitBtn.disabled = origDis; submitBtn.innerHTML = origHtml; }
        }
    }

    function onFormSubmit(e) {
        const form = e.target;
        if (!form.matches('[data-ajax="true"], .js-ajax-form')) return;

        let btn = form.querySelector('[data-ajax-submit]._clicked');
        if (!btn) btn = form.querySelector('[data-ajax-submit]') || form.querySelector('button[type="submit"]');

        e.preventDefault();
        submitAjaxForm(form, btn);
    }

    function markClickedButtons() {
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-ajax-submit]');
            if (!btn) return;
            const form = btn.form;
            if (form) form.querySelectorAll('[data-ajax-submit]._clicked').forEach(b => b.classList.remove('_clicked'));
            btn.classList.add('_clicked');
        }, true);
    }

    window.initPersonAjaxForms = function initPersonAjaxForms() {
        if (!window.__personAjaxBound) {
            document.addEventListener('submit', onFormSubmit, true);
            markClickedButtons();
            window.__personAjaxBound = true;
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        try { window.initPersonAjaxForms(); } catch (e) { console.error('[person-ajax] init failed:', e); }
    });
})();
