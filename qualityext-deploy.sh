#!/bin/bash

DEFAULT="3333"
PORT="$1"

git clone https://github.com/OpenRefine/OpenRefine.git
cd ./OpenRefine/extensions/
git clone https://github.com/diachron/quality-extension.git
cd ..
ant -buildfile ./build.xml
cd ./extensions/quality-extension/
ant -buildfile ./build.xml
cd ../..


if [[ -z "$PORT" ]] 
then
	./refine -i 0.0.0.0
else 
	./refine -i 0.0.0.0 -p $PORT
fi
