#!/bin/bash
if [ "$1" == "" ]; then
	echo Missing parameter
	exit -1
fi

if [ -f testdata/$1 ]; then
	img=$1
	rm -rf completed/* incoming/* processing/* && sleep 2 && cp testdata/$img incoming/$img.tmp && mv incoming/$img.tmp incoming/$img
else
	echo File does not exist
	exit -1
fi
