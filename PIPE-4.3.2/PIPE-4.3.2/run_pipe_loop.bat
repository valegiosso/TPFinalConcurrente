@echo off
setlocal
cd /d "%~dp0"
echo Lanzando PIPE (loop: relanzar al cerrar)...
:loop
rem 1) Si hay compilación en target, ejecutar directamente con java y dependencias copiadas
if exist "%~dp0target\classes\" (
	echo Ejecutando desde target/classes + target/dependency
	java -cp "%~dp0target\classes;%~dp0target\dependency\*;%~dp0target\pipe.jar" Pipe
	goto :afterRun
)

rem 2) Intentar usar mvn (en PATH)
where mvn >nul 2>&1
if %errorlevel%==0 (
	echo Ejecutando via mvn exec:exec
	mvn -DskipTests exec:exec
	goto :afterRun
)

rem 3) Intentar ruta típica de Chocolatey a mvn
if exist "C:\ProgramData\chocolatey\lib\maven\apache-maven*\bin\mvn.cmd" (
	echo Ejecutando via Chocolatey mvn
	call "C:\ProgramData\chocolatey\bin\mvn" -DskipTests exec:exec
	goto :afterRun
)

rem 4) Fallback al script antiguo launch.bat
echo Ejecutando launch.bat (fallback)...
call "%~dp0launch.bat"

:afterRun
echo.
echo PRESIONE UNA TECLA para relanzar PIPE o Ctrl+C para salir.
pause >nul
goto loop
