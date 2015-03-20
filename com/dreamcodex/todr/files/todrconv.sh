#!/bin/sh
if [ ! -d "conversions" ];
	then
		echo conversions dir does not exist, creating
		mkdir conversions
fi
if [ ! -d "conversions/$2" ];
	then
		echo $2 dir does not exist, creating
		mkdir "conversions/$2"
fi
if [ ! -d "conversions/$2/rawimages" ];
	then
		echo rawimages dir does not exist, creating
		mkdir "conversions/$2/rawimages"
fi
echo copying base files into module
cp -R _base/* conversions/$2/
java -jar todrconv.jar com.dreamcodex.todr.util.TODconverter $1 $2 conversions/$2/
