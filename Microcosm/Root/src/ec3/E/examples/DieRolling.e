/*
  Die rolling example
  v 1.1 Working Code
  Jan 25, 1996
  Arturo Bejar
  Copyright 1996 Electric Communities, all rights reserved.

  Change history:
  31/01/95 abs Changed keywords and renamed a couple of vars.

  Program output:

  First: Started roll
  Second: Started roll
  Second: First's result =151
  First: Second's result =238
  Second: First's key =15 and X = 136
  Second: Roll result = 5
  First: Second's key =23 and X = 215
  First: Roll result = 5
*/

package ec.examples.drg;

import ec.e.lang.EInteger;

public class DieRolling
{
    public static void main(String args[]) {
        DieRollingPeer First, Second;

        First = new DieRollingPeer();
        Second = new DieRollingPeer();

        First <- dieRollStart( Second );
    }
}

eclass DieRollingPeer
{
    EInteger concealedKey;
    EInteger hisKey;
    EInteger hisResult;
    EInteger myResult;

    emethod dieRollStart(DieRollingPeer otherGuy) {
        int myX = 136;
        int actualKey = 15;

        myResult = new EInteger( Fn( actualKey, myX ) );

        otherGuy <- dieRollWith( myResult, concealedKey,
                                &hisResult, &hisKey );

        System.out.println("First: Started roll");

        ewhen hisResult(int aResult) {
            &concealedKey <- forward( new EInteger( actualKey ) );
            System.out.println("First: Second's result =" + aResult);

            ewhen hisKey(int aKey) {
                int hisX = 0;
                int finalResult = 0;


                hisX = FnInv ( aResult, aKey );
                finalResult = Combine( myX, hisX );
                System.out.println("First: Second's key =" + aKey +
                                   " and X = " + hisX);
                System.out.println("First: Roll result = " + finalResult);
            }
        }
    }

    emethod dieRollWith (EInteger hisResult, EInteger hisKey,
                         EResult myResult, EResult myKey) {
        int myX = 215;
        int actualKey = 23;

        myResult <- forward( new EInteger( Fn( actualKey, myX ) ) );
        System.out.println("Second: Started roll");

        ewhen hisResult ( int aResult ) {
            myKey <- forward( new EInteger( actualKey ) );
            System.out.println("Second: First's result =" + aResult);

            ewhen hisKey (int aKey) {
                int hisX = 0;
                int finalResult = 0;

                hisX = FnInv( aResult, aKey );
                finalResult = Combine( myX, hisX );
                System.out.println("Second: First's key =" + aKey +
                                   " and X = " + hisX);
                System.out.println("Second: Roll result = " + finalResult);
            }
        }
    }

    int Fn( int a, int b) {
        return (a + b);
    }
    int FnInv( int a, int b)  {
        return (a - b);
    }
    int Combine( int a, int b) {
        return ( (a ^ b) % 6 );
    }
}
