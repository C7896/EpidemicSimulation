#!/bin/bash

# Check if an argument is provided
if [ -z "$1" ]; then
    echo "Usage: scripts/Run_JUnit.sh <PATH_TO_JUNIT_PLATFORM_CONSOLE_STANDALONE_1.10.2.JAR>"
    exit 1
fi

# Assign the argument to a variable
PATH_TO_JUNIT=$1

# Check if the file exists
if [ ! -f "$PATH_TO_JUNIT" ]; then
    echo "JUnit file does not exist."
    exit 1
fi

# Run JUnit5 tests
java -jar "$PATH_TO_JUNIT" --class-path out --scan-class-path