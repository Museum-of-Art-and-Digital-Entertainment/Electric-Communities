@echo off
echo.
echo CATALOG: Create a catalog of all ez test cases
rem
rem  Assumptions: 1) Command line prompt is 4DOS or 4NT from \ECDEV
rem               2) Directory structure matches environment variables in Setup section
rem               3) Original layout of ez files is maintained
rem  Output:      Catalog is in file TestCatalogTemp4.txt
rem  Created:     09/15/97 02:00 PM Benjamin Tiddle
rem  Updated:     10/07/97 09:30 PM Benjamin Tiddle
rem
rem ==============================================
rem Setup: set the necessary environment variables
rem ==============================================

rem Drive where the ez files and the tempory directory are located, (i.e. C:)
    set TC_DRIVE=c:
    if not direxist %TC_DRIVE% (goto usage)

rem Location of EZ test suite files
    set TC_PATH=\src\ec3\prevat\ec\ez\tests
    if not direxist %TC_DRIVE%\%TC_PATH% (goto usage)

rem Directory to store tempory files
    set TC_TMP=\tmp
    if not direxist %TC_DRIVE%%TC_TMP% (goto usage)

    goto main

rem =========================================================
rem Usage
rem =========================================================
    :usage

    echo.
    echo TC_DRIVE = %TC_DRIVE%
    echo TC_PATH  = %TC_DRIVE%%TC_PATH%
    echo TC_TMP   = %TC_DRIVE%%TC_TMP%
    echo.
    echo ERROR: Environment variables and directory structure must match.
    goto Cleanup

rem =========================================================
rem Main: create the catalog
rem =========================================================
    :main

rem Change to directory where the ez test files are located
    %TC_DRIVE%

rem Delete tempory file(s) that may already exists
    del %TC_TMP%\TestCatalogTemp?.txt /eq

rem Create a file that contains the file names of all ez files
    dir %TC_DRIVE%%TC_PATH%\*.ez /bs > %TC_TMP%\TestCatalogTemp1.txt

rem Print file section heading
    echo TEST FILES >> %TC_TMP%\TestCatalogTemp2.txt
    echo. >> %TC_TMP%\TestCatalogTemp2.txt

rem Print the list of ez files to the catalog, appending a single blank in front
    echo {print "  " $0} > %TC_TMP%\TestCatalogTemp5.awk
    awk -f %TC_TMP%\TestCatalogTemp5.awk %TC_TMP%\TestCatalogTemp1.txt >> %TC_TMP%\TestCatalogTemp2.txt

rem Print test case section heading
    echo. >> %TC_TMP%\TestCatalogTemp2.txt
    echo TEST CASES >> %TC_TMP%\TestCatalogTemp2.txt
    echo. >> %TC_TMP%\TestCatalogTemp2.txt

rem For each .ez file, print the catalog entry: filename, file summary, and each test case title
    for %f in (@%TC_TMP%\TestCatalogTemp1.txt) gosub casebanner
    goto next01
    :casebanner
    grep -h -E "Summary|Title|File " %f >> %TC_TMP%\TestCatalogTemp2.txt
    echo. >> %TC_TMP%\TestCatalogTemp2.txt
    return
    :next01

rem Remove begining "// "
    echo {if (index($0,"// ")) print " " substr($0,3) > %TC_TMP%\TestCatalogTemp3.awk
    echo else print $0} >> %TC_TMP%\TestCatalogTemp3.awk
    awk -f %TC_TMP%\TestCatalogTemp3.awk %TC_TMP%\TestCatalogTemp2.txt > %TC_TMP%\TestCatalogTemp4.txt

rem Read the catalog file in notepad to review and/or edit
    notepad %TC_TMP%\TestCatalogTemp4.txt

rem ====================================================================
rem Cleanup: remove all environment variables created in this batch file
rem ====================================================================
    :Cleanup

    set TC_DRIVE=
    set TC_PATH=
    set TC_TMP=

