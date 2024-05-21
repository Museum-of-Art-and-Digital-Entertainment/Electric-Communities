package ec.e.rep.steward;

eclass EByteArray {

    byte[] data;

    EByteArray(byte[] data) {
        this.data = data;
    }

    byte[] value() {
        return data;
    }
}
