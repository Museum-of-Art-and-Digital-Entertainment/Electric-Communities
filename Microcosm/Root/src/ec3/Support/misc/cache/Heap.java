// Heap.java 1.0 - originally 96/04/14 Walter Korman (found on WWW)
// This version has been altered to do arbitary node deletion and some other stuff
// Harry Richardson - 18/02/97

package ec.misc.cache;

public class Heap {
    private HeapNode root;   

    public  Heap() {
        root = null;
    }

    public HeapItem findMin() {
        if(root == null) throw new EmptyHeapException();
        return root.item;
    }

    public boolean insert(HeapItem item) {
        HeapNode n = new HeapNode(null, null, item);
        root = imeld(n, root);
        return true;
    }

    public void deleteArbitary(HeapItem item) {
        deleteArbitary(root, item);
    }

    public void deleteArbitary(HeapNode node, HeapItem item) {
        if (node == null) return;

        if (node.item == item)
            node = imeld(node.left, node.right);
        else {
            deleteArbitary(node.left, item);
            deleteArbitary(node.right, item);
        }
    }

    public HeapItem deleteMin() {
        HeapNode n = null;

        if(root == null) throw new EmptyHeapException();

        n = root;
        root = imeld(root.left, root.right);

        return n.item;
    }

    public HeapNode imeld(HeapNode h1, HeapNode h2) {
        HeapNode x, y, temp;

        if(h1 == null) return h2;
        if(h2 == null) return h1;

        if(h1.item.greaterThan(h2.item)) {
            temp = h1;
            h1 = h2;
            h2 = temp;
        }

        x = h1;
        y = h1;
        h1 = h1.right;
        y.right = y.left;

        while(h1 != null) {
            if(h1.item.greaterThan(h2.item)) {
                temp = h1;
                h1 = h2;
                h2 = temp;
            }
            
            y.left = h1;
            y = h1;
            h1 = y.right;
            y.right = y.left;
        }

        y.left = h2;
        return x;
    }

    public void print() {
        if(root != null) root.print();
        else System.out.println("<empty>");
    }

    public boolean empty() {return(root == null);}
}



class HeapNode {
    HeapNode left, right; 
    HeapItem item;       

    HeapNode() { left = null; right = null; item = null; }

    HeapNode(HeapItem item) {
        left = right = null;
        this.item = item;
    }

    HeapNode(HeapNode l, HeapNode r, HeapItem item) {
        left = l;
        right = r;
        this.item = item;
    }

    public void print() {
        if (left != null) left.print();
        if (item != null) item.print();
        if (right != null) right.print();
    }
}

