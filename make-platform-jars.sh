#!/bin/bash -ex

# I tried SO HARD to do this inside of Gradle, but Fabric Loom is bad and it's
# not possible. - unascribed

if [ -z "$2" ]; then
	echo "$0: usage: $0 <base jar> <base minecraft version>"
	exit 1
fi

abs=$(readlink -f "$1")
dirname=$(dirname "$abs")
basename=$(basename "$abs" .jar)

for p in platform-*; do
	ver=${p##*-}
	(
		cd $p
		./gradlew clean build
		mkdir -p build/jarContents
		cd build/jarContents
		unzip ../libs/*
		out="$dirname/$basename+$ver.jar"
		cp "$abs" "$out"
		sed -i 's/accessSchmidener/accessWidener/' fabric.mod.json
		zip -9 "$out" fabric.mod.json $(find -name '*.class')
	) &
done
wait
mv "$abs" "$dirname/$basename+$2.jar"
