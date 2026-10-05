@echo off
setlocal EnableExtensions
set "PROJECT_DIR=%~dp0"
set "FX_VERSION=21.0.2"
set "FX_ROOT=%USERPROFILE%\.m2\repository\org\openjfx"
set "OUTPUT_DIR=%PROJECT_DIR%target\evolab-classes"
set "SOURCE_LIST=%PROJECT_DIR%target\evolab-sources.txt"
set "FX_CP=%FX_ROOT%\javafx-base\%FX_VERSION%\javafx-base-%FX_VERSION%.jar;%FX_ROOT%\javafx-base\%FX_VERSION%\javafx-base-%FX_VERSION%-win.jar;%FX_ROOT%\javafx-graphics\%FX_VERSION%\javafx-graphics-%FX_VERSION%.jar;%FX_ROOT%\javafx-graphics\%FX_VERSION%\javafx-graphics-%FX_VERSION%-win.jar;%FX_ROOT%\javafx-controls\%FX_VERSION%\javafx-controls-%FX_VERSION%.jar;%FX_ROOT%\javafx-controls\%FX_VERSION%\javafx-controls-%FX_VERSION%-win.jar"

where javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK 21 javac was not found on PATH. Install a JDK and reopen this script.
    exit /b 1
)

for %%J in ("%FX_ROOT%\javafx-base\%FX_VERSION%\javafx-base-%FX_VERSION%.jar" "%FX_ROOT%\javafx-base\%FX_VERSION%\javafx-base-%FX_VERSION%-win.jar" "%FX_ROOT%\javafx-graphics\%FX_VERSION%\javafx-graphics-%FX_VERSION%.jar" "%FX_ROOT%\javafx-graphics\%FX_VERSION%\javafx-graphics-%FX_VERSION%-win.jar" "%FX_ROOT%\javafx-controls\%FX_VERSION%\javafx-controls-%FX_VERSION%.jar" "%FX_ROOT%\javafx-controls\%FX_VERSION%\javafx-controls-%FX_VERSION%-win.jar") do (
    if not exist "%%~J" goto missing_javafx
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"
if errorlevel 1 exit /b 1
(for /r "%PROJECT_DIR%src" %%F in (*.java) do @echo "%%~fF") > "%SOURCE_LIST%"
javac -encoding UTF-8 -cp "%FX_CP%" -d "%OUTPUT_DIR%" @"%SOURCE_LIST%"
if errorlevel 1 (
    echo Build failed. Review the compiler errors above.
    exit /b 1
)

if not exist "%OUTPUT_DIR%\visualization" mkdir "%OUTPUT_DIR%\visualization"
copy /y "%PROJECT_DIR%src\main\resources\visualization\evolab.css" "%OUTPUT_DIR%\visualization\evolab.css" >nul
pushd "%PROJECT_DIR%"
java -cp "%OUTPUT_DIR%;%FX_CP%" visualization.MainApp
set "APP_EXIT=%ERRORLEVEL%"
popd
exit /b %APP_EXIT%

:missing_javafx
echo JavaFX 21.0.2 jars are not present in "%FX_ROOT%".
echo Install Maven and run mvn javafx:run, or resolve the project dependencies first.
exit /b 1
