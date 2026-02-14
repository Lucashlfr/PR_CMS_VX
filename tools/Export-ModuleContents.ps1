# Save as: X:\workspace\PR_CMS\tools\Export-ModuleContents.ps1
param(
    [int]$Choice = -1,
    [string]$Root = 'X:\workspace\PR_CMS'
)

# --- Helpers ---------------------------------------------------------------

function Write-Header($text) {
    Write-Host ('=' * 80) -ForegroundColor DarkGray
    Write-Host $text -ForegroundColor Cyan
    Write-Host ('=' * 80) -ForegroundColor DarkGray
}

function Get-Modules($root) {
    Get-ChildItem -Path $root -Directory |
            Where-Object { $_.Name -match '^cms-' } |
            Sort-Object Name
}

function Ensure-Dir([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { [void](New-Item -ItemType Directory -Path $Path -Force) }
}

function Clear-Export([string]$Path) {
    if (Test-Path -LiteralPath $Path) {
        try {
            Get-ChildItem -LiteralPath $Path -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
        } catch {
            Write-Warning "Konnte Export-Ordner nicht vollständig leeren: $($_.Exception.Message)"
        }
    } else {
        Ensure-Dir $Path
    }
}

function Export-Module([string]$Name, [string]$ModuleRoot, [string]$OutRoot) {
    $exportDir = Join-Path $OutRoot $Name
    Ensure-Dir $exportDir

    Write-Header "Export: $Name  (Pfad: $ModuleRoot) -> $exportDir"

    $base = (Resolve-Path -LiteralPath $ModuleRoot).Path

    # 1) Alle .java-Dateien mit Inhalt
    try {
        Get-ChildItem -Path $ModuleRoot -Recurse -Filter *.java -File -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -notmatch '[\\\/]export[\\\/]' } |
                ForEach-Object {
                    "`n--- $($_.FullName) ---`n"
                    Get-Content -LiteralPath $_.FullName
                } | Out-File -FilePath (Join-Path $exportDir 'java_files.txt') -Encoding UTF8
    } catch { Write-Warning "java_files.txt: $($_.Exception.Message)" }

    # 2) Alle pom.xml-Dateien mit Inhalt
    try {
        Get-ChildItem -Path $ModuleRoot -Recurse -Filter pom.xml -File -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -notmatch '[\\\/]export[\\\/]' } |
                ForEach-Object {
                    "`n--- $($_.FullName) ---`n"
                    Get-Content -LiteralPath $_.FullName
                } | Out-File -FilePath (Join-Path $exportDir 'poms.txt') -Encoding UTF8
    } catch { Write-Warning "poms.txt: $($_.Exception.Message)" }

    # 3) Projektstruktur (neueste zuerst), relativ zum Modulpfad
    try {
        Get-ChildItem -Path $ModuleRoot -Recurse -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -notmatch '[\\\/]export[\\\/]' } |
                Sort-Object LastWriteTime -Descending |
                ForEach-Object {
                    $_.FullName -replace [Regex]::Escape($base), "" -replace "^[\\\/]", ""
                } | Out-File -FilePath (Join-Path $exportDir 'project_structure.txt') -Encoding UTF8
    } catch { Write-Warning "project_structure.txt: $($_.Exception.Message)" }

    # 4) Inhalte aus resources/static/** und resources/templates/** (robuste Suche)
    try {
        $resOut = Join-Path $exportDir 'resources-files.txt'
        $extensions = @('.html','.htm','.json','.txt','.xml','.svg','.md','.yml','.yaml','.properties','.css','.js')

        # Verzeichnisse finden, die GENAU '.../resources/static' oder '.../resources/templates' heißen
        $resourceDirs = Get-ChildItem -Path $ModuleRoot -Recurse -Directory -ErrorAction SilentlyContinue |
                Where-Object {
                    $_.FullName -notmatch '[\\\/]export[\\\/]' -and
                            ($_.FullName -match '[\\\/]resources[\\\/]static$' -or $_.FullName -match '[\\\/]resources[\\\/]templates$')
                }

        Write-Host ("[resources] Verzeichnisse: {0}" -f $resourceDirs.Count) -ForegroundColor DarkCyan

        $resourceFiles = foreach ($dir in $resourceDirs) {
            Get-ChildItem -Path $dir.FullName -Recurse -File -ErrorAction SilentlyContinue |
                    Where-Object { $extensions -contains $_.Extension.ToLower() }
        }

        Write-Host ("[resources] Dateien: {0}" -f ($resourceFiles | Measure-Object | Select-Object -ExpandProperty Count)) -ForegroundColor DarkCyan
        if (($resourceFiles | Measure-Object).Count -eq 0) {
            "Keine Ressourcen-Dateien gefunden unter '$ModuleRoot'." | Out-File -FilePath $resOut -Encoding UTF8
            return
        }

        $resourceFiles |
                Sort-Object FullName |
                ForEach-Object {
                    "`n--- $($_.FullName) ---`n"
                    # Für Textdateien passt -Raw (ganzer Inhalt); bei Bedarf auf Zeilenweise umstellen
                    Get-Content -LiteralPath $_.FullName -Raw
                } | Out-File -FilePath $resOut -Encoding UTF8
    } catch { Write-Warning "resources-files.txt: $($_.Exception.Message)" }
}


