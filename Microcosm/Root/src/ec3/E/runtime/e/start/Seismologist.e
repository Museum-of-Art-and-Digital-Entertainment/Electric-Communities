package ec.e.run;


/**
 *
 */
public einterface Seismologist {
    
    /**
     *
     */
    emethod noticeQuake(TimeQuake quake);
    
    /**
     *
     */
    emethod noticeCommit();
}


/**
 *
 */
public eclass QuakeReporter implements Seismologist {
    
    /**
     *
     */
    public QuakeReporter() {}
    
    /**
     *
     */
    emethod noticeQuake(TimeQuake quake) {
        System.err.println("quake " + quake);
        quake.waitForNext(this);
    }
    
    /**
     *
     */
    emethod noticeCommit() {
        System.err.println("committed");
    }
}

