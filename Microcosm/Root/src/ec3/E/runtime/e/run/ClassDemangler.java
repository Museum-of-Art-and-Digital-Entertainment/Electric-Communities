package ec.e.run;

public class ClassDemangler
{
    static public String demangleClass(String orig) {
        String[] split = demangleSplitClass(orig);
        String result = split[0];
        for (int i = 1; i < split.length; i++) {
            result += " " + split[i];
        }
        return result;
    }

    static public String[] demangleSplitClass(String orig) {
        String clostr = null;
        String estr = null;
        String plstr = null;

        if (orig.endsWith("$closure")) {
            clostr = "closure";
            orig = orig.substring(0, orig.length() - 8);
        }

        if (orig.endsWith("_$_Deflector")) {
            estr = "deflector";
            orig = orig.substring(0, orig.length() - 12);
        } else if (orig.endsWith("_$_Impl")) {
            estr = "e-impl";
            orig = orig.substring(0, orig.length() - 7);
        } else if (orig.endsWith("_$_Intf")) {
            estr = "e-interface";
            orig = orig.substring(0, orig.length() - 7);
        } else if (orig.endsWith("_$_Sealer")) {
            estr = "sealer";
            orig = orig.substring(0, orig.length() - 9);
        }

        if        (orig.endsWith("$iicode")) {
            plstr = "ingredient-code";
            orig = orig.substring(0, orig.length() - 7);
        } else if (orig.endsWith("$iijif")) {
            plstr = "ingredient-java-interface";
            orig = orig.substring(0, orig.length() - 6);
        } else if (orig.endsWith("$kind")) {
            plstr = "pluribus-kind";
            orig = orig.substring(0, orig.length() - 5);
        } else if (orig.endsWith("$pr")) {
            plstr = "presence-router";
            orig = orig.substring(0, orig.length() - 3);
        } else if (orig.endsWith("$prjif")) {
            plstr = "presence-router-java-interface";
            orig = orig.substring(0, orig.length() - 6);
        } else if (orig.endsWith("$ui")) {
            plstr = "unum-impl";
            orig = orig.substring(0, orig.length() - 3);
        } else if (orig.endsWith("$ur")) {
            plstr = "unum-router";
            orig = orig.substring(0, orig.length() - 3);
        } else if (orig.endsWith("$urjif")) {
            plstr = "unum-router-java-interface";
            orig = orig.substring(0, orig.length() - 6);
        }

        int len = 1;
        if (clostr != null) {
            len++;
        }
        if (estr != null) {
            len++;
        }
        if (plstr != null) {
            len++;
        }

        String[] result = new String[len];
        result[0] = orig;
        len = 1;
        if (plstr != null) {
            result[1] = plstr;
            len = 2;
        }
        if (estr != null) {
            result[len] = estr;
            len++;
        }
        if (clostr != null) {
            result[len] = clostr;
            len++;
        }

        return result;
    }
}
