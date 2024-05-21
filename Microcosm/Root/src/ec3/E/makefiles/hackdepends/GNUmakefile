
all: $(BUILD)/classes $(BUILD)/classes/HackDepends.class

$(BUILD)/classes:
	mkdir -p $(BUILD)/classes

$(BUILD)/classes/HackDepends.class: hackdepends.java
	javac -d $(BUILD_DRIVE)$(BUILD)/classes hackdepends.java
