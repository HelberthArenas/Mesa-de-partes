@echo off
REM Compila y ejecuta el sistema SIN Maven (requiere solo JDK 17 o superior en el PATH).
chcp 65001 >nul
if not exist out mkdir out
javac --release 17 -encoding UTF-8 -cp lib\sqlite-jdbc-3.46.1.3.jar -sourcepath src\main\java -d out src\main\java\pe\edu\pppdiaz\mesapartes\Main.java
if errorlevel 1 (
  echo Error de compilacion. Verifique que tiene instalado el JDK 17 o superior.
  pause
  exit /b 1
)
java -cp "out;lib\sqlite-jdbc-3.46.1.3.jar;src\main\resources" pe.edu.pppdiaz.mesapartes.Main
