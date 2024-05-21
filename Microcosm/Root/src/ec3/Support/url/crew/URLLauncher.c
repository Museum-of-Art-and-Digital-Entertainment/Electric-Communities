#include "ec_url_crew_URLLauncher.h"
#include "interpreter.h"

#ifdef WIN32
#include <string.h>
#endif

#define MAX_URL_LEN 1024

char *ProtocolStr[] = {
    "http:",
    "https:",
    "file:",
    "mailto:",
    "news:",
    "snews:"
};

#define MAX_PROTOCOLS 6


extern long ec_url_crew_URLLauncher_nativeOpenURL(struct Hec_url_crew_URLLauncher *foo ,struct Hjava_lang_String *url) {

    char theURL[MAX_URL_LEN];
#ifdef WIN32
    int i;
    HINSTANCE returnVal;
#endif

    javaString2CString(url, theURL, sizeof(theURL));

#ifdef WIN32
    for (i=0; i<MAX_PROTOCOLS; i++) {
        /*printf("checking protocol %s\n", ProtocolStr[i]);*/
        if ( ! _strnicmp(ProtocolStr[i], theURL, strlen(ProtocolStr[i]))) {
            returnVal = ShellExecute(NULL, 
                                     "open", 
                                     theURL, 
                                     NULL, 
                                     NULL, 
                                     SW_SHOWMINIMIZED);
            if ( (long) returnVal < 32) {
                /* return val spec'ed in the VC++ "ShellExecute" docs */
                return (long) returnVal;
            } else {
                return 0;
            }
        }
    }
    return -1; /* url was not a valid protocol */
#endif

#ifdef SPARC
    printf("nativeOpenURL on %s\n", theURL);
    return 0;
#endif

}


