#!/usr/bin/env bash

mkdir tmp
javac -cp 'dist/*' -d tmp `find src -iname '*.java'`
cd tmp
jar cvf odinms.jar *
cd ..
mv tmp/odinms.jar dist
rm -rf tmp
