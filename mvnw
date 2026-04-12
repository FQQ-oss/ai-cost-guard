#!/bin/sh
# Maven Wrapper script
set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    WRAPPER_URL=$(grep 'wrapperUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
    echo "Downloading Maven Wrapper from $WRAPPER_URL"
    if command -v curl > /dev/null 2>&1; then
        curl -sL -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    fi
fi

DIST_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
DIST_NAME=$(basename "$DIST_URL" .zip)
MAVEN_HOME="$HOME/.m2/wrapper/dists/$DIST_NAME"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven from $DIST_URL"
    mkdir -p "$HOME/.m2/wrapper/dists"
    TMPFILE=$(mktemp)
    if command -v curl > /dev/null 2>&1; then
        curl -sL -o "$TMPFILE" "$DIST_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$TMPFILE" "$DIST_URL"
    fi
    unzip -q "$TMPFILE" -d "$HOME/.m2/wrapper/dists"
    rm -f "$TMPFILE"
fi

export JAVA_HOME="${JAVA_HOME:-/c/Users/15105/.jdks/ms-17.0.18}"
export PATH="$JAVA_HOME/bin:$PATH"
exec "$MAVEN_HOME/bin/mvn" "$@"
