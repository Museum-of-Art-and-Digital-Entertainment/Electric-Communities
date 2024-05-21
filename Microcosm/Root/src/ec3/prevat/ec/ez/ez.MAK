ORIGIN		= Symantec Café
ORIGIN_VER	= Version 7.22
VERSION		= DEBUG

!IFDEF SUB_DEBUG
DEBUG		= $(SUB_DEBUG)
NDEBUG		= !$(SUB_DEBUG)
!ELSE
DEBUG		= 1
NDEBUG		= 0
!ENDIF

PROJ		= COSM
APPTYPE		= JAVA CONSOLE
PROJTYPE	= class

JAVAC		= sj
MAKE		= SMAKE
LNK		= LINK


HEADERS		= 
!IF $(DEBUG)
OUTPUTDIR	= C:\Cafe\classes
!IF EXIST (C:\Cafe\classes)
CREATEOUTPUTDIR	=
!ELSE
CREATEOUTPUTDIR	= if not exist $(OUTPUTDIR)\*.* md $(OUTPUTDIR)
!ENDIF
TARGETDIR	= .
CREATETARGETDIR	=

CFLAGS		=  -g -nowarn 
DEFINES		= 
!ELSE
OUTPUTDIR	= .
CREATEOUTPUTDIR	=
TARGETDIR	= .
CREATETARGETDIR	=

CFLAGS		=  -O 
DEFINES		= 
!ENDIF

MFLAGS		= MASTERPROJ=$(PROJ) 
DEBUGGERFLAGS	=  
PAR		= PROJS BATS SRCS

INCLUDES	= -classpath .;C:\ECDEV\jce-1.1\lib\jce.zip;C:\ECDEV\java-1.1.3\lib\classes.zip;C:\CAFE\CLASSES;C:\ECDEV\Build.win32\compute\classes;C:\ECDEV\Build.win32\crew\classes;C:\ECDEV\Build.win32\guest\classes;C:\ECDEV\Build.win32\steward\classes;C:\ECDEV\e\classes.zip;C:\ECDEV\e\compute.zip;C:\ECDEV\e\crew.zip;C:\ECDEV\e\guest.zip;C:\ECDEV\e\steward.zip;C:\ECDEV\pluribus\crew.zip;C:\ECDEV\pluribus\guest.zip;C:\ECDEV\pluribus\steward.zip;C:\ECDEV\support\compute.zip;C:\ECDEV\support\crew.zip;C:\ECDEV\support\guest.zip;C:\ECDEV\support\steward.zip;C:\ECDEV\ifc\classes.zip;C:\ECDEV\edoc\classes\edoc.zip;C:\ECDEV\cryptix\classes.zip;$(OUTPUTDIR)



