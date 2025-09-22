// flatpickr-init.js
// Initialisiert Flatpickr mit zwei Inputs (Start/End) und wählt den aktuellen Monat vor (Deutsch)

document.addEventListener('DOMContentLoaded', function() {
    // Flatpickr CSS-Anpassung für ausgewählte Tage
    const style = document.createElement('style');
    style.textContent = `
    .flatpickr-day.startRange,
    .flatpickr-day.endRange,
    .flatpickr-day.inRange,
    .flatpickr-day.selected {
      background-color: #003366 !important;
      color: #ffffff !important;
    }
  `;
    document.head.appendChild(style);

    const startInput = document.getElementById('litepickerStart');
    const endInput   = document.getElementById('litepickerEnd');
    if (!startInput || !endInput) {
        console.warn('Element #litepickerStart oder #litepickerEnd nicht gefunden');
        return;
    }

    // Eingabe per Tastatur blockieren
    [startInput, endInput].forEach(input => {
        input.readOnly = true;
        input.addEventListener('keydown', e => e.preventDefault());
        input.addEventListener('paste', e => e.preventDefault());
    });

    // Heutiges Datum ermitteln
    const today    = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDay  = new Date(today.getFullYear(), today.getMonth() + 1, 0);

    // Flatpickr mit Range-Plugin initialisieren
    flatpickr(startInput, {
        locale: 'de',                // Deutsch
        dateFormat: 'd.m.Y',         // Deutsches Datumsformat
        defaultDate: [firstDay, lastDay],
        mode: 'range',               // Range-Auswahl
        plugins: [new rangePlugin({ input: endInput })],
        onReady: function(selectedDates, dateStr, instance) {
            instance.setDate([firstDay, lastDay]);
        }
    });
});