#!/bin/bash

echo Batch Exporting....

BLENDER_PATH=$1
SOURCE_PATH=$2
TARGET_PATH=$3

for mapdir in $SOURCE_PATH*/; do
    if [ $mapdir != "*/" ]; then
        mapname=$(basename $mapdir)
    
        echo Exporting $mapname Map....
        blendfile="$mapdir$mapname.blend"
        
        $BLENDER_PATH./blender $blendfile --background --python CommandLineExport.py --target $TARGET_PATH
    fi
done

