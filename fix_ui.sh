#!/bin/bash

# 1. Fix MainActivity.kt Scaffold insets
sed -i 's/Scaffold(/Scaffold(\n        contentWindowInsets = WindowInsets(0, 0, 0, 0),/g' app/src/main/java/com/quick/play/MainActivity.kt
sed -i '1s/^/import androidx.compose.foundation.layout.WindowInsets\n/' app/src/main/java/com/quick/play/MainActivity.kt

