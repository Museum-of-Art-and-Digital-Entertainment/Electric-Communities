/* 
	Hackery.e
	v 0.1
	Simple hackery to experiment with E
	Gordie Freedman Copyright Electric Communities
	Proprietary and Confidential
*/


package ec.tests.misc;
import ec.e.run.EBoolean;
import ec.e.lang.EInteger;

public class Hackery
{
	public static void main(String args[])
	{
		HackeryE hack = new HackeryE();
		hack <- hello(1, 2, 3);
	}
}

eclass HackeryE 
{
	EInteger a;
	EBoolean b;
	EBoolean c = etrue;

	emethod hello(int x, int y, int z)
	{
		System.out.println("Hack hello with " + x + " " + y + " " + z);
		ewhen a (int v) {
			System.out.println("In the when for a");
		}

		ewhen b (boolean v) {
			System.out.println("In the when for b");
		}
		eorwhen c (boolean v) {
			System.out.println("In the when for c");
		}
	}
}

