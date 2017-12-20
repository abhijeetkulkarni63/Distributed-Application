.PHONY: all java clean

all: java

java:   controller   branch

clean:
	rm -f controller branch
	rm -f *.class
	rm -f javac_middleman Controller*.class Branch*.class BranchAddress*.class
	rm -f protoc_middleman Bank.java Intro.java

protoc_middleman: bank.proto intro.proto
	protoc --java_out=. bank.proto
	protoc --java_out=. intro.proto
	@touch protoc_middleman

javac_middleman: Controller.java Branch.java protoc_middleman
	javac -cp /home/phao3/protobuf/protobuf-3.4.0/java/core/target/protobuf.jar Controller.java Branch.java Bank.java Sender.java Receiver.java State.java BranchSetup.java SetupReceiver.java Intro.java
	@touch javac_middleman

controller: javac_middleman
	@echo "Writing shortcut script controller..."
	@echo '#! /bin/sh' > controller
	@echo 'java -classpath .:$$CLASSPATH Controller "$$@"' >> controller
	@chmod +x controller

branch: javac_middleman
	@echo "Writing shortcut script branch..."
	@echo '#! /bin/sh' > branch
	@echo 'java -classpath .:$$CLASSPATH Branch "$$@"' >> branch
	@chmod +x branch
