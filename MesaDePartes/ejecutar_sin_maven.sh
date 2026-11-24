#!/bin/sh
# Compila y ejecuta el sistema SIN Maven (requiere solo JDK 17 o superior).
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac --release 17 -encoding UTF-8 -cp lib/sqlite-jdbc-3.46.1.3.jar -sourcepath src/main/java -d out \
  src/main/java/pe/edu/pppdiaz/mesapartes/Main.java || exit 1
java -cp "out:lib/sqlite-jdbc-3.46.1.3.jar:src/main/resources" pe.edu.pppdiaz.mesapartes.Main
