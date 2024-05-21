package ec.tables;


/**
 *
 */
public class ShareCount {

    private int myCount;

    public ShareCount() {
        myCount = 1;
    }

    public boolean isExclusive() {
        return myCount <= 1;
    }

    public ShareCount dup() {
        myCount++;
        return this;
    }

    public ShareCount release() {
        if (myCount <= 0) {
            throw new RuntimeException("excess releases");
        }
        myCount--;
        return new ShareCount();
    }
}
