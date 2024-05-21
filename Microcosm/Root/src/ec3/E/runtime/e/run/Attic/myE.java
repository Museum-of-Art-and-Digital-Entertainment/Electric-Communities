// E-Support, (C) Electric Communities (and Michael Philippsen),
// 1997, all rights reserved.
// =============================================================

package ec.e.run;

import ec.e.net.NetIdentityMaker;

public class EObject
implements EObject_$_Intf
{
    public void when$async(EWhenClosure_$_Intf p1) {
        return;
    }

  /*
  public void respond$async(EWhenClosure_$_Intf p1)
  {
        return;
  }
  */
    public void order$async(RtEnvelope msg, EDistributor_$_Intf succ) {
        return;
    }
}

public interface EObject_$_Intf
extends E$Interface
{
  public void when$async(EWhenClosure_$_Intf p1) ;;
  public void order$async(RtEnvelope msg, EDistributor_$_Intf succ);
  ////public void respond$async(EWhenClosure_$_Intf p1);
}

public class EObject_$_Deflector
extends RtDeflector
implements EObject_$_Intf
{
    public EObject_$_Deflector(RtTether t, Object k) {
        super(t, k);
    }

  public void when$async(EWhenClosure_$_Intf p1) {
    target_$_.invoke(EObject_$_Sealer.sealer_$_when$ec_dot_e_dot_run_dot_EWhenClosure, {(Object) p1});
  }
  /*
  public void respond$async(EWhenClosure_$_Intf p1)
  {
    target_$_.invoke(EObject_$_Sealer.sealer_$_respond$ec_dot_e_dot_run_dot_EWhenClosure, {(Object)p1});
  }
  */
  public void order$async(RtEnvelope msg, EDistributor_$_Intf succ)
  {
    target_$_.invoke(EObject_$_Sealer.sealer_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor, {(Object) msg, (Object) succ});
  }
}

public class EObject_$_Proxy
extends EProxy_$_Impl
implements EObject_$_Intf
{
   public void when$async(EWhenClosure_$_Intf p1) {
      RtRun.enqueue(this, EObject_$_Sealer.seal_$_when$ec_dot_e_dot_run_dot_EWhenClosure(p1));
   };
  /*
  public void respond$async(EWhenClosure_$_Intf p1)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_respond$ec_dot_e_dot_run_dot_EWhenClosure(p1));
  }
  */
  public void order$async(RtEnvelope msg, EDistributor_$_Intf succ)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor (msg, succ));
  }
}

public class EObject_$_Channel
extends EChannel_$_Impl
implements EObject_$_Intf
{
    public EObject_$_Channel (boolean distFlag)
    {
        super (distFlag);
    }
   public void when$async(EWhenClosure_$_Intf p1) {
      RtRun.enqueue(this, EObject_$_Sealer.seal_$_when$ec_dot_e_dot_run_dot_EWhenClosure(p1));
   };
  /*
  public void respond$async(EWhenClosure_$_Intf p1)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_respond$ec_dot_e_dot_run_dot_EWhenClosure(p1));
  }
  */
  public void order$async(RtEnvelope msg, EDistributor_$_Intf succ)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor (msg, succ));
  }
}

public abstract class EObject_$_Impl
extends Object
implements EObject_$_Intf, Cloneable
{
    // Identity is maintained invariant for all exports of this Object
    private long identity = 0L;

    protected void when(EWhenClosure_$_Intf method) {
        if (method != null) {
            Object val;
            val = value();
            //System.out.println ("When calling back with value " + val +
            //" to method " + method);
            RtEnvelope retour = new RtEnvelope (
                EWhenClosure_$_Sealer.
                    sealer_$_doclosure$java_dot_lang_dot_Object, 
                {val});

            // XXX (GJF) Seems like we can do this optimization
            // XXX (danfuzz) Not really, this should get excised if
            // it ever gets in the way of fixing other E runtime aspects
            ((EObject_$_Impl)method).deliver(retour);
        } else {
            System.err.println ("*** error: when got a null closure");
        }
    }

    /*
    protected final void respond(EWhenClosure_$_Intf method) {
        if (method != null) {
            //System.out.println ("When calling back with value " + val +
            //" to method " + method);
            RtEnvelope retour =
                EWhenClosure_$_Sealer.seal_$_doclosure$java_dot_lang_dot_Object(this);

            // XXX (GJF) Seems like we can do this optimization
            // XXX (danfuzz) Not really, this should get excised if
            // it ever gets in the way of fixing other E runtime aspects
            ((EObject_$_Impl)method).deliver(retour);
        } else {
            System.err.println ("*** error: respond got a null closure");
        }
    }
    */

    protected void order (RtEnvelope msg, EDistributor_$_Intf dist) {
        if (msg != null && dist != null) {
            //System.out.println ("When calling back with value " + val +
            //" to method " + method);
            deliver(msg);
            RtEnvelope fwd = new RtEnvelope(EDistributor_$_Sealer.
                sealer_$_forward$ec_dot_e_dot_run_dot_EObject, 
                {(Object) this});
            RtRun.enqueue(dist, fwd);
        } else {
            System.err.println ("order got empty envelope or distributor");
        }
    }

   public void when$async(EWhenClosure_$_Intf p1) {
      RtRun.enqueue(this, EObject_$_Sealer.seal_$_when$ec_dot_e_dot_run_dot_EWhenClosure(p1));
   };

  /*
  public void respond$async(EWhenClosure_$_Intf p1)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_respond$ec_dot_e_dot_run_dot_EWhenClosure(p1));
  }
  */
  public void order$async(RtEnvelope msg, EDistributor_$_Intf succ)
  {
    RtRun.enqueue(this, EObject_$_Sealer.seal_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor (msg, succ));
  }

  protected void deliver (RtEnvelope env)
  {
      env.deliverTo (this);
  }

    public final long getIdentity ()
    {
        if (identity == 0L)
        {
            identity = NetIdentityMaker.nextIdentity();
        }
        return (identity);
    }

    protected Object clone ()
         throws CloneNotSupportedException
    {
        EObject_$_Impl other = (EObject_$_Impl) super.clone ();
        other.identity = 0L;
        return (other);
    }

    public Object value()
    {
        throw new RtRuntimeException
        ("Value method unimplemented for object of class " +
         Trace.eclassString (this));
    }
}



