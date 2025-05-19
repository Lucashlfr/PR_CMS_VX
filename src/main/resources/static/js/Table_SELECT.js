$(document).ready(function () {
    $('table.display').DataTable({

        dom: 'Blfrtip',
        buttons: ['selectAll', 'selectNone'],
        language: {
            "decimal": ",",
            "thousands": ".",
            "infoPostFix": "",
            "infoFiltered": "(gefiltert aus insgesamt _MAX_ Einträgen)",
            "loadingRecords": "Bitte warten Sie - Daten werden geladen ...",
            "lengthMenu": "Anzeigen von _MENU_ Einträgen",
            "paginate": {
                "first": "Erste",
                "last": "Letzte",
                "next": "Nächste",
                "previous": "Zurück"
            },
            "processing": "Verarbeitung läuft ...",
            "search": "Suche:",
            "searchPlaceholder": "Suchbegriff",
            "zeroRecords": "Keine Daten! Bitte ändern Sie Ihren Suchbegriff.",
            "emptyTable": "Keine Einträge zum Anzeigen vorhanden",
            "aria": {
                "sortAscending": ": aktivieren, um Spalte aufsteigend zu sortieren",
                "sortDescending": ": aktivieren, um Spalte absteigend zu sortieren"
            },
            "info": "Zeigt _START_ bis _END_  von _TOTAL_ Einträgen",
            "infoEmpty": "Keine Einträge zum Anzeigen vorhanden",
            buttons: {
                selectAll: "Alles auswählen",
                selectNone: "Keine auswählen"
            },
            "select": {
                "rows": {
                    _: '%d Zeilen ausgewählt',
                    0: 'Zeile anklicken um auszuwählen',
                    1: 'Eine Zeile ausgewählt'
                }
            }
        },
        select: {
            style: 'multi'
        }
    });
});