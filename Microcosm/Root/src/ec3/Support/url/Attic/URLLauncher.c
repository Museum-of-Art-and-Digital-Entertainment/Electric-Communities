#ifdef WIN32
#include "URLLauncher.h"
#include "interpreter.h"

#define MAX_URL_LEN 1024

extern long ec_url_URLLauncher_OpenURL(struct HURLLauncher *foo, struct Hjava_lang_String *url)  {
    char theURL[MAX_URL_LEN];
    HINSTANCE returnVal;
    javaString2CString(url, theURL, sizeof(theURL));
    returnVal = 
      ShellExecute(NULL, "open", theURL, NULL, NULL, SW_SHOWMINIMIZED);
    if ( (long) returnVal < 32)
      // return values described in the VC++ "ShellExecute" documentation
      return (long) returnVal;
    else
      return 0;
}
#endif
