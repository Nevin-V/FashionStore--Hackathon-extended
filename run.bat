@echo off
cd /d "%~dp0"
title Fashion Store
echo ====================================================================
echo      FASHION STORE MANAGEMENT SYSTEM
echo ====================================================================
echo.
echo PREREQUISITE: MySQL Server must be running on port 3306.
echo Make sure you have run schema.sql in MySQL before first launch.
echo.
echo Compiling Java source files...
if not exist out mkdir out
javac -cp ".;mysql-connector-j-8.4.0.jar" -d out src\*.java
echo.
echo Launching Fashion Store...
echo (Close the application window or press Ctrl+C to stop)
echo.
java -cp ".;out;mysql-connector-j-8.4.0.jar" FashionStoreApp

:: This we have referred ai to make this 'run.bat' file, so we can run application in one click.
