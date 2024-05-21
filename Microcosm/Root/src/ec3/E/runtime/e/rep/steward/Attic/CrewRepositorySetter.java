package ec.e.rep.steward;

import ec.e.start.Vat;
import ec.e.start.EEnvironment;
import ec.e.rep.steward.SimpleRepository;
import ec.e.rep.crew.CrewRepository;
import ec.e.run.OnceOnlyException;
import java.io.IOException;
import ec.e.rep.StandardRepository;

/**

 * This STEWARD class forwards the in-vat StandardRepository (after it
 * has been created and opened) to the CrewRepository, which makes it
 * globally available to all CREW classes.<p>

 * For convenience, StandardRepositorymaker calls setRepository below
 * after opening a StandardRepository. Therefore you normally don't
 * have to call this.

 */

public class CrewRepositorySetter {

    public static void setRepository(SimpleRepository repository, Vat vat) 
         throws IOException {
             try {
                 CrewRepository.setRepository(repository,vat);
             } catch (OnceOnlyException ooe) {
                 throw new IOException("Crew repository has already been set:" + ooe);
             }
    }
}
