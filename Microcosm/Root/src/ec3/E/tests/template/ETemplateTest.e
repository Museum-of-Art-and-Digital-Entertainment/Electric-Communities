/* 
	ETemplateTest.e
	v 0.1
	Template basic test	
	Gordie Freedman Copyright Electric Communities
	Proprietary and Confidential
*/


package ec.tests.template;

public class ETemplateTest
{
	public static void main(String args[])
	{
		ETemplateTester et = new ETemplateTester();
		et <- phoneHome();
	}
}

eclass ETemplateTester 
{
	emethod phoneHome ()
	{
		System.out.println("E Template Test ...");
	}
}
