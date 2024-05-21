/* 
	ECompilerTest.e
	v 0.1
	Compiler basic test	
	Gordie Freedman Copyright Electric Communities
	Proprietary and Confidential
*/


package ec.tests.compiler;

public class ECompilerTest
{
	public static void main(String args[])
	{
		ECompilerTester ec = new ECompilerTester();
		ec <- phoneHome();
	}
}

eclass ECompilerTester 
{
	emethod phoneHome ()
	{
		System.out.println("ECompilerTest ...");
	}
}
