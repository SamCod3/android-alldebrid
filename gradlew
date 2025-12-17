#!/bin/sh

#
# Gradle wrapper script for UNIX
#

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Get the directory of this script
APP_HOME="$( cd "$( dirname "$0" )" && pwd )"

# Build classpath
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Run Gradle
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
