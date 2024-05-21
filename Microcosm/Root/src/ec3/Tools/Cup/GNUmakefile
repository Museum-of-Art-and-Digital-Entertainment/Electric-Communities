
MODNAME       = java_cup
PACKAGE       = java_cup

JCPKGDIR      = java_cup
JCRUNPKGDIR   = java_cup/runtime

FILES.java =    $(JCPKGDIR)/Main.java \
	$(JCRUNPKGDIR)/lr_parser.java \
	$(JCRUNPKGDIR)/symbol.java \
	$(JCRUNPKGDIR)/token.java \
	$(JCRUNPKGDIR)/str_token.java \
	$(JCRUNPKGDIR)/int_token.java \
	$(JCRUNPKGDIR)/virtual_parse_stack.java \
	$(JCPKGDIR)/symbol.java \
	$(JCPKGDIR)/terminal.java \
	$(JCPKGDIR)/non_terminal.java \
	$(JCPKGDIR)/production_part.java \
	$(JCPKGDIR)/symbol_part.java \
	$(JCPKGDIR)/action_part.java \
	$(JCPKGDIR)/production.java \
	$(JCPKGDIR)/lr_item_core.java \
	$(JCPKGDIR)/shift_action.java \
	$(JCPKGDIR)/internal_error.java \
	$(JCPKGDIR)/parse_action_table.java \
	$(JCPKGDIR)/parse_action.java \
	$(JCPKGDIR)/reduce_action.java \
	$(JCPKGDIR)/version.java \
	$(JCPKGDIR)/parse_action_row.java \
	$(JCPKGDIR)/parse_reduce_table.java \
	$(JCPKGDIR)/parse_reduce_row.java \
	$(JCPKGDIR)/emit.java \
	$(JCPKGDIR)/terminal_set.java \
	$(JCPKGDIR)/action_production.java \
	$(JCPKGDIR)/parser.java \
	$(JCPKGDIR)/symbol_set.java \
	$(JCPKGDIR)/lexer.java \
	$(JCPKGDIR)/sym.java \
	$(JCPKGDIR)/lalr_item.java \
	$(JCPKGDIR)/lalr_item_set.java \
	$(JCPKGDIR)/lalr_state.java \
	$(JCPKGDIR)/lalr_transition.java 

include $(TOP)/E/makefiles/Defs.gmk

.PHONY: all classes 

all: basic_mkdirs mk_java

mk_java: $(CLASSDIR) .delete.classlist $(FILES.javat)
	if [ -s $(CLASSLIST) ]; \
	then $(KSH) $(JAVAHOME)/bin/javac -d $(BUILD_DRIVE)$(CLASSDIR) $(JAVAC_FLAGS) \
	    $(shell if [ -s $(CLASSLIST) ]; then cat $(CLASSLIST); fi) ; \
	fi
	if [ -s $(TOUCHLIST) ]; then \
	    touch $(shell if [ -s $(TOUCHLIST) ]; then cat $(TOUCHLIST); fi); \
	fi

clean: basic_clean
	-rm -rf  $(CLASSDIR)/$(JCRUNPKGDIR)

include $(TOP)/E/makefiles/Rules.gmk

