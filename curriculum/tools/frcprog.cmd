@echo off
rem ---------------------------------------------------------------------------
rem  frcprog — Windows launcher for tools\Frcprog.java
rem
rem  Same idea as the bash version: find a JDK, hand off to the single Java
rem  source file that is the actual tool. Java 11+ runs a .java file directly, so
rem  there is nothing to build.
rem
rem  Preference order: JAVA_HOME, then WPILib's bundled JDK, then PATH.
rem ---------------------------------------------------------------------------
setlocal

set "HERE=%~dp0"
set "PROJECT=%HERE%.."

set "JAVA_EXE="

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

if not defined PUBLIC set "PUBLIC=C:\Users\Public"

if not defined JAVA_EXE (
  if exist "%PUBLIC%\wpilib\2026\jdk\bin\java.exe" set "JAVA_EXE=%PUBLIC%\wpilib\2026\jdk\bin\java.exe"
)
if not defined JAVA_EXE (
  if exist "%PUBLIC%\wpilib\2025\jdk\bin\java.exe" set "JAVA_EXE=%PUBLIC%\wpilib\2025\jdk\bin\java.exe"
)
if not defined JAVA_EXE (
  where java >nul 2>nul && set "JAVA_EXE=java"
)

if not defined JAVA_EXE (
  echo No Java found.
  echo.
  echo frcprog runs on the JDK that WPILib installs. If you have not installed
  echo WPILib yet, that is lesson 0A and it is the right place to start:
  echo   https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html
  exit /b 1
)

cd /d "%PROJECT%"
"%JAVA_EXE%" "%HERE%Frcprog.java" %*
exit /b %ERRORLEVEL%
