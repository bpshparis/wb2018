#!/bin/bash

JAVAC=$(command -v javac)

if [ "${JAVAC}" == "" ]; then
	echo "ERROR !!! javac not found PATH. Exiting..."
	exit 1
fi

rm -rf WebContent/WEB-INF/classes/*

javac -cp wlp/*:WebContent/WEB-INF/lib/* -d WebContent/WEB-INF/classes/ src/com/ekaly/web/*.java src/com/ekaly/test/*.java src/com/ekaly/tools/*.java       

exit 0
