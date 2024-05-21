package ec.e.serial;

import java.io.OutputStream;
import java.io.IOException;
import ec.tables.PEHashtable;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ObjOpener;
import ec.e.openers.Chef;
import ec.e.openers.ParamRecipe;

public class ParamSerializer extends Serializer {

    private PEHashtable myParameters;
    private ParamRecipe myParamRecipe;
    private Chef myChef;

    static final int PARAMSERIAL_ID = 254;

    /**
     * The new ParamSerializer will use the initialization of
     * Serializer, but will register the parameters table before any
     * other objects are processed.  Serializaton will then be able to
     * make use of the parameters table as an object and the index
     * will be the same when a ParamUnserializer is used.
     */
    /*package*/ ParamSerializer(OutputStream out,
                                OpenerRecipe maker,
                                PEHashtable parameters) {
        super(out, maker);
        myParameters = parameters;
        myParamRecipe = new ParamRecipe(parameters, null);
        //Note that we don't registerEncoder on myChef
        myChef = new Chef(maker, myParamRecipe);
        registerNext(myChef);
    }

    /**
     * Create a Serializer or ParamSerializer, used instead of a constructor.
     * When the parameters table is null or empty, use a Serializer instead of
     * a ParamSerializer.
     *
     * @param out;
     * @param maker;
     * @param parameters nullOk;
     */
    static public Serializer make(OutputStream out,
                                  OpenerRecipe maker,
                                  PEHashtable parameters)
        throws IOException
    {
        if ((parameters == null) || parameters.isEmpty()) {
            return Serializer.make(out, maker);
        } else {
            out.write(PARAMSERIAL_ID);
            return new ParamSerializer(out, maker, parameters);
        }
    }

    /*package*/ ObjOpener openerForObject(Object obj) {
        if (myParameters.containsKey(obj)) {
            return myChef;
        } else {
            return super.openerForObject(obj);
        }
    }
}




