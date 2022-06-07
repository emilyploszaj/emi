#!/bin/bash -ex

# I tried SO HARD to do this inside of Gradle, but Fabric Loom is bad and it's
# not possible. - unascribed

if [ -z "$3" ]; then
	echo "$0: usage: $0 <base jar> <base source jar> <base minecraft version>"
	exit 1
fi

abs=$(readlink -f "$1")
dirname=$(dirname "$abs")
basename=$(basename "$abs" .jar)

absSrc=$(readlink -f "$2")

zip -d "$absSrc" dev/emi/emi/EmiPort.java

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
	outSrc="$dirname/$basename+$ver-sources.jar"
	cp "$absSrc" "$outSrc"
done
wait
mv "$abs" "$dirname/$basename+$3.jar"
mv "$absSrc" "$dirname/$basename+$3-sources.jar"
