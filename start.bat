@echo off
echo Starting AL Mustafa POS System...

REM Set the path to where you extracted JavaFX on the Windows machine
set FX_PATH="C:\javafx-sdk-17\lib"

REM Launch the application
java --module-path %FX_PATH% --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -jar HypermallSystem.jar

pause