// ====================================================================
// cms-editors.js
// TinyMCE (+ Feather Icons) initialisieren & nach Fragment-Refresh re-initialisieren
// Robust gegen Race-Conditions (wartet auf window.tinymce)
// ====================================================================

window.cmsEditors = (function () {

    const SELECTORS = ['#applicationHtml', '#pressHtml', '#content'];
    const SELECTOR_STR = SELECTORS.join(',');

    function waitForTinyMCE(maxMs = 5000, stepMs = 100) {
        return new Promise((resolve, reject) => {
            const started = Date.now();
            (function tick() {
                if (window.tinymce && typeof tinymce.init === 'function' && typeof tinymce.remove === 'function') {
                    return resolve();
                }
                if (Date.now() - started > maxMs) {
                    return reject(new Error('TinyMCE not available in time'));
                }
                setTimeout(tick, stepMs);
            })();
        });
    }

    async function initTinymce(root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);

        try {
            await waitForTinyMCE();
        } catch (_) {
            // TinyMCE steht (noch) nicht zur Verfügung – einfach aussteigen
            return;
        }

        // Nur initialisieren, wenn mindestens ein Ziel-Element vorhanden ist
        if (!scope || !SELECTORS.some(sel => scope.querySelector(sel))) {
            return;
        }

        // Bestehende Editoren im Scope entfernen (TinyMCE 6/7)
        try {
            tinymce.remove(SELECTOR_STR);
        } catch (_) {}

        // Neu initialisieren
        try {
            tinymce.init({
                selector: SELECTOR_STR,
                plugins: 'advlist autolink lists link image charmap preview anchor table',
                toolbar: 'undo redo | formatselect | bold italic underline | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image table',
                menubar: false,
                branding: false,
                height: 600,
                image_uploadtab: false,
                content_style: 'body { font-family: Arial, sans-serif; font-size: 14px; }'
            });
        } catch (e) {
            console.warn('[cms-editors] tinymce.init failed:', e);
        }
    }

    function initFeather() {
        if (window.feather && typeof feather.replace === 'function') {
            try { feather.replace(); } catch(e) { console.warn('[cms-editors] feather.replace failed:', e); }
        }
    }

    async function init(root) {
        await initTinymce(root);
        initFeather();
    }

    return {
        init,
        reinit: init
    };
})();
