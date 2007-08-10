# simple makefile for now

CC = gcc
JAVAC = javac

all: libopendiamondjava.so src/edu/cmu/cs/diamond/opendiamond/glue/OpenDiamond.java
	ant

opendiamond_wrap.c src/edu/cmu/cs/diamond/opendiamond/glue/OpenDiamond.java: opendiamond.i
	mkdir src/edu/cmu/cs/diamond/opendiamond/glue
	swig -Wall -java $$(pkg-config opendiamond --cflags-only-I) -package edu.cmu.cs.diamond.opendiamond.glue -outdir src/edu/cmu/cs/diamond/opendiamond/glue $<


libopendiamondjava.so: opendiamond_wrap.c
	$(CC) -fno-strict-aliasing -m32 -shared $$(pkg-config opendiamond --cflags --libs) -g -O2 -Wall -o $@ $<

clean:
	ant clean
	$(RM) libopendiamondjava.so opendiamond_wrap.c src/edu/cmu/cs/diamond/opendiamond/glue/*.java *~ bin




.PHONY: all clean
