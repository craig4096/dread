import sys
import os
import SceneExport

# get the target directory
target = None
for i in range(0, len(sys.argv)):
    if sys.argv[i] == "--target":
        if i < len(sys.argv)-1:
            target = sys.argv[i+1]
            
if target is not None:
    SceneExport.ExportAllFunction(target)
else:
    print("Please specify --target directory for export")
