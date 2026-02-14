// ====================================================================
// cms-charts.js
// - Bietet window.cmsCharts.init(scope) zum (Re-)Initialisieren von Charts
// - Erkennt das Rückmeldungs-Doughnut (#myPieChart) auch nach Fragment-Loads
// ====================================================================

(function () {
    function parseNum(scope, id) {
        const el = scope.querySelector("#" + id);
        if (!el) return 0;
        const raw = String(el.textContent || el.value || "0");
        const n = parseInt(raw.replace(/\D+/g, ""), 10);
        return Number.isFinite(n) ? n : 0;
    }

    function initResponsesChart(scope) {
        const canvas =
            scope.querySelector("#collapse-responses #myPieChart") ||
            scope.querySelector("#myPieChart");
        if (!canvas || typeof Chart === "undefined") return;

        // Doppelte Initialisierung vermeiden
        if (canvas.dataset.chartInit === "1") return;

        const mapping    = parseNum(scope, "mapping");
        const register   = parseNum(scope, "register");
        const cancelled  = parseNum(scope, "cancelled");
        const noFeedback = parseNum(scope, "noFeedback");

        const ctx = canvas.getContext("2d");
        new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: ["Ohne Mapping", "Angemeldet", "Abgemeldet", "Keine Rückmeldung"],
                datasets: [{
                    data: [mapping, register, cancelled, noFeedback],
                    backgroundColor: ["#f4a100","#00ac69","#e81500","#69707a"],
                    hoverBackgroundColor: ["#f4a100","#00ac69","#e81500","#69707a"],
                    hoverBorderColor: "rgba(234,236,244,1)"
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                legend: { display: true, position: "bottom" },
                tooltips: { enabled: true },
                cutoutPercentage: 70
            }
        });

        canvas.dataset.chartInit = "1";
    }

    window.cmsCharts = {
        init(root) {
            const scope =
                typeof root === "string" ? document.querySelector(root) : (root || document);
            if (!scope) return;
            initResponsesChart(scope);
        }
    };

    // Erstinitialisierung auf vollständiger Seite
    document.addEventListener("DOMContentLoaded", function () {
        if (window.Chart) window.cmsCharts.init(document);
    });
})();
