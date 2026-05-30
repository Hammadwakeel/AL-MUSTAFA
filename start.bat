@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   AL Mustafa POS System - Launcher
echo ========================================
echo.

set LIB_DIR=%CD%\lib
set FX_PATH=

REM ===== Search for JavaFX lib folder =====
echo [SCAN] Searching for JavaFX SDK...

if exist "javafx-sdk-17\lib\javafx.controls.jar" (
    set FX_PATH=%CD%\javafx-sdk-17\lib
    goto :verify_fx
)
if exist "javafx-sdk-17.0.2\lib\javafx.controls.jar" (
    set FX_PATH=%CD%\javafx-sdk-17.0.2\lib
    goto :verify_fx
)

REM Search for javafx.controls.jar in current folder
for /f "delims=" %%i in ('dir /s /b javafx.controls.jar 2^>nul') do (
    set "JAR_DIR=%%~dpi"
    REM Remove trailing backslash
    set "FX_PATH=!JAR_DIR:~0,-1!"
    goto :verify_fx
)

REM Download if not found
if exist "javafx-sdk-17.zip" (
    echo [EXTRACT] Extracting JavaFX SDK...
    powershell -Command "Expand-Archive -Path 'javafx-sdk-17.zip' -DestinationPath '.' -Force"
    del "javafx-sdk-17.zip" 2>nul
    goto :find_fx
)

echo.
echo [DOWNLOAD] JavaFX SDK not found. Downloading (~17MB)...
echo This may take a few minutes...
echo.

powershell -Command "try { Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'javafx-sdk-17.zip' -TimeoutSec 120 } catch { Write-Host 'Download failed'; exit 1 }"

if exist "javafx-sdk-17.zip" (
    echo [EXTRACT] Extracting JavaFX SDK...
    powershell -Command "Expand-Archive -Path 'javafx-sdk-17.zip' -DestinationPath '.' -Force"
    del "javafx-sdk-17.zip"
    goto :find_fx
) else (
    echo [ERROR] JavaFX download failed!
    echo.
    echo MANUAL SETUP:
    echo 1. Go to: https://gluonhq.com/products/javafx/
    echo 2. Download SDK 17 for Windows
    echo 3. Extract to current folder
    echo 4. Rename folder to: javafx-sdk-17.0.2
    pause
    exit /b 1
)

:find_fx
REM Try to find lib folder after extraction
if exist "javafx-sdk-17.0.2\lib\javafx.controls.jar" (
    set FX_PATH=%CD%\javafx-sdk-17.0.2\lib
    goto :verify_fx
)

REM Search recursively
for /f "delims=" %%i in ('dir /s /b javafx.controls.jar 2^>nul') do (
    set "JAR_DIR=%%~dpi"
    set "FX_PATH=!JAR_DIR:~0,-1!"
    goto :verify_fx
)

echo [ERROR] Could not locate JavaFX library!
pause
exit /b 1

:verify_fx
if not exist "%FX_PATH%\javafx.controls.jar" (
    echo [ERROR] javafx.controls.jar not found at: %FX_PATH%
    pause
    exit /b 1
)
echo [OK] JavaFX SDK found at: %FX_PATH%
echo.

REM ===== SQLite JDBC Driver =====
if exist "%LIB_DIR%\sqlite-jdbc.jar" (
    echo [OK] SQLite JDBC driver found!
    goto :check_slf4j
)

echo [DOWNLOAD] SQLite JDBC driver not found. Downloading...
mkdir "%LIB_DIR%" 2>nul

powershell -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.41.2.2/sqlite-jdbc-3.41.2.2.jar' -OutFile '%LIB_DIR%\sqlite-jdbc.jar' -TimeoutSec 60 } catch { }"

if not exist "%LIB_DIR%\sqlite-jdbc.jar" (
    echo [ERROR] SQLite download failed!
    pause
    exit /b 1
)

:check_slf4j
echo.

if exist "%LIB_DIR%\slf4j-api.jar" (
    echo [OK] SLF4J logger found!
) else (
    echo [DOWNLOAD] SLF4J API not found. Downloading...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar' -OutFile '%LIB_DIR%\slf4j-api.jar' -TimeoutSec 30 } catch { }"
)

if exist "%LIB_DIR%\slf4j-simple.jar" (
    echo [OK] SLF4J Simple binding found!
) else (
    echo [DOWNLOAD] SLF4J Simple not found. Downloading...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar' -OutFile '%LIB_DIR%\slf4j-simple.jar' -TimeoutSec 30 } catch { }"
)

REM ===== Database Folder =====
if not exist "db" (
    mkdir db
)

REM ===== Launch =====
echo.
echo ========================================
echo   All dependencies ready!
echo ========================================
echo.
echo [START] Launching Hypermall POS System...
echo.

REM Build java command with proper quoting
set JAVA_CMD=java --module-path "%FX_PATH%" --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/com.sun.javafx.text=ALL-UNNAMED --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED -jar HypermallSystem.jar

REM Execute the command
%JAVA_CMD%

if errorlevel 1 (
    echo.
    echo [ERROR] Application failed to start!
    echo.
    echo Checklist:
    echo   1. Java 17+ must be installed: java -version
    echo   2. Download Java from: https://adoptium.net/
    echo   3. Make sure Java is in PATH
)
echo.
pause