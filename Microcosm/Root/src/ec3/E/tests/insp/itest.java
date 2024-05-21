package ec.tests.insp;

import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import java.util.Hashtable;
import java.util.Vector;
import ec.tests.insp.Feline;    // Test case class
import ec.tests.insp.Canine;    // Test case class

/* 

 * Class to test the Inspector under the Agency
 * Agency now always contains the ability to start an Inspector.
 * To run this test, type
 * java ec.e.start.EBoot ec.tests.insp.itest Inspector=stop,EZ
 * to a Windows bash shell.

 */
 
eclass itest implements ELaunchable {
    emethod go(EEnvironment env) {
        InspectorTester tester = new InspectorTester(env);
        tester <- go();
    }
}

public eclass HelloE implements ELaunchable
{
    emethod go(EEnvironment env) {
    HelloWorldDoohickey first = new HelloWorldDoohickey();
    first <- hello();
    }
}

eclass HelloWorldDoohickey 
{
    emethod hello() {
    System.out.println("Hola Mundo");
    }
}

public eclass TestClass {
    int i = 17;
    Object myObject = null;

    emethod setObject(Object o, TestClass2 from) {
        myObject = o;
        System.out.println("TestClass object received message");
        from <- setOtherObject(this);
    }
}

public eclass TestClass2 {
    int j = 18;
    Object otherObject = null;

    emethod setOtherObject(Object o) {
        System.out.println("TestClass2 object received message");
        otherObject = o;
    }
}

eclass InspectorTester {
    Canine makeDog(String sound) {

        Canine dog = new Canine();
        dog.whatISay = sound;
        dog.Someboolean = true;
        dog.Somechar = 'X';
        dog.Somelong = 1234;
        //        dog.Somedouble = 3.14159;
        dog.Somefloat = (float)41.999;
        dog.cat = null;
        dog.Someshort = 567;
        dog.Someint = 9876;
        dog.Somebyte = 5;
        dog.Somenull = null;
        dog.Somedummy = 666;
        dog.nums[0] = 11111111;
        dog.nums[1] = 22222222;
        dog.nums[2] = 33333333;

        dog.ch[0]='M';
        dog.ch[1]='e';
        dog.ch[2]='o';
        dog.ch[3]='w';

        dog.smurf[0] = 50;
        dog.smurf[1] = 60;
        dog.smurf[2] = 70;
        dog.smurf[3] = 80;
        dog.smurf[4] = 90;
        dog.flags[0] = false;
        dog.flags[1] = true;
        dog.flags[2] = true;
        dog.flags[3] = false;
        dog.flags[4] = true;
        dog.flags[5] = false;
        dog.guy[0] = 4000;
        dog.guy[1] = 5000;
        dog.guy[2] = 6000;

        dog.furr[0] = (float) 4.0004;
        dog.furr[1] = (float) 5.0005;
        dog.furr[2] = (float) 6.0006;

        dog.Someobj[0] = new Integer(1);
        dog.Someobj[1] = new String("Hi Gordie");

        dog.Someobj2[0][0] = new String("0,0");
        dog.Someobj2[0][1] = new String("0,1");
        dog.Someobj2[1][0] = new String("1,0");
        dog.Someobj2[1][1] = new String("1,1");

        return dog;
    }

    public InspectorTester(EEnvironment env) {
    }

    emethod go() {
        try {
            Canine dog1 = makeDog("Growl");
            Canine dog2 = makeDog("Ruff");
            Canine dog3 = makeDog("Arf");
            ec.e.inspect.Inspector.gather(dog1, "Animals", "dog1");

            Hashtable sounds = new Hashtable(10);
            sounds.put("Cow", "Moo");
            sounds.put("Cat", "Meow");
            sounds.put("Dog", "Arf");
            sounds.put("Sheep", "Baa");
            ec.e.inspect.Inspector.gather(sounds, "Animals", "sounds");
            ec.e.inspect.Inspector.gather(sounds, "sounds");

            String[] birds = {"goose","dove","falcon","eagle","hen"};
            ec.e.inspect.Inspector.gather(birds,"Animals","birds");

            Vector dogs = new Vector();
            dogs.addElement(dog1);
            dog2.Someobj[0] = dog1;
            dog3.Someobj[0] = dog2;
            dogs.addElement(dog2);
            dogs.addElement(dog3);
            ec.e.inspect.Inspector.gather(dogs,"Animals", "All Dogs");

            // Create an E message or two

            System.out.println("Creating helloworlddoohickey");
            HelloWorldDoohickey first = new HelloWorldDoohickey();
            ec.e.inspect.Inspector.gather(first,"E Objects", "HelloWorldDoohickey first");
    
            first <- hello();

            TestClass testObj = new TestClass();
            ec.e.inspect.Inspector.gather(testObj,"E Objects", "testObj");
            TestClass2 testObj2 = new TestClass2();
            ec.e.inspect.Inspector.gather(testObj2,"E Objects", "testObj2");
            testObj <- setObject(this, testObj2);
        } catch (Exception e) {
            System.out.println("Something threw error:" + e);
        }
    }
}
