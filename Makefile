SOURCE_DIR	:= Source
OUTPUT_DIR	:= Binary
DOCS_DIR	:= Docs
RM		:= rm -rf
JAVA		:= java
JAVAC		:= javac
JFLAGS		:= -sourcepath $(SOURCE_DIR) -g -d $(OUTPUT_DIR) -Xlint
JVMFLAGS	:= -ea -esa -Xfuture
JVM		:= $(JAVA) $(JVMFLAGS)
JAR		:= jar
JARFLAGS	:= cf
JAVADOC		:= javadoc

class_path	:= OUTPUT_DIR

all:
		JAVAC $(JFLAGS) $(SOURCE_DIR)/*.java

javadocs:
		JAVADOC -d $(DOCS_DIR) $(SOURCE_DIR)/*.java

clean:
		$(RM) $(OUTPUT_DIR)/*