# --- Bootstrapping ---------------------------------------------------------

if (-not (Test-Path -LiteralPath $Root)) {
    throw "Root '$Root' nicht gefunden."
}

$exportRoot = Join-Path $Root 'export'
Ensure-Dir $exportRoot

function Show-Menu($modules) {
    Write-Host ""
    Write-Host "Wähle ein Modul per Nummer, '0' für ALLE, 'R' zum Aktualisieren, 'Q' zum Beenden:" -ForegroundColor Green
    Write-Host "  0) ALLE"
    for ($i = 0; $i -lt $modules.Count; $i++) {
        $idx = $i + 1
        Write-Host (" {0,2}) {1}" -f $idx, $modules[$i].Name)
    }
}

# --- Optional: einmaliger Param-Lauf, dann interaktive Schleife ------------

# 1x parametrisierter Lauf (wenn -Choice übergeben wurde)
if ($Choice -ge 0) {
    $modules = Get-Modules -root $Root
    if ($Choice -eq 0) {
        Clear-Export $exportRoot
        Export-Module -Name 'ALL' -ModuleRoot $Root -OutRoot $exportRoot
    } elseif ($Choice -ge 1 -and $Choice -le $modules.Count) {
        Clear-Export $exportRoot
        $sel = $modules[$Choice - 1]
        Export-Module -Name $sel.Name -ModuleRoot $sel.FullName -OutRoot $exportRoot
    } else {
        Write-Warning "Auswahl ${Choice} ist außerhalb des gültigen Bereichs (0..$($modules.Count))."
    }
    $Choice = -1
}

# Interaktive Endlosschleife (bis 'Q')
while ($true) {
    $modules = Get-Modules -root $Root
    if ($modules.Count -eq 0) {
        Write-Host "Keine Module unter '$Root' gefunden." -ForegroundColor Yellow
        break
    }

    Show-Menu -modules $modules
    $input = Read-Host "Deine Auswahl"

    switch -Regex ($input.Trim()) {
        '^(q|quit|exit)$' {
            Write-Host "Beendet." -ForegroundColor Cyan
            break
        }
        '^(r|reload)$' {
            Write-Host "Modulliste aktualisiert." -ForegroundColor Cyan
            continue
        }
        '^\d+$' {
            $num = [int]$input
            if ($num -eq 0) {
                Clear-Export $exportRoot
                Export-Module -Name 'ALL' -ModuleRoot $Root -OutRoot $exportRoot
            } elseif ($num -ge 1 -and $num -le $modules.Count) {
                $sel = $modules[$num - 1]
                Clear-Export $exportRoot
                Export-Module -Name $sel.Name -ModuleRoot $sel.FullName -OutRoot $exportRoot
            } else {
                Write-Warning "Ungültige Zahl. Gültig: 0..$($modules.Count)"
            }
            continue
        }
        default {
            Write-Warning "Unbekannte Eingabe. Nutze Zahl, 'R' oder 'Q'."
            continue
        }
    }
}
