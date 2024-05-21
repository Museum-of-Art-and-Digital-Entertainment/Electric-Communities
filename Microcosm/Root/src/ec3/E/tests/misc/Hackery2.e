/* 
	Hackery2.e
	v 0.1
	More simple hackery to experiment with E
	Gordie Freedman Copyright Electric Communities
	Propietary and Confidential
*/


package ec.tests.misc;

public class Hackery2
{
	public static void main(String args[])
	{
	
		HackeryE hack = new HackeryE();
		WackeryE wack = new WackeryE();
		
		hack <- hello();
		wack <- hello();
	}
}

einterface HackeryInterface
{
        hello();
}

eclass HackeryE implements HackeryInterface
//eclass HackeryE
{
	emethod hello()
	{
		System.out.println("Hack hello");
	}
}

//eclass WackeryE extends HackeryE implements HackeryInterface
//eclass WackeryE implements HackeryInterface
//eclass WackeryE
eclass WackeryE extends HackeryE
{
	emethod hello()
	{
		System.out.println("Wack hello");
	}
}

