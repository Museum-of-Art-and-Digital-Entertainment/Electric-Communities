#include <StubPreamble.h>
#include <ec_util_Sniffer.h>

#ifdef WIN32
__declspec(dllimport) 
#endif
void sniffReferencers (struct Hjava_util_Hashtable *alreadyScanned,
					   HArrayOfObject *referencers, 
					   struct Hjava_lang_Object *target);

void ec_util_Sniffer_sniffReferencers(struct Hec_util_Sniffer *me,
								  struct Hjava_util_Hashtable *scannedObjects,
								  HArrayOfObject *referencers,
								  struct Hjava_lang_Object *objectToScanFor)
{
	sniffReferencers (scannedObjects, referencers, objectToScanFor);
}

