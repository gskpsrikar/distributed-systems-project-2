#!/bin/bash

# Code by Jordan Frimpter
# Command to grant permission to file to run [RUN THIS]: chmod +x build.sh

# code to remove carriage returns from files: sed -i -e 's/\r$//' <filename>

# compilation command [CHANGE THIS to match your project files]
bash cleanup.sh
bash cleanFiles.sh
git reset --hard HEAD
git pull
javac Main.java

echo done building