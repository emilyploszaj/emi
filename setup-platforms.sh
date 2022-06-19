#!/bin/bash -ex

for p in platform-*; do
	ver=${p##*-}
	(
		cd $p
		./gradlew clean build
		cd build/libs
		cp ./*.jar include.jar
		sed -z 's/,\s*"environment".*$/\n}/' ../resources/main/fabric.mod.json > fabric.mod.json
		sed -i 's/"id": "emi"/"id": "emi-port"/' fabric.mod.json
		zip include.jar fabric.mod.json
		rm fabric.mod.json
	)
done