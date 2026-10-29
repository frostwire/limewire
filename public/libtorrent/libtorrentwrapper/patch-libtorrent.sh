#!/bin/bash
CURRENT_PATH=`pwd`
SCRIPT_PATH="`dirname \"$0\"`"              # relative
SCRIPT_PATH="`( cd \"$SCRIPT_PATH\" && pwd )`"  # absolutized and normalized
PATCHES_PATH="$SCRIPT_PATH/patches"

echo "CURRENT_PATH: $CURRENT_PATH"
echo "SCRIPT_PATH: $SCRIPT_PATH"
echo "PATCHES_PATH: $PATCHES_PATH"

find $PATCHES_PATH -name *.patch -exec patch -p0 -i {} \;

