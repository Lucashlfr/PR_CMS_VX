// ====================================================================
// event-files.js
// Drag&Drop + Multi-Upload für Dateien in #drop-zone
// Erwartet folgende Elemente innerhalb des "Dateien"-Fragments:
// - #drop-zone, #browse-btn, #file-input
// - hidden inputs: #target (UUID), #type (z. B. 'EVENT')
// - CSRF: <meta> ODER versteckte Inputs (siehe Fragment unten)
// - Tabelle wird nach Upload via /event/fragment/files?id=... neu geladen
// ====================================================================

(function () {
    const dropZone = document.getElementById("drop-zone");
    const browseBtn = document.getElementById("browse-btn");
    const fileInput = document.getElementById("file-input");
    const eventIdInput = document.getElementById("target");
    const typeInput = document.getElementById("type");
    const filesContainerSelector = "#files-container";

    if (!dropZone || !browseBtn || !fileInput || !eventIdInput || !typeInput) return;

    // Upload-Endpoint (Controller: POST /upload)
    const uploadEndpoint = "/upload";

    // CSRF aus <meta> oder Hidden-Inputs holen
    function getCsrf() {
        // Versuch 1: <meta>
        let token  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || null;
        let header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || null;

        // Versuch 2: Hidden-Inputs
        // - token:  #csrf_token (th:value="${_csrf.token}")
        // - header: #csrf_header (th:value="${_csrf.headerName}")
        // - param:  #csrf_param  (th:value="${_csrf.parameterName}") -> falls als Form-Field gesendet werden soll
        const hiddenToken  = document.getElementById("csrf_token")?.value || null;
        const hiddenHeader = document.getElementById("csrf_header")?.value || null;
        const hiddenParam  = document.getElementById("csrf_param")?.value || "_csrf";

        if (!token && hiddenToken) token = hiddenToken;
        if (!header && hiddenHeader) header = hiddenHeader;

        return {
            header: header || "X-CSRF-TOKEN",
            token: token,
            paramName: hiddenParam
        };
    }

    const refreshFiles = async () => {
        const eventId = eventIdInput.value;
        if (!eventId) return;
        const container = document.querySelector(filesContainerSelector);
        if (!container) return;
        try {
            const html = await fetch(`/event/fragment/files?id=${encodeURIComponent(eventId)}`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                credentials: 'same-origin'
            }).then(r => r.text());
            container.innerHTML = html;
            if (window.cmsCollapses?.restoreIn) window.cmsCollapses.restoreIn(filesContainerSelector);
            if (window.cmsEditors?.reinit) window.cmsEditors.reinit(filesContainerSelector);
            showToast("Dateiliste aktualisiert.", "success");
        } catch (e) {
            console.error(e);
            showToast("Fehler beim Aktualisieren der Dateiliste.", "error");
        }
    };

    const uploadFiles = async (files) => {
        if (!files || files.length === 0) return;
        const eventId = eventIdInput.value;
        const type = typeInput.value || "EVENT";

        const formData = new FormData();
        formData.append("target", eventId);
        formData.append("type", type);
        Array.from(files).forEach(f => formData.append("files", f));

        // CSRF sowohl als Header als auch (falls vorhanden) als Form-Param mitsenden
        const csrf = getCsrf();
        if (csrf?.token) {
            // Als Form-Parameter (Spring akzeptiert das parallel zu Headern)
            formData.append(csrf.paramName || "_csrf", csrf.token);
        }

        const headers = { 'X-Requested-With': 'XMLHttpRequest' };
        if (csrf?.token && csrf?.header) {
            headers[csrf.header] = csrf.token;
        }

        try {
            const res = await fetch(uploadEndpoint, {
                method: "POST",
                body: formData,
                headers,
                credentials: "same-origin" // sendet Cookies (JSESSIONID) mit
            });

            if (!res.ok) {
                let msg = `Upload fehlgeschlagen: ${res.status}`;
                try {
                    const ct = res.headers.get('content-type') || '';
                    if (ct.includes('application/json')) {
                        const j = await res.json();
                        if (j?.message) msg = j.message;
                    } else {
                        const t = await res.text();
                        if (t) msg = t;
                    }
                } catch(_) {}
                throw new Error(msg);
            }

            showToast("Upload abgeschlossen.", "success");
            await refreshFiles();
        } catch (e) {
            console.error(e);
            showToast(e?.message || "Upload fehlgeschlagen.", "error");
        }
    };

    // Drag&Drop
    ["dragenter", "dragover"].forEach(ev =>
        dropZone.addEventListener(ev, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.add("dragover");
        })
    );
    ["dragleave", "drop"].forEach(ev =>
        dropZone.addEventListener(ev, (e) => {
            e.preventDefault();
            e.stopPropagation();
            dropZone.classList.remove("dragover");
        })
    );
    dropZone.addEventListener("drop", (e) => {
        const dt = e.dataTransfer;
        const files = dt?.files;
        uploadFiles(files);
    });

    // Button "Durchsuchen"
    browseBtn.addEventListener("click", (e) => {
        e.preventDefault();
        fileInput.click();
    });

    // Auswahl via Dialog
    fileInput.addEventListener("change", () => {
        if (fileInput.files?.length) uploadFiles(fileInput.files);
        fileInput.value = ""; // reset
    });
})();
