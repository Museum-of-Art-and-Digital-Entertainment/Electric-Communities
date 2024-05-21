package ec.e.net.steward;

import ec.e.start.Seismologist;
import ec.e.start.Vat;
import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;
import ec.e.start.TimeQuake;
import ec.e.start.SmashedException;
import ec.e.net.Registrar;
import ec.e.net.crew.ConnectionStatisticsCrew;


public eclass ConnectionStatisticsSteward 
        implements ConnectionStatistics, Seismologist {

    private Registrar myRegistrar;
    private Vat myVat;

    /**
       Create a new ConnectionStatisticsSteward.

       @param remoteAddr where the connection is coming from.
      */
    public ConnectionStatisticsSteward(Vat vat, Registrar registrar) {
        myVat = vat;
        myRegistrar = registrar;
        buildCrew();
    }

    private void buildCrew() {
        FragileRootHolder root = myVat.makeFragileRoot((Object) this, (Seismologist) this);
        new ConnectionStatisticsCrew(root);
    }

    // Responsibility from Seismologist
    emethod noticeCommit() {}

    emethod noticeQuake(TimeQuake quake) {
        buildCrew();
    }

    // Responsibility from ConnectionStatistics
    public String statistics() {
        return myRegistrar.statistics();
    }

}