SRCS		= ..\..\ec3\prevat\ec\ez\runtime\AlreadyDefinedException.java ..\..\ec3\prevat\ec\ez\collect\ArityMismatchException.java ..\..\ec3\prevat\ec\ez\collect\ArrayIterator.java  \
		..\..\ec3\prevat\ec\ez\ezvm\AssignExpr.java ..\..\ec3\prevat\ec\ez\syntax\Assoc.java ..\..\ec3\prevat\ec\ez\collect\AssociationEnumeration.java ..\..\ec3\prevat\ec\ez\ezvm\CallExpr.java  \
		..\..\ec3\prevat\ec\ez\prim\ConstructorNode.java ..\..\ec3\prevat\ec\ez\ezvm\DefineExpr.java ..\..\ec3\prevat\ec\ez\ezvm\DispatchExpr.java ..\..\ec3\prevat\ec\ez\collect\DoubleInterval.java  \
		..\..\ec3\prevat\ec\ez\collect\DoubleIterator.java ..\..\ec3\prevat\ec\ez\runtime\Ejection.java ..\..\ec3\prevat\ec\ez\ezvm\Ejector.java ..\..\ec3\prevat\ec\ez\ezvm\EnterPovExpr.java  \
		..\..\ec3\prevat\ec\ez\ezvm\EqualsExpr.java ..\..\ec3\prevat\ec\ez\ezvm\EscapeExpr.java ..\..\ec3\prevat\ec\ez\ezvm\Expr.java ..\..\ec3\prevat\ec\ez\prim\EZBoolean.java  \
		..\..\ec3\prevat\ec\ez\prim\EZDouble.java ..\..\ec3\prevat\ec\ez\prim\EZEnumeration.java ..\..\ec3\prevat\ec\ez\prim\EZEnvelope.java ..\..\ec3\prevat\ec\ez\prim\EZEObjectWrapper.java  \
		..\..\ec3\prevat\ec\ez\syntax\EZIdentifier.java ..\..\ec3\prevat\ec\ez\ezvm\EZImpl_$_Impl.java ..\..\ec3\prevat\ec\ez\ezvm\EZImpl_$_Proxy.java ..\..\ec3\prevat\ec\ez\prim\EZInteger.java  \
		..\..\ec3\prevat\ec\ez\syntax\EZLexer.java ..\..\ec3\prevat\ec\ez\syntax\EZLiteral.java ..\..\ec3\prevat\ec\ez\ezvm\EZMethodNode.java ..\..\ec3\prevat\ec\ez\runtime\EZObject.java  \
		..\..\ec3\prevat\ec\ez\syntax\EZParser.java ..\..\ec3\prevat\ec\ez\prim\EZPromiseBreaker.java ..\..\ec3\prevat\ec\ez\prim\EZServiceEnvelope.java ..\..\ec3\prevat\ec\ez\prim\EZStaticWrapper.java  \
		..\..\ec3\prevat\ec\ez\prim\EZString.java ..\..\ec3\prevat\ec\ez\syntax\EZToken.java ..\..\ec3\prevat\ec\ez\prim\EZUniversal.java ..\..\ec3\prevat\ec\ez\prim\InstanceMethodNode.java  \
		..\..\ec3\prevat\ec\ez\collect\IntegerIterator.java ..\..\ec3\prevat\ec\ez\collect\Interval.java ..\..\ec3\prevat\ec\ez\prim\JavaMemberNode.java ..\..\ec3\prevat\ec\ez\ezvm\LiteralExpr.java  \
		..\..\ec3\prevat\ec\ez\ezvm\LoopExpr.java ..\..\ec3\prevat\ec\ez\collect\Mapping.java ..\..\ec3\prevat\ec\ez\collect\MappingImpl.java ..\..\ec3\prevat\ec\ez\ezvm\MatchNode.java  \
		..\..\ec3\prevat\ec\ez\runtime\MethodNode.java ..\..\ec3\prevat\ec\ez\syntax\MsgPatt.java ..\..\ec3\prevat\ec\ez\collect\NameTable.java ..\..\ec3\prevat\ec\ez\collect\NameTableEditor.java  \
		..\..\ec3\prevat\ec\ez\collect\NameTableEditorImpl.java ..\..\ec3\prevat\ec\ez\collect\NotFoundException.java ..\..\ec3\prevat\ec\ez\ezvm\NounExpr.java ..\..\ec3\prevat\ec\ez\collect\NullMarker.java  \
		..\..\ec3\prevat\ec\ez\prim\Oops.java ..\..\ec3\prevat\ec\ez\prim\PackagePath.java ..\..\ec3\prevat\ec\ez\runtime\ParseNode.java ..\..\ec3\prevat\ec\ez\ezvm\PathExpr.java  \
		..\..\ec3\prevat\ec\ez\ezvm\PovExpr.java ..\..\ec3\prevat\ec\ez\prim\PrintFunc.java ..\..\ec3\prevat\ec\ez\prim\PrintlnFunc.java ..\..\ec3\prevat\ec\ez\prim\Promise_$_Impl.java  \
		..\..\ec3\prevat\ec\ez\prim\Promise_$_Proxy.java ..\..\ec3\prevat\ec\ez\prim\PromiseDistributor_$_Impl.java ..\..\ec3\prevat\ec\ez\prim\PromiseDistributor_$_Intf.java ..\..\ec3\prevat\ec\ez\prim\PromiseDistributor_$_Proxy.java  \
		..\..\ec3\prevat\ec\ez\prim\PromiseInterior.java ..\..\ec3\prevat\ec\ez\prim\RenamedInstanceMethodNode.java ..\..\ec3\prevat\ec\ez\ezvm\RequestNode.java ..\..\ec3\prevat\ec\ez\prim\RespondActivator.java  \
		..\..\ec3\prevat\ec\ez\runtime\Script.java ..\..\ec3\prevat\ec\ez\prim\ScriptMaker.java ..\..\ec3\prevat\ec\ez\ezvm\SendExpr.java ..\..\ec3\prevat\ec\ez\ezvm\SequenceExpr.java  \
		..\..\ec3\prevat\ec\ez\prim\SimpleScript.java ..\..\ec3\prevat\ec\ez\runtime\SourceSpan.java ..\..\ec3\prevat\ec\ez\prim\StaticMethodNode.java ..\..\ec3\prevat\ec\ez\prim\SugarMethodNode.java  \
		..\..\ec3\prevat\ec\ez\syntax\SyntaxException.java ..\..\ec3\prevat\ec\ez\ezvm\ThrowExpr.java ..\..\ec3\prevat\ec\ez\ezvm\TryExpr.java ..\..\ec3\prevat\ec\ez\collect\Tuple.java  \
		..\..\ec3\prevat\ec\ez\collect\TupleImpl.java ..\..\ec3\prevat\ec\ez\ui\EZMain.java ..\..\ec3\prevat\ec\ez\ui\EZStart.java ..\..\ec3\prevat\ec\ez\ui\Listener.java  \
		..\..\ec3\prevat\ec\ez\ui\ListenerFrame.java ..\..\ec3\E\runtime\e\run\RtRun.java 

BATS		= 

.SUFFIXES: .JAVA .CLASS 

.JAVA.CLASS:
	$(JAVAC) $(CFLAGS) $(INCLUDES) -d $(OUTPUTDIR) $*.java



all:	noteout createdir $(PRECOMPILE) compall $(POSTCOMPILE) _done


all2:	createdir $(PRECOMPILE) compall $(POSTCOMPILE) _done


all3:	createdir $(PRECOMPILE) compall2 $(POSTCOMPILE) _done


compall:	$(OBJS)
	$(JAVAC) $(CFLAGS) $(INCLUDES) -d $(OUTPUTDIR) -make -cdb $(PROJ).cdb @<<
	$(SRCS)
<<


compall2:	$(OBJS)
	$(JAVAC) $(CFLAGS) $(INCLUDES) -d $(OUTPUTDIR) -cdb $(PROJ).cdb @<<
	$(SRCS)
<<

noteout:
	REM Output to $(OUTPUTDIR)

createdir:
	$(CREATEOUTPUTDIR)
	$(CREATETARGETDIR)
	
_done:
		REM  Project is up to date

buildall:	clean	all3


clean:
		-del $(TARGETDIR)\$$SCW$$.$(PROJTYPE)
		-del $(TARGETDIR)\$(PROJ).CLE
		-del COSM.dpd
		-del $(OBJS)




