
@echo off

set MAIN_CLASS=#MAIN_CLASS#

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd "%DIRNAME%.."
set "ROOT_DIR=%CD%"
popd

set LIB_DIR=%ROOT_DIR%\lib
set APP_DIR=%ROOT_DIR%\app

set APP_CP=%APP_DIR%\*

if not exist %APP_DIR% (
  if exist %ROOT_DIR%\..\classes (
    set APP_DIR=%ROOT_DIR%\..\classes
    set APP_CP=%ROOT_DIR%\..\classes
  )
)

set SERVER_OPTS=

set CLASSPATH=%APP_CP%;%LIB_DIR%\*

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  if not exist "%JAVA_HOME%" (
    echo JAVA_HOME "%JAVA_HOME%" path doesn't exist
    goto END
   ) else (
     if not exist "%JAVA_HOME%\bin\java.exe" (
       echo "%JAVA_HOME%\bin\java.exe" does not exist
       goto END_NO_PAUSE
     )
      echo Setting JAVA property to "%JAVA_HOME%\bin\java"
    set "JAVA=%JAVA_HOME%\bin\java"
  )
)

set SERVER_OPTS=%SERVER_OPTS% %JAVA_OPTS% -cp %CLASSPATH%
"%JAVA%" %SERVER_OPTS% %MAIN_CLASS%
