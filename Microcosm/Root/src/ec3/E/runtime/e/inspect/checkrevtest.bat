@echo off
if "%1" == "" goto error
java ec.e.start.EBoot ec.e.inspect.CheckpointReviveTester %1 TraceLog_ec.e.quake.StableStore=debug
goto done
:error
echo Usage: checkrevtest filename
echo where filename is name of an existing checkpoint file
:done
