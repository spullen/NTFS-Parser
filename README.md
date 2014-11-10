# NTFS Parser
Starter implementation of NTFS parsing originally written by [Scott Pullen](https://github.com/spullen) and updated by [Ian Thomas](https://github.com/ToxicBakery) for Intellij/Gradle.

# Install
From the root project directory run the gradle tasks 'clean build installApp' and optionally 'distZip' for packaging.

## Linux
./gradlew clean build installApp

## Windows
gradlew.bat clean build installApp

# Usage
The compbiled artifact is stored in <project root>/build/install/utility. The bin directory contains the files needed to execute the jar from the shell such that java -jar does not need to be used.

## Linux
./utility -h

## Windows
utility.bat -h
