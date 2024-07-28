#!/bin/bash
if [ -z "$1" ]; then
  echo "Please provide a port number."
  exit 1
fi

javac *.java
if [ $? -ne 0 ]; then
  echo "Compilation failed."
  exit 1
fi

java Server "$1"