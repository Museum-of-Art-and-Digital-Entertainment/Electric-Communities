package ec.tests.java.gc;

import java.gc.*;

/**
 * See <a href="http://www.cs.umd.edu/users/dabe/CDs/Songs/sahra.cynthia.sylvia.stout.would.not.take.the.garbage.out.html">
 * this class's namesake</a>.  Trivial test at the moment.
 */
public class SahraStout {

    static public void main(String[] args) {
        BackRefTable brt = new BackRefTable();
	// brt.update();
	System.err.println(brt);
    }
}

