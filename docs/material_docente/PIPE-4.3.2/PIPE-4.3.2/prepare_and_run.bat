@echo off
setlocal
cd /d "%~dp0"
echo Copiando JARs desde src\local-libs a ./lib ...
if not exist lib mkdir lib
for /r "%~dp0src\local-libs" %%f in (*.jar) do (
  copy /Y "%%f" "%~dp0lib\" >nul
)
echo Creando lista de fuentes y compilando...
dir /S /B src\main\java\*.java > sources.txt
javac -d Pipe -cp "lib\*" @sources.txt
if errorlevel 1 (
  echo.
  echo COMPILACION FALLIDA. Revisa que las dependencias esten en src\local-libs.
  pause
  exit /b 1
)
echo Compilacion OK. Lanzando launch.bat ...
call "%~dp0launch.bat"
echo.
echo Presione una tecla para salir.
pause >nul
