# Java compiler
JAVAC = javac
# Java runtime
JAVA = java
# Classes to compile
CLASSES = Main.java Switch.java Node.java Frame.java
# Directory for compiled .class files
BIN_DIR = bin

# Default target
.PHONY: all compile run clean
all: $(BIN_DIR) compile

# Compile Java classes into bin directory
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

# Rule to compile each Java class into the bin directory
$(BIN_DIR)/%.class: %.java
	$(JAVAC) -d $(BIN_DIR) $<

# Rule to compile all classes
compile: $(patsubst %.java,$(BIN_DIR)/%.class,$(CLASSES))

# Running main program, assuming there are 5 nodes (change if want different number)
run: $(CLASSES)
	$(JAVA) Main 5

# Clean up .class files and output files
clean:
	rm -rf $(BIN_DIR)
	rm -f node*output.txt
