@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Startup Script
@REM ----------------------------------------------------------------------------

@echo off
setlocal enabledelayedexpansion

set "WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_JAR%" (
    echo Maven Wrapper JAR not found: %WRAPPER_JAR%
    exit /b 1
)

if not exist "%WRAPPER_PROPERTIES%" (
    echo Maven Wrapper properties not found: %WRAPPER_PROPERTIES%
    exit /b 1
)

REM Read distributionUrl from properties
for /f "usebackq tokens=1,2 delims==" %%a in ("%WRAPPER_PROPERTIES%") do (
    if "%%a"=="distributionUrl" set "DISTRIBUTION_URL=%%b"
)

if not defined DISTRIBUTION_URL (
    echo distributionUrl not found in maven-wrapper.properties
    exit /b 1
)

REM Find Java
set "JAVA_EXE=java"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: JAVA_HOME is not set and no 'java' command could be found.
    exit /b 1
)

REM Set MAVEN_USER_HOME
set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
if defined M2_HOME set "MAVEN_USER_HOME=%M2_HOME%"

REM Set MAVEN_OPTS
set "MAVEN_OPTS=-Xmx1024m -XX:MaxMetaspaceSize=256m %MAVEN_OPTS%"

REM Execute Maven Wrapper
"%JAVA_EXE%" ^
    %MAVEN_OPTS% ^
    -classpath "%WRAPPER_JAR%" ^
    "-Dmaven.multiModuleProjectDirectory=%CD%" ^
    "-Dmaven.wrapper.distributionUrl=%DISTRIBUTION_URL%" ^
    "-Dmaven.user.home=%MAVEN_USER_HOME%" ^
    org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
