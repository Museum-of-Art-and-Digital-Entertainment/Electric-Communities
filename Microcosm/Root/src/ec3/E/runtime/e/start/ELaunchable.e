package ec.e.run;


/**
 * For a class to be launchable by EBoot, it must implement ELaunchable 
 */
public einterface ELaunchable {
    
    /**
     * The first guest is started by being passed the all powerful
     * EEnvironment, whose authority is should subdivide and hand out
     * piecemeal. 
     */
    emethod go(EEnvironment env);
}


