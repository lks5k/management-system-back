#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# -----------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
# -----------------------------------------------------------------

# OS specific support.
darwin=false
case "$(uname)" in
  Darwin*) darwin=true;;
esac

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

if [ ! -x "$JAVACMD" ] && ! command -v "$JAVACMD" > /dev/null 2>&1 ; then
  echo "Error: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
  exit 1
fi

# Detect wrapperJarFile path
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-.}"
wrapperJarFile="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ -r "$wrapperJarFile" ]; then
  exec "$JAVACMD" \
    -classpath "$wrapperJarFile" \
    "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
else
  echo "Downloading Maven Wrapper jar..." >&2
  wrapperUrl=$(grep wrapperUrl "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d= -f2)
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$wrapperJarFile" "$wrapperUrl" || { echo "Failed to download." >&2; exit 1; }
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$wrapperJarFile" "$wrapperUrl" || { echo "Failed to download." >&2; exit 1; }
  else
    echo "Neither curl nor wget is available." >&2; exit 1
  fi
  exec "$JAVACMD" \
    -classpath "$wrapperJarFile" \
    "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
fi
