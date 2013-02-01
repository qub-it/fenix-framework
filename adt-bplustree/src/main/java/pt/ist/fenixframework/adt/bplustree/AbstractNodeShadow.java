package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import pt.ist.fenixframework.core.Externalization;

/** The keys comparison function should be consistent with equals. */
public abstract class AbstractNodeShadow<T extends Serializable> extends AbstractNodeShadow_Base implements Iterable {
    /* Node Interface */

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    abstract AbstractNodeShadow insert(Comparable key, T value);
    /** Removes the element with the given key */
    abstract AbstractNodeShadow remove(Comparable key);
    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    abstract T get(Comparable key);
    /** Returns the value at the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract T getIndex(int index);
    /** Returns the value that was removed from  the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract AbstractNodeShadow removeIndex(int index);
    /** Returns <code>true</code> if this map contains a mapping for the specified key.  */
    abstract boolean containsKey(Comparable key);
    /** Returns the number os key-value mappings in this map */
    abstract int size();
    /** Returns the keys mapped in this map */
    abstract Collection<? extends Comparable> getKeys();

    abstract String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds);


    /* **** Uncomment the following to support pretty printing of nodes **** */
    
    // static final AtomicInteger GLOBAL_COUNTER = new AtomicInteger(0);
    // protected int counter = GLOBAL_COUNTER.getAndIncrement();
    // public String toString() {
    // 	return "" + counter;
    // }

    /* *********** */

    
    public  AbstractNodeShadow() {
        super();
    }

    AbstractNodeShadow getRoot() {
	InnerNodeShadow thisParent = this.getParent();
	return thisParent == null ? this : thisParent.getRoot();
    }
    
    AbstractNodeShadow getRootShadow() {
	InnerNodeShadow thisParent = this.getParentShadow();
	return thisParent == null ? this : thisParent.getRootShadow();
    }

    abstract Map.Entry<Comparable,T> removeBiggestKeyValue();
    abstract Map.Entry<Comparable,T> removeSmallestKeyValue();
    abstract Comparable getSmallestKey();
    abstract void addKeyValue(Map.Entry keyValue);
    // merge elements from the left node into this node. smf: maybe LeafNode can be a subclass of InnerNode
    abstract void mergeWithLeftNode(AbstractNodeShadow leftNode, Comparable splitKey);
    // the number of _elements_ in this node (not counting sub-nodes)
    abstract int shallowSize();
    abstract Iterator<? extends Comparable> keysIterator();

    /*
     * We need to ensure that no DomainObjects escape in the serialization.  This would be a problem
     * if 'someone' (e.g. Infinispan in clustered mode) needed to internalize a D.O. (hidden in a
     * treemap entry) while the FenixFramework was still running its static initialization code. So
     * we convert the TreeMap to/from a byte[] every time it is externalized/internalize by the FF.
     * We wrap it in a Serializable class (TreeMapExternalization) ,because some backends (e.g. OGM)
     * don't deal well with byte[] yet.
     */

    public static Serializable /*byte[]*/ externalizeTreeMap(TreeMap treeMap) {
        // return Externalization.externalizeObject(new TreeMapExternalization(treeMap));
        return new TreeMapExternalization(treeMap);
        // return treeMap;
    }

    public static TreeMap internalizeTreeMap(Serializable/*byte[]*/ externalizedTreeMap) {
        // TreeMapExternalization treeMapExternalization = Externalization.internalizeObject(externalizedTreeMap);
        return ((TreeMapExternalization)externalizedTreeMap).toTreeMap();
        // return (TreeMap)externalizedTreeMap;
    }

    private static class TreeMapExternalization implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] serializedTreeMap;

        TreeMapExternalization(TreeMap<Comparable,? extends Serializable> treeMap) {
            this.serializedTreeMap = Externalization.externalizeSerializable(treeMap);
        }

        TreeMap toTreeMap() {
            return (TreeMap)Externalization.internalizeSerializable(serializedTreeMap);
        }
    }
}
