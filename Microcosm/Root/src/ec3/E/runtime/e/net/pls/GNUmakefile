
FILES= \
	pls_test \
	pls_killifhung \
	pls_checkup \
	pls_initial \
	pls_restart

all:
	mkdir -p $(BUILD)/bin
	cp $(FILES) $(BUILD)/bin

# add these lines to the end of /etc/inittab to keep a pls up:
#
# pr:234:respawn:/home/eng/pls-location/Release/bin/pls_restart -fg 5667 >/dev/console 2>&1
# pk:234:respawn:/home/eng/pls-location/Release/bin/pls_killifhung 5667 >/dev/console 2>&1
