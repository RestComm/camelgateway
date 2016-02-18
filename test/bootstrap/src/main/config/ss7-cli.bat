@echo off
rem -------------------------------------------------------------------------
rem Mobicents SS7 CLI Bootstrap Script for Win32
rem -------------------------------------------------------------------------

rem $Id: run.bat,v 1.5 2010/12/13 10:15:40 abhayani Exp $

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

pushd %DIRNAME%..
set SHELL_HOME=%CD%
echo ========
echo %SHELL_HOME%

popd

REM Add bin/native to the PATH if present
if exist "%SHELL_HOME%\native" set PATH=%SHELL_HOME%\native;%PATH%
if exist "%SHELL_HOME%\native" set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path="%PATH%"

REM Run section  - here we define node and default ip
set IP=127.0.0.1

rem Read all command line arguments

REM
REM The %ARGS% env variable commented out in favor of using %* to include
REM all args in java command line. See bug #840239. [jpl]
REM
REM set ARGS=
REM :loop
REM if [%1] == [] goto endloop
REM         set ARGS=%ARGS% %1
REM         shift
REM         goto loop
REM :endloop


set ARGS=""


:loop
 if [%1] == [] goto endloop
         set ARGS=%ARGS% %1
         shift
         goto loop
 :endloop


rem Find run.jar, or we can't continue

set RUNJAR=%SHELL_HOME%\bin\mobicents-ss7-shell.jar
if exist "%RUNJAR%" goto FOUND_RUN_JAR
echo Could not locate %RUNJAR%. Please check that you are in the
echo bin directory when running this script.
goto END

:FOUND_RUN_JAR

if not "%JAVA_HOME%" == "" goto ADD_TOOLS

set JAVA=java

echo JAVA_HOME is not set.  Unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
goto SKIP_TOOLS

:ADD_TOOLS

set JAVA=%JAVA_HOME%\bin\java

rem A full JDK with toos.jar is not required anymore since jboss web packages
rem the eclipse jdt compiler and javassist has its own internal compiler.
if not exist "%JAVA_HOME%\lib\tools.jar" goto SKIP_TOOLS

rem If exists, point to the JDK javac compiler in case the user wants to
rem later override the eclipse jdt compiler for compiling JSP pages.
set JAVAC_JAR=%JAVA_HOME%\lib\tools.jar

:SKIP_TOOLS

rem If JBOSS_CLASSPATH or JAVAC_JAR is empty, don't include it, as this will 
rem result in including the local directory in the classpath, which makes
rem error tracking harder.
if not "%JAVAC_JAR%" == "" set RUNJAR=%JAVAC_JAR%;%RUNJAR%
if "%SHELL_CLASSPATH%" == "" set RUN_CLASSPATH=%RUNJAR%
if "%RUN_CLASSPATH%" == "" set RUN_CLASSPATH=%SHELL_CLASSPATH%;%RUNJAR%

set SHELL_CLASSPATH=%RUN_CLASSPATH%

rem Setup JBoss specific properties
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name=%PROGNAME% 

rem Add -server to the JVM options, if supported
"%JAVA%" -version 2>&1 | findstr /I hotspot > nul
if not errorlevel == 1 (set JAVA_OPTS=%JAVA_OPTS% -server)

rem JVM memory allocation pool parameters. Modify as appropriate.
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m

rem profiler
rem set JAVA_OPTS=%JAVA_OPTS% -agentpath:C:\Users\kulikov\.netbeans\6.1\lib\deployed\jdk15\windows\profilerinterface.dll=C:\Users\kulikov\.netbeans\6.1\lib,5140
rem set JAVA_OPTS=%JAVA_OPTS% -agentpath:C:\Users\kulikov\.netbeans\6.1\lib\deployed\jdk16\windows\profilerinterface.dll=C:\Users\kulikov\.netbeans\6.1\lib,5140

rem With Sun JVMs reduce the RMI GCs to once per hour
set JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000

rem JPDA options. Uncomment and modify as appropriate to enable remote debugging.
rem set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y %JAVA_OPTS%

rem Setup the java endorsed dirs
set SHELL_ENDORSED_DIRS=%SHELL_HOME%\bin\lib


echo ===============================================================================
echo.
echo   Mobicents SS7 Management Shell Bootstrap Environment
echo.
echo   SHELL_HOME: %SHELL_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo   CLASSPATH: %SHELL_CLASSPATH%
echo   
echo   SHELL_ENDORSED_DIRS: %SHELL_ENDORSED_DIRS%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% -Djava.ext.dirs="%SHELL_ENDORSED_DIRS%" -classpath "%SHELL_CLASSPATH%" org.mobicents.ss7.management.console.Shell %*
rem if ERRORLEVEL 10 goto RESTART

:END
if "%NOPAUSE%" == "" pause

:END_NO_PAUSE