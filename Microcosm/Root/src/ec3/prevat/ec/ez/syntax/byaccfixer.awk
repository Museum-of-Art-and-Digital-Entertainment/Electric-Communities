#!awk
BEGIN { flag = 0; }
{ gsub( /\<yaccpar\>/, "EZParser" ) }
{ gsub( /\<System\.out\.println\(/, "yyprintln(" ) }
{ gsub( /\<double\>/, "Object" ) }
$0 == "Object val_pop()" { flag = 1; }
$0 == "Object val_peek(int relative)" { flag = 1; }
/^\}/ { flag = 0; }
{ if (flag) { gsub( /\<return -1;$/, "return null;" ) } }
{ print }

