@echo off

rem Check if an argument is provided
if "%1"=="" (
    echo Usage: scripts/Run_JUnit.bat ^<PATH_TO_JUNIT_PLATFORM_CONSOLE_STANDALONE_1.10.2.JAR^>
    exit /b 1
)

rem Assign the argument to a variable
set PATH_TO_JUNIT=%1

rem Check if the directory exists
if not exist "%PATH_TO_JUNIT%" (
    echo JUnit directory does not exist.
    exit /b 1
)

rem Run JUnit5 tests
java -jar %PATH_TO_JUNIT% --class-path out --scan-class-path