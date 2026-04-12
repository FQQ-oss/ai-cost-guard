@echo off
setlocal

set "JAVA_HOME=%USERPROFILE%\.jdks\ms-17.0.18"
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

for /f "tokens=2 delims==" %%a in ('findstr "distributionUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set "DIST_URL=%%a"
for %%i in ("%DIST_URL%") do set "DIST_NAME=%%~ni"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven from %DIST_URL%
    mkdir "%USERPROFILE%\.m2\wrapper\dists" 2>nul
    powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TEMP%\maven.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
    del "%TEMP%\maven.zip"
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
