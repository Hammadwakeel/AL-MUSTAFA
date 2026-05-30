@echo off
echo ========================================
echo   AL Mustafa POS System - Launcher
echo ========================================
echo.

set FX_DIR=javafx-sdk-17
set FX_PATH=%CD%\%FX_DIR%\lib
set LIB_DIR=%CD%\lib

REM ===== Step 1: JavaFX SDK =====
if exist "%FX_DIR%\lib\javafx.controls.jar" (
    echo [OK] JavaFX SDK found!
    goto :check_libs
)

if exist "%FX_DIR%-win.zip" (
    echo [EXTRACT] Extracting JavaFX SDK...
    powershell -Command "Expand-Archive -Path '%FX_DIR%-win.zip' -DestinationPath '.' -Force"
    del "%FX_DIR%-win.zip" 2>nul
    goto :check_libs
)

echo.
echo [DOWNLOAD] JavaFX SDK not found. Downloading (~17MB)...
echo.

powershell -Command "try { Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'javafx-sdk-17.zip' -TimeoutSec 120 } catch { Write-Host 'Download failed'; exit 1 }"

if exist "javafx-sdk-17.zip" (
    echo [EXTRACT] Extracting JavaFX SDK...
    powershell -Command "Expand-Archive -Path 'javafx-sdk-17.zip' -DestinationPath '.' -Force"
    del "javafx-sdk-17.zip"
) else (
    echo [ERROR] JavaFX download failed!
    echo Please manually download from: https://gluonhq.com/products/javafx/
    echo Extract to current folder as 'javafx-sdk-17'
    pause
    exit /b 1
)

:check_libs
echo.

REM ===== Step 2: SQLite JDBC Driver =====
if exist "%LIB_DIR%\sqlite-jdbc.jar" (
    echo [OK] SQLite JDBC driver found!
    goto :check_slf4j
)

echo [DOWNLOAD] SQLite JDBC driver not found. Downloading...
mkdir "%LIB_DIR%" 2>nul

powershell -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.41.2.2/sqlite-jdbc-3.41.2.2.jar' -OutFile '%LIB_DIR%\sqlite-jdbc.jar' -TimeoutSec 60 } catch { Write-Host 'Download failed'; exit 1 }"

if not exist "%LIB_DIR%\sqlite-jdbc.jar" (
    echo [ERROR] SQLite download failed!
    pause
    exit /b 1
)

:check_slf4j
echo.

REM ===== Step 3: SLF4J Logger (optional) =====
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

REM ===== Step 4: Database Folder =====
if not exist "db" (
    echo [CREATE] Creating database folder...
    mkdir db
)

REM ===== Step 5: Launch =====
echo.
echo ========================================
echo   All dependencies ready!
echo ========================================
echo.
echo [START] Launching Hypermall POS System...
echo.

REM Using -jar with manifest Class-Path (no -cp needed, it's in MANIFEST.MF)
java --module-path "%FX_PATH%" --add-modules javafx.controls,javafx.fxml --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED -jar HypermallSystem.jar

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start application!
    echo.
    echo Checklist:
    echo   1. Java 17+ must be installed
    echo   2. Download Java from: https://adoptium.net/
    echo   3. Run: java -version
)
echo.
pause