// ------------------------------------------------------------

public class EObject_$_Sealer
extends RtSealer
{
  public static final EObject_$_Sealer sealer_$_when$ec_dot_e_dot_run_dot_EWhenClosure = new EObject_$_Sealer(0, "function when(ec.e.run.EWhenClosure)");

  public static final RtEnvelope seal_$_when$ec_dot_e_dot_run_dot_EWhenClosure (EWhenClosure_$_Intf method) {
    return (new RtEnvelope (sealer_$_when$ec_dot_e_dot_run_dot_EWhenClosure,
                            {(Object) method}));
  }
  /*
  public static final EObject_$_Sealer sealer_$_respond$ec_dot_e_dot_run_dot_EWhenClosure = new EObject_$_Sealer(0, "function respond(ec.e.run.EWhenClosure)");
  public static final RtEnvelope seal_$_respond$ec_dot_e_dot_run_dot_EWhenClosure (EWhenClosure_$_Intf method) {
    return (new RtEnvelope (sealer_$_respond$ec_dot_e_dot_run_dot_EWhenClosure,
                            {(Object) method}));
  }
  */
  public static final EObject_$_Sealer sealer_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor = new EObject_$_Sealer (1, "function order(ec.e.run.RtEnvelope, ec.e.run.EDistributor)");

  public static final RtEnvelope seal_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor (RtEnvelope env, EDistributor_$_Intf succ) {
          return (new RtEnvelope (sealer_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor, {(Object) env, (Object) succ}));
  }

  private static EObject_$_Sealer[] the_$_Sealers;

  static {
    the_$_Sealers = new EObject_$_Sealer[2];
    ////the_$_Sealers = new EObject_$_Sealer[3];
    the_$_Sealers[0] = sealer_$_when$ec_dot_e_dot_run_dot_EWhenClosure;
    the_$_Sealers[1] = sealer_$_order$ec_dot_e_dot_run_dot_RtEnvelope$ec_dot_e_dot_run_dot_EDistributor;
    ////the_$_Sealers[2] = sealer_$_respond$ec_dot_e_dot_run_dot_EWhenClosure;
  }

  public EObject_$_Sealer(int my_$_Index, String name) {
    super(my_$_Index, name);
  }

  public void invoke(Object target, Object[] args_$_) throws Exception {
    EObject_$_Impl realTarget;
    try {
      realTarget = (EObject_$_Impl)target;
    } catch (RuntimeException e) {
        badTarget (target);
        return;
    }
    switch (my_$_Index) {
      case 0: {
        EWhenClosure_$_Intf arg_$_0;
        try {
            arg_$_0 = (EWhenClosure_$_Intf) args_$_[0];
        } catch (RuntimeException e) {
            badArgs (target, args_$_);
            return;
        }
        realTarget.when(arg_$_0);
        break;
      }
      case 1: {
        RtEnvelope arg_$_0;
        EDistributor_$_Intf arg_$_1;
        try {
            arg_$_0 = (RtEnvelope) args_$_[0];
            arg_$_1 = (EDistributor_$_Intf) args_$_[1];
        } catch (RuntimeException e) {
            badArgs (target, args_$_);
            return;
        }
        realTarget.order(arg_$_0, arg_$_1);
        break;
      }
      /*
      case 2: {
        EWhenClosure_$_Intf arg_$_0;
        try {
            arg_$_0 = (EWhenClosure_$_Intf) args_$_[0];
        } catch (RuntimeException e) {
            badArgs (target, args_$_);
            return;
        }
        realTarget.respond(arg_$_0);
        break;
      }
      */
    default:
        badSealer();
    }
  };

  protected RtSealer otherSealer (int msg) {
    return (the_$_Sealers[msg]);
  }
}
