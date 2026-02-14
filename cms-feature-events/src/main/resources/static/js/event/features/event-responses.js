// ====================================================================
// event-responses.js (Chart.js 2.9.4) – robust & fragment-tauglich
// - Eindeutige Canvas-ID: #responsesChart
// - Neuaufbau bei DOM-Load und beim Öffnen von #collapse-responses
// - Vorherige Instanz wird clean zerstört
// ====================================================================

(function () {
    function getNumber(id) {
        const el = document.getElementById(id);
        if (!el) return 0;
        const raw = String(el.textContent || el.value || "0");
        const n = parseInt(raw.replace(/\D+/g, ""), 10);
        return Number.isFinite(n) ? n : 0;
    }

    function buildChart() {
        if (typeof Chart === "undefined") return;

        const canvas = document.getElementById("responsesChart");
        if (!canvas) return;

        // evtl. alten Chart sauber zerstören
        if (canvas._chart) {
            try { canvas._chart.destroy(); } catch (_) {}
            canvas._chart = null;
        }

        const mapping   = getNumber("mapping");
        const register  = getNumber("register");
        const cancelled = getNumber("cancelled");
        const noFeedback= getNumber("noFeedback");

        const data = {
            labels: ["Ohne Mapping", "Angemeldet", "Abgemeldet", "Keine Rückmeldung"],
            datasets: [{
                data: [mapping, register, cancelled, noFeedback],
                backgroundColor: ["#f4a100", "#00ac69", "#e81500", "#69707a"],
                hoverBackgroundColor: ["#f4a100", "#00ac69", "#e81500", "#69707a"],
                hoverBorderColor: "rgba(234,236,244,1)"
            }]
        };

        const options = {
            responsive: true,
            maintainAspectRatio: false,
            legend: { display: false, position: "bottom" },
            tooltips: {
                enabled: true,
                backgroundColor: "rgb(255,255,255)",
                bodyFontColor: "#858796",
                borderColor: "#dddfeb",
                borderWidth: 1,
                xPadding: 15,
                yPadding: 15,
                displayColors: false,
                caretPadding: 10
            },
            cutoutPercentage: 70
        };

        const ctx = canvas.getContext("2d");
        canvas._chart = new Chart(ctx, { type: "doughnut", data, options });
    }

    // Beim Laden versuchen (falls Panel sichtbar)
    document.addEventListener("DOMContentLoaded", buildChart);

    // Wenn das Panel geöffnet wird → korrektes Re-Layout, falls vorher hidden
    document.addEventListener("shown.bs.collapse", function (e) {
        if (e.target && e.target.id === "collapse-responses") {
            buildChart();
        }
    });
})();
