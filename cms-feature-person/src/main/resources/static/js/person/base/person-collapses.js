/**
 * Speichert offene Collapses pro Seite/Query in localStorage und stellt sie wieder her.
 * Schließt NIE selbstständig – steuert nur State.
 */
(function () {
    function buildKey() {
        const url = new URL(location.href);
        const keep = new URLSearchParams();
        const src = new URLSearchParams(url.search);
        ['id','s','q','startDate','endDate'].forEach(k => { if (src.has(k)) keep.set(k, src.get(k)); });
        if ([...keep.keys()].length === 0) { [...src.keys()].sort().forEach(k => keep.set(k, src.get(k))); }
        const norm = keep.toString();
        return "person:openCollapseIds:" + url.pathname + (norm ? "?" + norm : "");
    }
    const KEY = buildKey();

    function readSet() {
        try { return new Set(JSON.parse(localStorage.getItem(KEY) || "[]")); }
        catch { return new Set(); }
    }
    function writeSet(set) {
        try { localStorage.setItem(KEY, JSON.stringify(Array.from(set))); } catch {}
    }

    function resolveIdFromToggle(t) {
        if (!t) return null;
        const sel = t.getAttribute("data-bs-target") || t.getAttribute("href") || "";
        if (!sel) return null;
        const hash = sel.includes("#") ? sel.slice(sel.indexOf("#")) : sel;
        return hash.startsWith("#") ? hash.slice(1) : null;
    }

    function syncHeaderAria(id, root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);
        const hdr = scope.querySelector(
            `[data-bs-toggle="collapse"][data-bs-target="#${CSS.escape(id)}"], [data-bs-toggle="collapse"][href="#${CSS.escape(id)}"]`
        );
        if (hdr && !hdr.closest(".modal")) {
            hdr.setAttribute("aria-expanded", "true");
            hdr.classList.remove("collapsed");
        }
    }

    function openById(id, root) {
        const scope = typeof root === "string" ? document.querySelector(root) : (root || document);
        const el = scope ? scope.querySelector("#" + CSS.escape(id)) : document.getElementById(id);
        if (!el || el.closest(".modal")) return;
        try {
            const inst = bootstrap?.Collapse?.getOrCreateInstance(el, { toggle: false });
            inst ? inst.show() : el.classList.add("show");
        } catch { el.classList.add("show"); }
        syncHeaderAria(id, scope || document);
    }

    function restoreAll(root) {
        readSet().forEach(id => openById(id, root));
    }

    let raf = null;
    function scheduleWrite(set) {
        if (raf) cancelAnimationFrame(raf);
        raf = requestAnimationFrame(() => writeSet(set));
    }

    function attach() {
        document.addEventListener("click", (e) => {
            const toggle = e.target.closest?.('[data-bs-toggle="collapse"]');
            if (!toggle || toggle.closest(".modal")) return;

            const id = resolveIdFromToggle(toggle);
            const target = id && document.getElementById(id);
            if (!target) return;

            const set = readSet();
            const willOpen = !target.classList.contains("show");
            if (willOpen) set.add(id); else set.delete(id);
            scheduleWrite(set);
        }, true);

        document.addEventListener("shown.bs.collapse", (ev) => {
            const el = ev.target;
            if (!el?.id || el.closest(".modal")) return;
            const set = readSet(); set.add(el.id); scheduleWrite(set);
        });
        document.addEventListener("hidden.bs.collapse", (ev) => {
            const el = ev.target;
            if (!el?.id || el.closest(".modal")) return;
            const set = readSet(); set.delete(el.id); scheduleWrite(set);
        });

        const saveNow = () => {
            try {
                const ids = Array.from(document.querySelectorAll(".collapse.show[id]"))
                    .filter(el => !el.closest(".modal")).map(el => el.id);
                localStorage.setItem(KEY, JSON.stringify(ids));
            } catch {}
        };
        window.addEventListener("beforeunload", saveNow);
        window.addEventListener("pagehide", saveNow);
        document.addEventListener("visibilitychange", () => { if (document.visibilityState === "hidden") saveNow(); });

        window.addEventListener("popstate", () => setTimeout(() => restoreAll(document), 0));
        window.addEventListener("pageshow", (e) => { if (e.persisted) restoreAll(document); });
    }

    window.personCollapses = {
        init() { restoreAll(document); attach(); },
        restoreIn(scopeRoot) { restoreAll(scopeRoot); }
    };

    document.addEventListener("DOMContentLoaded", () => {
        try { window.personCollapses.init(); } catch (e) { console.error("[person-collapses] init failed:", e); }
    });
})();
