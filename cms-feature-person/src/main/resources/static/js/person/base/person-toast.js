<!-- person-toast.js -->
/**
 * Leichter Proxy auf window.CMSToast (falls Fragment geladen), sonst Fallback.
 * Lokal gekapselt fürs Person-Modul.
 */
window.personToast = (function () {
    function fallback(message, title) {
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
            setTimeout(() => toast.remove(), 3500);
        } catch (_) {
        }
    }

    function success(msg, title) {
        if (window.CMSToast?.success) return window.CMSToast.success(msg, title);
        fallback(msg, title || "Erfolg");
    }

    function error(msg, title) {
        if (window.CMSToast?.error) return window.CMSToast.error(msg, title);
        fallback(msg, title || "Fehler");
    }

    return {success, error};
})();
