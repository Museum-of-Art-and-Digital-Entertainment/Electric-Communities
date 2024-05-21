package ec.e.run;

abstract public class InternalEWhenClosure {
    protected boolean amUnused = true;
    protected boolean use() {
        if (amUnused) {
            amUnused = false;
            return true;
        } else {
            return false;
        }
    }
    abstract protected void doit(Object val);
}

abstract public class InternalEOrWhenClosure extends InternalEWhenClosure {
    private InternalEWhenClosure myBaseClosure;
    public InternalEOrWhenClosure(InternalEWhenClosure baseClosure) {
        myBaseClosure = baseClosure;
    }
    protected boolean use() {
        return myBaseClosure.use();
    }
}

abstract public class InternalEIfClosure extends InternalEWhenClosure {
    private BitVec myFlags;
    public InternalEIfClosure(int eorifCount) {
        if (eorifCount > 0)
            myFlags = new BitVec(eorifCount);
        else
            myFlags = null;
    }
    abstract protected void doElse();
    protected void elseCheck(int elseCase) {
        if (myFlags == null) {
            if (use()) {
                doElse();
            }
        } else {
            myFlags.clrBit(elseCase);
            if (myFlags.isEmpty()) {
                if (use()) {
                    doElse();
                }
            }
        }
    }
    protected boolean test(Object testFlag) {
        if (((Boolean) testFlag).booleanValue()) {
            return use();
        } else {
            elseCheck(0);
            return false;
        }
    }
}

abstract public class InternalEOrIfClosure extends InternalEWhenClosure {
    private InternalEIfClosure myBaseClosure;
    private int myElseCase;
    public InternalEOrIfClosure(InternalEIfClosure baseClosure, int elseCase) {
        myBaseClosure = baseClosure;
        myElseCase = elseCase;
    }
    protected boolean test(Object testFlag) {
        if (((Boolean) testFlag).booleanValue()) {
            return myBaseClosure.use();
        } else {
            myBaseClosure.elseCheck(myElseCase);
            return false;
        }
    }
}

abstract public class InternalECatchClosure {
    protected void doit(Object obj) {
        catchMe((Throwable) obj);
    }
    abstract protected void catchMe(Throwable e);
}

public class BitVec {
    private long myBits = 0;

    public BitVec(int length) {
        if (length > 64)
            throw new RuntimeException("BitVec init length " + length +
                                       " exceeds limit of 64");
        for (int i=0; i<length; ++i)
            setBit(i);
    }

    public void setBit(int num) {
        myBits |= (1L << num);
    }

    public void clrBit(int num) {
        myBits &= ~(1L << num);
    }

    public boolean tstBit(int num) {
        return (myBits & (1L << num)) != 0;
    }

    public boolean isEmpty() {
        return myBits == 0;
    }
}
