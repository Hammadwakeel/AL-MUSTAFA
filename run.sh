#!/bin/bash

# 1. Configuration
JDK_BIN="/usr/lib/jvm/java-17-openjdk-amd64/bin"
FX_PATH=/usr/share/openjfx/lib
BIN_PATH="bin"
LIB_PATH="lib/*"

# 2. Prepare Build Environment
rm -rf $BIN_PATH
mkdir -p $BIN_PATH

# 3. Sync Resources
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* $BIN_PATH/
fi

# 4. Integrity Check for JARs
if [ ! -s lib/slf4j-simple.jar ]; then
    echo "❌ Error: lib/slf4j-simple.jar is empty or missing. Please re-download it."
    exit 1
fi

echo "🔍 Compiling Hypermall System..."
find src/main/java -name "*.java" > sources.txt

# 5. Compile (Forced to Java 17)
$JDK_BIN/javac --module-path $FX_PATH --add-modules javafx.controls,javafx.fxml \
      -cp "$LIB_PATH" -d $BIN_PATH @sources.txt

# 6. Package and Run
if [ $? -eq 0 ]; then
    echo "📦 Packaging into JAR..."
    
    echo "Main-Class: com.hypermall.Launcher" > manifest.txt
    echo "Class-Path: $(ls lib/*.jar | tr '\n' ' ')" >> manifest.txt
    
    $JDK_BIN/jar cvfm HypermallSystem.jar manifest.txt -C $BIN_PATH .
    rm manifest.txt
    
    echo "✅ Success! HypermallSystem.jar has been created."
    
    # ---------------------------------------------------------
    # 7. LAUNCH THE APP (Combined Fix: Env Strip + Absolute Path)
    # ---------------------------------------------------------
    echo "🚀 Launching Hypermall System..."
    env -u LD_LIBRARY_PATH $JDK_BIN/java \
        --module-path $FX_PATH \
        --add-modules javafx.controls,javafx.fxml \
        --enable-native-access=javafx.graphics \
        -jar HypermallSystem.jar
    
else
    echo "❌ Compilation failed."
fi

rm sources.txt