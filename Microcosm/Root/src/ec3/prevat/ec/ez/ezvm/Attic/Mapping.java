package ec.ez.ezvm;

public interface Mapping {

    public Object get(Object key) throws NotFoundException;

    public Mapping occlude(Mapping under);
}

