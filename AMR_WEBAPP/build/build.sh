#!/bin/bash

# Read paths from the configuration file
source config.properties

# Function to display help
display_help() {
    echo "-h, --help : Display this help message"
    echo "jars       : Build Java project AMR_API and make the jar"
    echo "copy       : Copy the WebContent to Tomcat webapps directory"
    echo "start      : Start Tomcat"
    echo "stop       : Stop Tomcat"
    echo "build      : Build the application"
}

# Function to stop Tomcat
stop() {
    echo "Stopping Tomcat"

    cd "$TOMCAT_DIR/bin" || exit
    ./shutdown.sh

    sleep 0.5
}

# Function to build the Java project and move the jar
jars() {
    echo "Building Java project AMR_API"

    cd "$WORKSPACE_DIR/AMR_API" || exit
    rm -rf bin/*

    find ./src -name "*.java" -print0 | xargs -0 javac -d ./bin -cp './jars/*'

    echo "Main-Class: fr.amr.Main" > Manifest.txt
    echo "Class-Path: $(find ./jars -name "*.jar" -printf "%p ") " >> Manifest.txt
    echo "Making jar amr-api.jar"
    jar cvfm amr-api.jar Manifest.txt -C bin/ . > /dev/null 2>&1

    sleep 0.5

    echo "Moving jar amr-api.jar to $WORKSPACE_DIR/AMR_WEBAPP/WebContent/WEB-INF/lib"
    mv amr-api.jar "$WORKSPACE_DIR/AMR_WEBAPP/WebContent/WEB-INF/lib/amr-api.jar"
    rm Manifest.txt

    sleep 0.5
}

# Function to copy the content of the directory
copy() {
    echo "Clearing content in $TOMCAT_DIR/webapps/$TOMCAT_CTX"

    cd "$TOMCAT_DIR/webapps/$TOMCAT_CTX" || exit
    rm -rf ./*

    cd "$WORKSPACE_DIR/AMR_WEBAPP/WebContent" || exit
    echo "Copying content from $WORKSPACE_DIR/AMR_WEBAPP/WebContent/ to $TOMCAT_DIR/webapps/$TOMCAT_CTX/"
    cp -r ./* "$TOMCAT_DIR/webapps/$TOMCAT_CTX"

    sleep 0.5
}

# Function to start Tomcat
start() {
    echo "Clearing Tomcat logs"
    cd "$TOMCAT_DIR/logs" || exit
    rm ./*.out
    rm ./*.log

    echo "Starting Tomcat"
    cd "$TOMCAT_DIR/bin" || exit
    ./startup.sh
}

# Function to build the application
build() {
    stop
    jars
    copy
    start
}

# Check if an argument was passed
if [ $# -eq 0 ]; then
    # No argument passed, call the build function by default
    build
elif [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    # Help argument passed, display help
    display_help
else
    # An argument was passed, call the corresponding function
    "$@"
fi