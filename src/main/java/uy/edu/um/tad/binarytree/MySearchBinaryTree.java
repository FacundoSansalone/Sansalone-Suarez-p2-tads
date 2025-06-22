/**
 * 
 */
package uy.edu.um.tad.binarytree;

import uy.edu.um.tad.linkedlist.MyList;

public interface MySearchBinaryTree<K extends Comparable<K>, V> {

	void add(K key, V value);

	void remove(K key);

	boolean contains(K key);

	V find(K key);

	MyList<K> inOrder();

	TreeNode<K, V> getRoot();

	MyList<V> inOrderValues();

}
