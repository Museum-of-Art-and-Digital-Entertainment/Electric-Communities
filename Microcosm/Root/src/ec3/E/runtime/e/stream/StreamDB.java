/*
    Native Stream in and Stream out.
*/

package ec.e.stream;

import java.lang.System;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.e.openers.ClassRecipe;
import ec.e.openers.RootClassRecipe;
import ec.e.openers.OpenerRecipe;
import ec.e.openers.ObjOpener;
import ec.e.openers.ArrayOpener;
import ec.e.openers.guest.AllowingClassRecipe;
import ec.e.util.EmptyEnumeration;
import ec.util.NestedException;


/**
 *
 */
public class StreamDB {

    static private final ClassRecipe OurNonTransientClassRecipe
        = AllowingClassRecipe.make(RootClassRecipe.THE_ONE,
                                   "ec.e.stream.PassByCopy");

    static private final OpenerRecipe OurOpenerRecipe
        = new OpenerRecipe(PassByCopy.DECODER_MAKERS,
                           PassByCopy.ENCODER_MAKERS,
                           OurNonTransientClassRecipe);

    static public Object preload(Class clazz, RtDecoder decoder)
         throws IOException
    {
        ObjOpener opener;
        try {
            //XXX Kludge.  Using forEncodingAs rather that forDecodingBy
            //because comm doesn't yet use Openers for identification.
            opener = OurOpenerRecipe.forEncodingAn(clazz);

        } catch (ClassNotFoundException ex) {
            throw new NestedException("finding an ObjOpener", ex);
        } catch (InvocationTargetException ex) {
            throw new NestedException("finding an ObjOpener", ex);
        } catch (IllegalAccessException ex) {
            throw new NestedException("finding an ObjOpener", ex);
        } catch (NoSuchMethodException ex) {
            throw new NestedException("finding an ObjOpener", ex);
        }
        Object result = opener.decodePreface(decoder);

        //XXX The special case for arrays has probably always been
        //broken and should be removed.  We're leaving it in for the
        //moment for compatibility
        if (clazz.getName().charAt(0) == '[') {
            opener.decodeBody(result, decoder);
        }
        return result;
    }

    static public Object load(Object object, RtDecoder decoder)
         throws IOException
    {
        //XXX Kludge until we switch comm to Serializers
        ObjOpener opener = OurOpenerRecipe.forEncoding(object);

        //XXX The special case for arrays has probably always been
        //broken and should be removed.  We're leaving it in for the
        //moment for compatibility
        if (opener instanceof ArrayOpener) {
            return object;
        }
        opener.decodeBody(object, decoder);
        return object;
    }

    static public void store(Object object, RtEncoder encoder)
         throws IOException {

        ObjOpener opener = OurOpenerRecipe.forEncoding(object);

        //XXX The special case for arrays has probably always been
        //broken and should be removed.  We're leaving it in for the
        //moment for compatibility
        if (opener instanceof ArrayOpener) {
            opener.encodePreface(object, encoder);
        }
        opener.encodeBody(object, encoder);
    }

    /**
     * Instrumentation no longer works.  We need to put it back in.
     */
    static public void setDebugOn() {}
    static public void setDebugOff() {}
}
