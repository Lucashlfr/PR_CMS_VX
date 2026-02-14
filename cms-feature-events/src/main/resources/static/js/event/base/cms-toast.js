// ====================================================================
// cms-toast.js (Proxy auf das Fragment window.CMSToast)
// ====================================================================

function showToast(message, type = "info", title) {
    // Wenn CMSToast aus dem Fragment vorhanden ist, nutze es
    if (window.CMSToast) {
        const t = (type || "info").toLowerCase();
        if (t === "success") return window.CMSToast.success(message, title);
        if (t === "error"   || t === "danger") return window.CMSToast.error(message, title);
        if (t === "warning") return window.CMSToast.error(message, title || "Hinweis"); // optional: eigener Warn-Typ
        return window.CMSToast.success(message, title || "Info");
    }

    // Fallback (falls Fragment mal nicht geladen ist)
    try {
        const toast = document.createElement("div");
        toast.className = "toast show";
        toast.style.position = "fixed";
        toast.style.bottom = "1rem";
        toast.style.right = "1rem";
        toast.style.zIndex = "1080";
        toast.innerHTML = `
            <div class="toast-header">
                <strong class="me-auto">${title || "Info"}</strong>
                <button type="button" class="btn-close ms-2 mb-1" onclick="this.closest('.toast').remove()"></button>
            </div>
            <div class="toast-body">${message}</div>
        `;
        (document.getElementById("toast-stack") || document.body).appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    } catch(_) {}
}
