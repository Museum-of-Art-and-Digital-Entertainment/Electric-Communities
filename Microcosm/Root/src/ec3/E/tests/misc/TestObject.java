package ec.tests.misc;

public class TestObject
{
	// Single long value
    public long longValue;

	// Hopefully to generate uneven alignment for the longs
    public short shortValue;
	public char charValue;

	// Array of longs
    public long longArray[] = new long[3];

	// Double Values
	public double doubleValue;
	public double doubleArray[] = new double[3];

	// Float Values 
	public float floatValue;
	public float floatArray[] = new float[3];

	// Object Values;
//	public Object objectValue;
//	public Object objectArray[] = new Object[3];

	TestObject() {

		// Initialize the oddballs
		shortValue = 123;
		charValue = 'c';

		// Initialize the long values
		longValue = (long)12345678901L;
		longArray[0] = 23456789012L;
		longArray[1] = 222;
		longArray[2] = -23456789012L;

		// Initialize the double values
		doubleValue = (double)123.4567890123456789123456789;
		doubleArray[0] = 1.23;
		doubleArray[1] = -987654321.1234567890987654321;
		doubleArray[2] = 12345678901.123;

		// Initialize the float values
		floatValue = (float)6.789;
		floatArray[0] = (float)1.2345;
		floatArray[1] = (float)-9.8765;
		floatArray[2] = (float)123456.8901234567;
	}

	public boolean equals (TestObject otherObject) {
		boolean returnValue = true;

		if (this.shortValue != otherObject.shortValue) { returnValue = false; }
		if (this.charValue != otherObject.charValue) { returnValue = false; }

		if (this.longValue != otherObject.longValue) { returnValue = false; }
		if (this.longArray[0] != otherObject.longArray[0]) { returnValue = false; }
		if (this.longArray[1] != otherObject.longArray[1]) { returnValue = false; }
		if (this.longArray[2] != otherObject.longArray[2]) { returnValue = false; }

		if (this.doubleValue != otherObject.doubleValue) { returnValue = false; }
		if (this.doubleArray[0] != otherObject.doubleArray[0]) { returnValue = false; }
		if (this.doubleArray[1] != otherObject.doubleArray[1]) { returnValue = false; }
		if (this.doubleArray[2] != otherObject.doubleArray[2]) { returnValue = false; }

		if (this.floatValue != otherObject.floatValue) { returnValue = false; }
		if (this.floatArray[0] != otherObject.floatArray[0]) { returnValue = false; }
		if (this.floatArray[1] != otherObject.floatArray[1]) { returnValue = false; }
		if (this.floatArray[2] != otherObject.floatArray[2]) { returnValue = false; }

		return returnValue;
	}

	public void compareAndPrintResults (TestObject otherObject) {
		printResultTable ("testObject", "otherObject");
		printResultTable ("----------------", "---------------");
		printResultTable (String.valueOf (this.charValue), String.valueOf (otherObject.charValue));
		printResultTable (String.valueOf (this.shortValue), String.valueOf (otherObject.shortValue));

		System.out.println ("\nLongs:");
		printResultTable (String.valueOf (this.longValue), String.valueOf (otherObject.longValue));
		printResultTable (String.valueOf (this.longArray[0]), String.valueOf (otherObject.longArray[0]));
		printResultTable (String.valueOf (this.longArray[1]), String.valueOf (otherObject.longArray[1]));
		printResultTable (String.valueOf (this.longArray[2]), String.valueOf (otherObject.longArray[2]));

		System.out.println ("\nDoubles:");
		printResultTable (String.valueOf (this.doubleValue), String.valueOf (otherObject.doubleValue));
		printResultTable (String.valueOf (this.doubleArray[0]), String.valueOf (otherObject.doubleArray[0]));
		printResultTable (String.valueOf (this.doubleArray[1]), String.valueOf (otherObject.doubleArray[1]));
		printResultTable (String.valueOf (this.doubleArray[2]), String.valueOf (otherObject.doubleArray[2]));

		System.out.println ("\nFloats:");
		printResultTable (String.valueOf (this.floatValue), String.valueOf (otherObject.floatValue));
		printResultTable (String.valueOf (this.floatArray[0]), String.valueOf (otherObject.floatArray[0]));
		printResultTable (String.valueOf (this.floatArray[1]), String.valueOf (otherObject.floatArray[1]));
		printResultTable (String.valueOf (this.floatArray[2]), String.valueOf (otherObject.floatArray[2]));

		if (this.equals (otherObject) != true) 
			System.out.println ("\n*** TEST FAILED: original object is different from decoded object ***");
		else
			System.out.println ("\n*** TEST SUCCEEDED: original object is equal to decoded object ***");
	}

	public static void printResultTable (String firstCol, String secondCol) {
		int numberOfSpaces = 25 - firstCol.length();
		char spaces[] = new char[numberOfSpaces];

		while (numberOfSpaces-- > 0) spaces[numberOfSpaces] = ' ';

		System.out.println (firstCol + spaces + secondCol);
	}

}

