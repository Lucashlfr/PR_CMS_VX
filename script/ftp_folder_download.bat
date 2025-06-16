@echo off
setlocal

:: FTP-Zugangsdaten
set FTP_HOST=29753-44055.pph-server.de
set FTP_USER=root
set FTP_PASS=2d9pmLhL26
set REMOTE_DIR=/opt/cms/cms_vx/uploads
set LOCAL_DIR=X:\workspace\CentralManagmentSystemX\cms_vx\uploads

:: WinSCP Skriptdatei erzeugen
set SCRIPT_FILE=winscp_script.txt

(
echo open ftp://%FTP_USER%:%FTP_PASS%@%FTP_HOST%
echo option transfer binary
echo lcd "%LOCAL_DIR%"
echo cd "%REMOTE_DIR%"
echo synchronize local "%LOCAL_DIR%" "%REMOTE_DIR%"
echo exit
) > %SCRIPT_FILE%

:: WinSCP Konsole ausführen
"C:\Program Files (x86)\WinSCP\WinSCP.com" /script=%SCRIPT_FILE%

:: Aufräumen
del %SCRIPT_FILE%
echo Ordner wurde heruntergeladen.
pause
