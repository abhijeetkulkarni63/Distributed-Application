#Targets:
#  Default target: build project
#  clean:          remove all generated files.
#  submit:         build compress archive all project source files.

SRC_FILES = \
  src \
  Makefile \
  README

default:
	ant -buildfile src/build.xml

clean:
	ant -buildfile src/build.xml clean

run:
	ant -buildfile src/build.xml run
