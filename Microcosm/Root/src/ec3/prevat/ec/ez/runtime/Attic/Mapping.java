package ec.ez.runtime;

public interface Mapping {

    public Object get(Object key) throws NotFoundException;

    public Mapping occlude(Mapping under);
}

