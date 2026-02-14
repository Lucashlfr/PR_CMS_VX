// ====================================================================
// cms-collapses.js
// Einheitliches Collapse-State-Handling (delegiert).
// - Speichert Zustand sofort beim Klick (präventiv)
// - Schreibt das echte Ergebnis bei shown/hidden (delegiert, kein Rebind nötig)
// - Stellt beim Laden / bfcache / History zuverlässig wieder her
// - Ignoriert Collapses innerhalb von Modals
// - Exportiert: window.cmsCollapses.restoreIn(scope)
// ====================================================================

(function () {
    const KEY = "openCollapseIds:" + location.pathname;

    // ---------- Storage ----------
    function readSet() {
        try { return new Set(JSON.parse(localStorage.getItem(KEY) || "[]")); }
        catch { return new Set(); }
    }
    function writeSet(set) {
        localStorage.setItem(KEY, JSON.stringify(Array.from(set)));
    }
    function saveNowFromDOM(root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);
        const ids = Array.from(scope.querySelectorAll(".collapse.show[id]"))
            .filter(el => !el.closest(".modal"))
            .map(el => el.id);
        localStorage.setItem(KEY, JSON.stringify(ids));
    }

    // ---------- Utils ----------
    function resolveTargetIdFromToggle(toggleEl) {
        if (!toggleEl) return null;
        const sel = toggleEl.getAttribute("data-bs-target") || toggleEl.getAttribute("href") || "";
        if (!sel) return null;
        const hash = sel.includes("#") ? sel.slice(sel.indexOf("#")) : sel;
        return hash.startsWith("#") ? hash.slice(1) : null;
    }
    function syncHeaderAria(id, root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);
        const header = scope.querySelector(
            `[data-bs-toggle="collapse"][data-bs-target="#${CSS.escape(id)}"], [data-bs-toggle="collapse"][href="#${CSS.escape(id)}"]`
        );
        if (header && !header.closest(".modal")) {
            header.setAttribute("aria-expanded", "true");
            header.classList.remove("collapsed");
        }
    }
    function openById(id, root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);
        const el = scope ? scope.querySelector("#" + CSS.escape(id)) : document.getElementById(id);
        if (!el || el.closest(".modal")) return;
        try {
            const inst = bootstrap?.Collapse?.getOrCreateInstance
                ? bootstrap.Collapse.getOrCreateInstance(el, { toggle: false })
                : null;
            if (inst) inst.show(); else el.classList.add("show");
        } catch { el.classList.add("show"); }
        syncHeaderAria(id, scope || document);
    }

    function restoreAll(root) {
        const set = readSet();
        set.forEach(id => openById(id, root));
    }

    // ---------- Delegierte Listener ----------
    function attachDelegatedListeners() {
        // A) Sofort beim Klick auf Header speichern (präventiv)
        document.addEventListener("click", function (e) {
            const toggle = e.target.closest && e.target.closest('[data-bs-toggle="collapse"]');
            if (!toggle || toggle.closest(".modal")) return;

            const id = resolveTargetIdFromToggle(toggle);
            if (!id) return;
            const target = document.getElementById(id);
            if (!target) return;

            // Klick passiert VOR dem Toggle. Ableitung:
            // aktuell offen? -> wird schließen => remove; sonst add.
            const set = readSet();
            const willOpen = !target.classList.contains("show");
            if (willOpen) set.add(id); else set.delete(id);
            writeSet(set);
        }, true);

        // B) Tatsächliche Ergebnisse delegiert mitschreiben (gilt auch für neu eingefügte DOM-Knoten)
        document.addEventListener("shown.bs.collapse", (ev) => {
            const el = ev.target;
            if (!el || !el.id || el.closest(".modal")) return;
            const set = readSet(); set.add(el.id); writeSet(set);
        });
        document.addEventListener("hidden.bs.collapse", (ev) => {
            const el = ev.target;
            if (!el || !el.id || el.closest(".modal")) return;
            const set = readSet(); set.delete(el.id); writeSet(set);
        });

        // C) Verlassen/Navi – finalen DOM-Zustand sichern
        const save = () => { try { saveNowFromDOM(document); } catch {} };
        window.addEventListener("beforeunload", save);
        window.addEventListener("pagehide", save);
        document.addEventListener("visibilitychange", () => {
            if (document.visibilityState === "hidden") save();
        });

        // D) History / bfcache – sauber wiederherstellen
        window.addEventListener("popstate", () => setTimeout(() => restoreAll(document), 0));
        window.addEventListener("pageshow", (e) => { if (e.persisted) restoreAll(document); });
    }

    // Public API
    window.cmsCollapses = {
        init() {
            restoreAll(document);
            attachDelegatedListeners();
        },
        // Nach Fragment-Refresh: im Scope wieder auf "offen" syncen (Listener sind delegiert → kein Rebind nötig)
        restoreIn(scopeRoot) {
            restoreAll(scopeRoot);
        }
    };

    // Auto-Init
    document.addEventListener("DOMContentLoaded", () => {
        try { window.cmsCollapses.init(); } catch (e) { console.error("[cms-collapses] init failed:", e); }
    });
})();
