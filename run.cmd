@echo off
setlocal
cd /d "%~dp0"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto java_ready
for /d %%J in ("%USERPROFILE%\.jdks\*17*") do if exist "%%J\bin\java.exe" set "JAVA_HOME=%%J"
:java_ready
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
if exist "%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" (
  call "%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" -Dmaven.repo.local=.tools/repository %*
) else (
  call mvn %*
)
exit /b %errorlevel%
