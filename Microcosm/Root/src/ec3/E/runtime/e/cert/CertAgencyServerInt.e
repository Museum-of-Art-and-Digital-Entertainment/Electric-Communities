package ec.cert;

import java.security.KeyPair;
import ec.cert.VerifierDescription;

einterface CertAgencyServerInt {
  emethod getVerifierServers(EResult whoWantsToKnow);
  emethod createNewCertificateType(KeyPair aKeyPair, 
                                   VerifierDescription description);
  emethod ping(EResult whoWantsToKnow);
  emethod saveNeeded();
  emethod saveState();
}
