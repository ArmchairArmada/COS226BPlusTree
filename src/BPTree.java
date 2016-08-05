import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of a B+ Tree that implements the Map interface. 
 * 
 * @author Nathan
 *
 * @param <K>  Type of the key, must extend Comparable
 * @param <V>  Type of the value to store.
 */
public class BPTree<K extends Comparable<K>, V> implements Map<K, V> {
	
	// --- Inner Classes Used By BPTree --------------------------------------- 
	
	/**
	 * Results returned from the node split operation.
	 * 
	 * @author Nathan
	 */
	private class SplitResult {
		public Node splitNode;
		public K midKey;
		
		public SplitResult(K midKey, Node splitNode) {
			this.splitNode = splitNode;
			this.midKey = midKey;
		}
	}
	
	
	/**
	 * Results returned from the redistribution operation
	 * 
	 * @author Nathan
	 */
	private class RedistResult {
		public boolean redistSuccess;
		public K middleKey;
		
		public RedistResult(boolean redistSuccess, K middleKey) {
			this.redistSuccess = redistSuccess;
			this.middleKey = middleKey;
		}
	}
	
	
	/**
	 * Node base class
	 * 
	 * @author Nathan
	 *
	 */
	protected abstract class Node {
		protected BPTree<K,V> bpTree;
		
		/**
		 * Node constructor.  Simply stores reference to tree this node belongs
		 * to.
		 * 
		 * @param bpTree  B+ Tree this node belongs to.
		 */
		public Node(BPTree<K,V> bpTree) {
			this.bpTree = bpTree;
		}

		/**
		 * Returns the number of items in this node.
		 * 
		 * @return  Number of items in this node.
		 */
		public abstract int size();
		
		/**
		 * Returns whether or not the node is empty.
		 * 
		 * @return  True if empty, False if not
		 */
		public abstract boolean isEmpty();
		
		/**
		 * If a node is too large, it needs to split.
		 * 
		 * @return  True if too large, else False.
		 */
		public abstract boolean tooLarge();
		
		/**
		 * If a node is too small it needs to redistribute or merge.
		 * 
		 * @return  True if too small, else False.
		 */
		public abstract boolean tooSmall();
		
		/**
		 * Checks if this node or a descendant node contains the specified key. 
		 * 
		 * @param key  Key to check for
		 * @return     True if this or descendant node contains key, else false.
		 */
		public abstract boolean contains(K key);
		
		/**
		 * Finds and returns value associated with the specified key in either
		 * itself or a descendant node.  Returns null if not found.
		 * 
		 * @param key  Key to get the value for.
		 * @return     Value at specified key or null if not found.
		 */
		public abstract V get(K key);
		
		/**
		 * Inserts a key value pair entry into the subtree of the node.  It
		 * will be handed of down the tree to be inserted into a leaf node.
		 * 
		 * If the key already exists in the tree, the entry will be updated
		 * with the new value.
		 * 
		 * @param entry  Key value pair representing the entry to insert.
		 * 
		 * @return  Value that was previously stored at entry, if any.
		 */
		public abstract V insert(KeyVal<K,V> entry);
		
		/**
		 * Removes an entry from the subtree of the node associated with the
		 * key.  Returns the value of the entry at that key or null if key
		 * not found.
		 * 
		 * @param key  Key of entry to remove.
		 * @return     Value of removed entry or null if no matching entry.
		 */
		public abstract V remove(K key);
		
		/**
		 * Split the node and distribute it's elements between this node and
		 * the newly created node.
		 * 
		 * @return  Results of splitting this node.
		 */
		public abstract SplitResult split();
		
		/**
		 * Merge another node into this node -- causing the elements of the
		 * nodes to be combined.  Merging may require updating a key with the
		 * key of the parent node's element of the node being merged.
		 * 
		 * @param other  Other node to merge into this node.
		 * @param parentKey  Key of the element in the parent node.
		 */
		public abstract void merge(Node other, K parentKey);
		
		/**
		 * Redistribute elements evenly between this node and another node.
		 * Redistributing may require updating the key of one of the elements
		 * in this node with the key of the element from the parent node.
		 * 
		 * @param other      Other node to distribute elements between.
		 * @param parentKey  Key of the parent node's element.
		 * @return           Results of the redistribution.
		 */
		public abstract RedistResult redistribute(Node other, K parentKey);
		
		/**
		 * Recursively prints full tree to System.out -- for debugging.
		 * 
		 * @param depth  Depth of the node, used for indentation.
		 */
		public abstract void printFullTree(int depth);
		
		/**
		 * Counts nodes in the subtree.
		 * 
		 * @return  Number of nodes in subtree.
		 */
		public abstract int countNodes();
		
		/**
		 * Get the leaf that should contain the specified key.  The leaf may
		 * not actually contain this key.
		 * 
		 * @param key  Key that the leaf should contain.
		 * @return     A leaf node
		 */
		public abstract LeafNode getLeaf(K key);
		
		/**
		 * Get first (left most) leaf in the tree.
		 * 
		 * @return  A leaf node
		 */
		public abstract LeafNode getFirstLeaf();

		/**
		 * Save the node.
		 * 
		 * The file format the B+ Tree is saved into isn't very robust -- it is
		 * more for demonstration purposes as the file format is easy to load
		 * in a text editor and read.  The StringParseInterface provides a means
		 * of converting an Object into a String or converting a String into an
		 * Object.  A lot of key and value types may be difficult to use because
		 * the data will have to all fit in one line of text and you may need
		 * some access to the internal workings of the Object.
		 * 
		 * Serialization has been considered, but I thought something that is
		 * plain text might be better for looking at the file in a text editor
		 * and checking it for correctness.
		 * 
		 * Other text based formats have also been considered, but JSON would
		 * require an extra library and XML is more verbose than I would have
		 * liked to have used.
		 * 
		 * @param depth  Depth of node (indentation level)
		 * @param bufferedWriter  Writer to write node data to
		 * @param keyParser    Converts between key type and string
		 * @param valueParser  Converts between value type and string
		 * @throws IOException  If problems saving
		 */
		public abstract void save(int depth, BufferedWriter bufferedWriter,
			StringParseInterface keyParser, StringParseInterface valueParser)
			throws IOException;

		/**
		 * Load the node.
		 * 
		 * @see LeafNode#save
		 * 
		 * @param size  Size of node after loading
		 * @param prevLeaf  Previous leaf node, used for linking leaves together
		 * @param bufferedReader  Reader to read node data from
		 * @param keyParser    Converts between key type and string
		 * @param valueParser  Converts between value type and string
		 * @return  Last added Leaf Node
		 * @throws IOException  If problems loading
		 */
		public abstract LeafNode load(int size, LeafNode prevLeaf,
				BufferedReader bufferedReader, StringParseInterface keyParser,
				StringParseInterface valueParser)
				throws IOException;
	}
	
	
	/**
	 * Leaf Node Class.
	 * 
	 * The leaf nodes are the lowest nodes in the tree and contain the actual
	 * key/value data entries stored in the data structure.
	 * 
	 * The leaf nodes link to each other like doubly linked lists so range based
	 * search operations can be performed.
	 * 
	 * @author Nathan
	 *
	 */
	protected class LeafNode extends Node {
		protected ArrayList<KeyVal<K,V>> entries;
		protected LeafNode prev = null;
		protected LeafNode next = null;
		
		/**
		 * LeafNode constructor.
		 * 
		 * Stores reference to B+ Tree this node belongs to and also creates
		 * list of key/value pairs for entries in the data structure.
		 * 
		 * @param bpTree  B+ Tree this node belongs to.
		 */
		public LeafNode(BPTree<K,V> bpTree) {
			super(bpTree);
			entries = new ArrayList<KeyVal<K,V>>(bpTree.leafSize+1);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return entries.size();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty() {
			return entries.isEmpty();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean tooLarge() {
			return entries.size() > bpTree.leafSize;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean tooSmall() {
			return entries.size() < bpTree.leafHalfSize;
		}
		
		/**
		 * {@inheritDoc}
		 * 
		 * Since this is a leaf node, the entry might be found in this node.
		 */
		@Override
		public boolean contains(K key) {
			if (entries.size() == 0) {
				return false;
			}
			
			KeyVal<K,V> entry = new KeyVal<K,V>(key, null);
			int index = findPossibleIndex(entry);
			
			if (index >= entries.size()) {
				return false;
			}
			
			return entries.get(index).compareTo(entry) == 0;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public V get(K key) {
			if (entries.size() == 0) {
				return null;
			}
			
			KeyVal<K,V> entry = new KeyVal<K,V>(key, null);
			int index = findPossibleIndex(entry);
			
			if (index >= entries.size()) {
				return null;
			}
			
			if (entries.get(index).compareTo(entry) == 0) {
				return entries.get(index).getValue();
			}
			
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V insert(KeyVal<K,V> entry) {
			if (entries.size() == 0) {
				bpTree.incSize();
				entries.add(entry);
				return null;
			}
			
			int index = findPossibleIndex(entry);
			
			if (index >= entries.size()) {
				bpTree.incSize();
				entries.add(entry);
				return null;
			}
			
			if (entries.get(index).compareTo(entry) == 0) {
				V oldVal = entries.get(index).getValue();
				entries.set(index, entry);
				return oldVal;
			}
			
			bpTree.incSize();
			entries.add(index, entry);
			
			return null;
		}

		/**
		 * Finds a possible index where the entry with the same key as the
		 * given entry may reside.  The entry at this location may not be the
		 * same, an additional check may be needed to compare them or the index
		 * may be used as a location where a new entry should be inserted.
		 * 
		 * @param entry  Entry with same key that is being searched for.
		 * @return       Index of the entry that searched indexy may be.
		 */
		private int findPossibleIndex(KeyVal<K, V> entry) {
			// TODO: Optimize -- use binary search
			int index = 0;
			while (index < entries.size() && entry.compareTo(entries.get(index)) > 0) {
				index++;
			}
			return index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V remove(K key) {
			if (entries.size() == 0) {
				return null;
			}
			
			KeyVal<K,V> entry = new KeyVal<K,V>(key, null);
			int index = findPossibleIndex(entry);
			
			if (index >= entries.size()) {
				return null;
			}
			
			if (entries.get(index).compareTo(entry) == 0) {
				V oldValue = entries.get(index).getValue();
				bpTree.decSize();
				entries.remove(index);
				return oldValue;
			}
			
			return null;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public SplitResult split() {
			// New node resulting from the split
			LeafNode splitNode = new LeafNode(bpTree);
			
			// Where to split
			int midIndex = (int)Math.ceil(entries.size() / 2.0);
			
			// Move half the old children into new children for this and the split node
			ArrayList<KeyVal<K,V>> oldEntries = entries;
			entries = new ArrayList<KeyVal<K,V>>(oldEntries.subList(0, midIndex));
			splitNode.entries = new ArrayList<KeyVal<K,V>>(
					oldEntries.subList(midIndex, oldEntries.size())
			);
			
			// Mid key will be first entry on split node
			K midKey = splitNode.entries.get(0).getKey();
			
			// Update prev/next links between Leaf Nodes
			splitNode.next = next;
			next = splitNode;
			splitNode.prev = this;
			
			return new SplitResult(midKey, splitNode);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void merge(Node other, K parentKey) {
			@SuppressWarnings("unchecked")
			LeafNode node = (LeafNode)other;
			
			entries.addAll(node.entries);
			
			// Update prev/next between Leaf Nodes
			next = node.next;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public RedistResult redistribute(Node other, K parentKey) {
			if (size() + other.size() < bpTree.leafHalfSize*2) {
				// Too small to redistribute
				return new RedistResult(false, null);
			}
			
			@SuppressWarnings("unchecked")
			LeafNode node = (LeafNode)other;
			
			ArrayList<KeyVal<K,V>> tmp = entries;
			tmp.addAll(node.entries);
			
			int midIndex = tmp.size() / 2;
			
			entries = new ArrayList<KeyVal<K,V>>(tmp.subList(0, midIndex));
			node.entries = new ArrayList<KeyVal<K,V>>(tmp.subList(midIndex, tmp.size()));
			
			return new RedistResult(true, node.entries.get(0).getKey());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void printFullTree(int depth) {
			String tab = "";
			if (depth != 0)
				tab = String.format("%1$" + (depth*3) + "s", "");
			
			for (KeyVal<K,V> entry : entries) {
				System.out.println(tab + "L" + entry);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int countNodes() {
			return 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BPTree<K, V>.LeafNode getLeaf(K key) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BPTree<K, V>.LeafNode getFirstLeaf() {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void save(int depth, BufferedWriter bufferedWriter,
				StringParseInterface keyParser, StringParseInterface valueParser)
				throws IOException {
			
			StringBuilder sb = new StringBuilder();
			
			for (int i=0; i<depth; i++) {
				sb.append(" ");
			}
			
			String pad = sb.toString();
			
			sb.append("L,");
			sb.append(size());
			sb.append("\n");
			
			bufferedWriter.write(sb.toString());
			
			for (KeyVal<K,V> entry : entries) {
				sb.setLength(0);
				
				sb.append(pad);
				sb.append(keyParser.makeString(entry.getKey()));
				sb.append("|");
				sb.append(valueParser.makeString(entry.getValue()));
				sb.append("\n");
				
				bufferedWriter.write(sb.toString());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public BPTree<K, V>.LeafNode load(int size,
				BPTree<K, V>.LeafNode prevLeaf, BufferedReader bufferedReader,
				StringParseInterface keyParser, StringParseInterface valueParser)
				throws IOException {
			
			if (prevLeaf != null) {
				prevLeaf.next = this;
				prev = prevLeaf;
			}
			
			String line;
			String keyPart;
			String valPart;
			int divIndex;
			K k;
			V v;
			
			for (int i=0; i<size; i++) {
				line = bufferedReader.readLine();
				divIndex = line.indexOf('|');
				keyPart = line.substring(0, divIndex).trim();
				valPart = line.substring(divIndex+1, line.length()).trim();
				
				k = (K)keyParser.parseString(keyPart);
				v = (V)valueParser.parseString(valPart);
				
				bpTree.incSize();
				entries.add(new KeyVal<K,V>(k,v));
			}
			
			return this;
		}
	}
	
	
	/**
	 * Inner Node Class.
	 * 
	 * Inner nodes direct a path leading to the leaf node that might contain
	 * the entry we are storing/looking for.
	 * 
	 * @author Nathan
	 */
	protected class InnerNode extends Node {
		protected ArrayList<KeyVal<K,Node>> children;
		
		/**
		 * InnerNode Constructor.
		 * 
		 * Stores reference to B+ Tree this node belongs to and creates a list
		 * of children that are directly below this node in the tree.
		 * 
		 * @param bpTree  B+ Tree this node belongs to.
		 */
		public InnerNode(BPTree<K,V> bpTree) {
			super(bpTree);
			children = new ArrayList<KeyVal<K,Node>>(bpTree.innerSize+1);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return children.size();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty() {
			return children.isEmpty();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean tooLarge() {
			return children.size() > bpTree.innerSize;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean tooSmall() {
			return children.size() < bpTree.innerHalfSize;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean contains(K key) {
			// TODO: Optimize -- should not need to create entries to find child
			KeyVal<K,V> entry = new KeyVal<K,V>(key,null); 
			int index = findGreaterIndex(entry);
			Node childNode = children.get(index).getValue(); 
			return childNode.contains(key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V get(K key) {
			KeyVal<K,V> entry = new KeyVal<K,V>(key, null);
			int index = findGreaterIndex(entry);
			Node childNode = children.get(index).getValue(); 
			return childNode.get(key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V insert(KeyVal<K,V> entry) {
			// Find child that is >= entry and insert
			int index = findGreaterIndex(entry);
			Node childNode = children.get(index).getValue(); 
			V oldVal = childNode.insert(entry);
			
			// Is the child node now too large?
			if (childNode.tooLarge()) {
				// Split
				SplitResult sr = childNode.split();
				
				// When a leaf node splits, middle key is copied into children
				// When an inner node splits, middle key is "pushed up"
				
				// Set child of greater key to the new split node
				children.get(index).setValue(sr.splitNode);
				
				// Insert a child with key given to us pointing to old child node.
				// This inserts first half of split as new child entry.
				KeyVal<K,Node> newChild = new KeyVal<K,Node>(sr.midKey, childNode);
				children.add(index, newChild);
			}
			
			return oldVal;
		}

		/**
		 * Find the index of the child with a key larger than the key of the
		 * specified entry.  This index is needed because entries are stored
		 * under the child element that is larger than the entry.
		 * 
		 * @param entry  Entry with same key as entry we are looking for.
		 * @return       Index of child element with a larger key.
		 */
		private int findGreaterIndex(KeyVal<K, V> entry) {
			// TODO: Optimize -- use binary search
			KeyVal<K, Node> kv = new KeyVal<K, Node>(entry.getKey(), null);
			int index = 0;
			while (index < size()-1 && kv.compareTo(children.get(index)) >= 0) {
				index++;
			}
			return index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public V remove(K key) {
			KeyVal<K,V> entry = new KeyVal<K,V>(key,null);
			
			// Find child that is >= entry and remove
			int index = findGreaterIndex(entry);
			Node childNode = children.get(index).getValue(); 
			V removedValue = childNode.remove(key);
			
			// Is child node too small?
			if (childNode.tooSmall()) {
				int firstIndex;
				Node first;
				int secondIndex;
				Node second;
				
				// If childNode is last node, merge left instead of right
				if (index == children.size()-1) {
					firstIndex = index-1;
					first = children.get(firstIndex).getValue();
					secondIndex = index;
					second = childNode;
				}
				else {
					firstIndex = index;
					first = childNode;
					secondIndex = index+1;
					second = children.get(secondIndex).getValue();
				}
				
				K parentKey = children.get(firstIndex).getKey();
				
				RedistResult rr = first.redistribute(second, parentKey);
				if (rr.redistSuccess) {
					// Redistributing moves things around, so
					// we need to update the key in the children list.
					children.get(firstIndex).setKey(rr.middleKey);
				}
				else {
					// Could not redistribute, need to merge instead.
					KeyVal<K,Node> kv = children.get(secondIndex);
					first.merge(second, parentKey);
					children.get(firstIndex).setKey(kv);
					children.remove(secondIndex);
				}
			}
			
			return removedValue;
		}
		

		/**
		 * {@inheritDoc}
		 */
		@Override
		public SplitResult split() {
			// New node resulting from the split
			InnerNode splitNode = new InnerNode(bpTree);
			
			// Where to split
			int midIndex = (int)Math.ceil(children.size() / 2.0);
			
			// Divide the children between this node and the split node.
			ArrayList<KeyVal<K,Node>> oldChildren = children;
			children = new ArrayList<KeyVal<K,Node>>(oldChildren.subList(0, midIndex));
			splitNode.children = new ArrayList<KeyVal<K,Node>>(
					oldChildren.subList(midIndex, oldChildren.size())
			);
			
			// Change the last key in child list into infinity
			K midKey = children.get(children.size()-1).getKey();
			children.get(children.size()-1).setKeyInf();
			
			return new SplitResult(midKey, splitNode);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void merge(Node other, K parentKey) {
			@SuppressWarnings("unchecked")
			InnerNode node = (InnerNode)other;
			
			children.get(children.size()-1).setKey(parentKey);
			children.addAll(node.children);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RedistResult redistribute(Node other, K parentKey) {
			if (size() + other.size() < bpTree.innerHalfSize*2) {
				// Too small to redistribute
				return new RedistResult(false, null);
			}
			
			@SuppressWarnings("unchecked")
			InnerNode node = (InnerNode)other;
			
			ArrayList<KeyVal<K,Node>> tmp = children;
			// Set infinite key to parent key
			tmp.get(tmp.size()-1).setKey(parentKey);
			tmp.addAll(node.children);
			
			int midIndex = tmp.size() / 2;
			
			children = new ArrayList<KeyVal<K,Node>>(tmp.subList(0, midIndex));
			node.children = new ArrayList<KeyVal<K,Node>>(tmp.subList(midIndex, tmp.size()));
			
			// Set last key to infinity
			K midKey = children.get(children.size()-1).getKey();
			children.get(children.size()-1).setKeyInf();
			
			return new RedistResult(true, midKey);
		}
		
		/**
		 * Creates a new root node and links up the old root and the node that
		 * had been split from it.
		 * 
		 * @param key  Key left node is less than.
		 * @param left  First child node
		 * @param right  Second child node.
		 */
		public void makeRoot(K key, Node left, Node right) {
			children.add(new KeyVal<K,Node>(key, left));
			children.add(new KeyVal<K,Node>(right));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void printFullTree(int depth) {
			String tab = "";
			if (depth != 0)
				tab = String.format("%1$" + (depth*3) + "s", "");
			
			for (KeyVal<K,Node> child : children) {
				System.out.println(tab + child.getKey());
				Node childNode = child.getValue();
				childNode.printFullTree(depth + 1);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int countNodes() {
			int sum = 0;
			for (int i=0; i<size(); i++) {
				sum += children.get(i).getValue().countNodes();
			}
			return sum + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BPTree<K, V>.LeafNode getLeaf(K key) {
			KeyVal<K,V> entry = new KeyVal<K,V>(key, null);
			int index = findGreaterIndex(entry);
			Node childNode = children.get(index).getValue(); 
			return childNode.getLeaf(key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BPTree<K, V>.LeafNode getFirstLeaf() {
			return children.get(0).getValue().getFirstLeaf();
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public void save(int depth, BufferedWriter bufferedWriter,
				StringParseInterface keyParser, StringParseInterface valueParser)
				throws IOException {
			
			StringBuilder sb = new StringBuilder();
			
			for (int i=0; i<depth; i++) {
				sb.append(" ");
			}
			
			String pad = sb.toString();
			
			sb.append("I,");
			sb.append(size());
			sb.append("\n");
			
			bufferedWriter.write(sb.toString());
			
			for (KeyVal<K,Node> child : children) {
				sb.setLength(0);
				
				sb.append(pad);
				
				if (child.hasInfKey())
					sb.append("INF");
				else
					sb.append(keyParser.makeString(child.getKey()));
				
				sb.append("\n");
				
				bufferedWriter.write(sb.toString());
				
				child.getValue().save(depth+1, bufferedWriter, keyParser, valueParser);
			}
			
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public LeafNode load(int size, LeafNode prevLeaf,
				BufferedReader bufferedReader, StringParseInterface keyParser,
				StringParseInterface valueParser) throws IOException {
			
			LeafNode leaf = prevLeaf;
			String keyStr;
			String line;
			int separator;
			String nodeType;
			int nodeSize;
			K k;
			Node node;
			
			for (int i=0; i<size; i++) {
				keyStr = bufferedReader.readLine().trim();
				line = bufferedReader.readLine();
				
				separator = line.indexOf(',');
				nodeType = line.substring(0, separator).trim();
				nodeSize = Integer.parseInt(line.substring(separator+1, line.length()));
				
				if (nodeType.equals("I")) {
					node = new InnerNode(bpTree);
				}
				else {
					node = new LeafNode(bpTree);
				}
				
				if (i != size-1) {
					k = (K)keyParser.parseString(keyStr);
					children.add(new KeyVal<K,Node>(k,node));
				}
				else {
					children.add(new KeyVal<K,Node>(node));
				}
				
				leaf = node.load(nodeSize, leaf, bufferedReader, keyParser, valueParser);
			}
			
			return leaf;
		}
	}
	
	
	// --- BPTree Variables ---------------------------------------------------
	
	protected int innerSize;      // Maximum size of inner nodes
	protected int leafSize;       // Maximum size of leaf nodes
	protected int innerHalfSize;  // Minimum size of inner nodes
	protected int leafHalfSize;   // Minimum size of leaf nodes
	
	protected int entryCount = 0; // Number of entries in the tree
	
	protected Node root;  // Root node

	/**
	 * This constructor should only be used if you are intending on immediately
	 * loading from a file.
	 */
	public BPTree() {
		// Doesn't do anything.
	}
	
	/**
	 * BPTree Constructor
	 * 
	 * Creates a B+ Tree with the specified characteristics.
	 * 
	 * @param innerSize  Maximum size of inner nodes
	 * @param leafSize   Maximum size of leaf nodes
	 */
	public BPTree(int innerSize, int leafSize) {
		this.innerSize = innerSize;
		this.leafSize = leafSize;
		innerHalfSize = (int)Math.ceil(innerSize / 2.0);
		leafHalfSize = (int)Math.ceil(leafSize / 2.0);
		
		clear();
	}
	
	/**
	 * Save the B+ Tree to a text file.  Because I decided to save to a text
	 * file, and because anything can be used for the key and value types,
	 * it seemed like a good idea for save and load to take as a parameter
	 * an object that can convert between an object and a string.
	 * 
	 * I am aware that I could probably have serialized the data, but that would
	 * not allow people to easily open up the file in a text editor and look at
	 * it. 
	 * 
	 * @param filename      Name of file to save to.
	 * @param keyParser     Object to handle converting keys to strings.
	 * @param valueParser   Object to handle converting values to strings
	 * @throws IOException  If there is a problem writing the file.
	 */
	public void save(String filename, StringParseInterface keyParser,
			StringParseInterface valueParser) throws IOException {
		File file = new File(filename);
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		
		bufferedWriter.write(innerSize + "\n");
		bufferedWriter.write(leafSize + "\n");
		root.save(0, bufferedWriter, keyParser,	valueParser);
		
		bufferedWriter.close();
	}
	
	/**
	 * Load the B+ Tree from a text file.  See save's doc string.
	 * 
	 * @param filename      File to load from.
	 * @param keyParser     Object to convert strings to K objects
	 * @param valueParser   Object to convert strings to V objects
	 * @return  A new BPTree created from the file's data
	 * @throws IOException  If there is a problem with loading file
	 */
	public void load(
			String filename, StringParseInterface keyParser,
			StringParseInterface valueParser) throws IOException {
		
		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file.getAbsoluteFile());
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			innerSize = Integer.parseInt(bufferedReader.readLine());
			leafSize = Integer.parseInt(bufferedReader.readLine());
			
			innerHalfSize = (int)Math.ceil(innerSize / 2.0);
			leafHalfSize = (int)Math.ceil(leafSize / 2.0);
			
			String line = bufferedReader.readLine();
			int separator = line.indexOf(',');
			String nodeType = line.substring(0, separator).trim();
			int nodeSize = Integer.parseInt(line.substring(separator+1, line.length()));
			
			if (nodeType.equals("I")) {
				root = new InnerNode(this);
			}
			else {
				root = new LeafNode(this);
			}
			
			root.load(nodeSize, null, bufferedReader, keyParser, valueParser);
			
			bufferedReader.close();
		} catch (NumberFormatException e) {
			throw new IOException();
		}
	}
	
	/**
	 * Gets the maximum size that the inner nodes are allowed to grow to.
	 * 
	 * @return  Inner node's maximum size.
	 */
	public int getInnerSize() {
		return innerSize;
	}
	
	/**
	 * Gets the maximum size that the leaf nodes are allowed to grow to.
	 * 
	 * @return  Leaf node's maximum size.
	 */
	public int getLeafSize() {
		return leafSize;
	}
	
	/**
	 * Gets the number of entries stored in the B+ Tree.
	 * 
	 * @see java.util.Map#size()
	 *  
	 * @return  Number of entries in the tree.
	 */
	@Override
	public int size() {
		return entryCount;
	}
	
	/**
	 * Increase entry count
	 */
	private void incSize() {
		entryCount++;
	}
	
	/**
	 * Decrease entry count
	 */
	private void decSize() {
		entryCount--;
	}
	
	/**
	 * Recursively add up number of nodes.
	 * 
	 * @return  Node count
	 */
	public int countNodes() {
		return root.countNodes();
	}

	/**
	 * Checks if the B+ Tree is empty.
	 * 
	 * @see java.util.Map#isEmpty()
	 * 
	 * @return  True if no entries stored.
	 */
	@Override
	public boolean isEmpty() {
		return root.isEmpty();
	}

	/**
	 * Checks if the specified key exists in the B+ Tree.
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 * 
	 * @param key  Key to check for.
	 * @return     True if an entry with this key exists, else false.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		return root.contains((K)key);
	}

	/**
	 * Check if there is an entry that has the specified value.
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 * 
	 * @param value  Value to check for.
	 * @return       True if an entry has this value, false if not.
	 */
	@Override
	public boolean containsValue(Object value) {
		LeafNode current = root.getFirstLeaf();
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				if (value.equals(entry.getValue()))
					return true;
			}
			current = current.next;
		}
		return false;
	}

	/**
	 * Get the value of the entry associated with the specified key.  Returns
	 * null if not found.
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 * 
	 * @param key  Key of entry to retrieve value from.
	 * @return     The value of the entry with this key, or null if not found.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		return root.get((K)key);
	}
	
	/**
	 * Get a collection of entries with keys that are less than or equal to the
	 * specified key.
	 * 
	 * @param key  Key to stop on
	 * @return     A collection of entries
	 */
	public Collection<Entry<K,V>> getFirstToKey(Object key) {
		ArrayList<Entry<K,V>> values = new ArrayList<Entry<K,V>>();
		
		LeafNode current = root.getFirstLeaf();
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				@SuppressWarnings("unchecked")
				int cmp = entry.getKey().compareTo((K)key);
				
				if (cmp <= 0)
					values.add(entry);
				
				if (cmp >= 0)
					return values;
			}
			current = current.next;
		}
		
		return values;
	}
	
	/**
	 * Get a collection of entries with keys that are greater than or equal to
	 * the specified key.
	 * 
	 * @param key  Key to start from
	 * @return     A collection of entries
	 */
	public Collection<Entry<K,V>> getKeyToLast(Object key) {
		ArrayList<Entry<K,V>> values = new ArrayList<Entry<K,V>>();
		
		@SuppressWarnings("unchecked")
		LeafNode current = root.getLeaf((K)key);
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				@SuppressWarnings("unchecked")
				int cmp = entry.getKey().compareTo((K)key);
				
				if (cmp >= 0)
					values.add(entry);
			}
			current = current.next;
		}
		
		return values;
	}
	
	/**
	 * Get a collection of entries that have keys that are greater than or
	 * equal to key1 and are less than or equal to key2.
	 * 
	 * @param key1  Key to start from
	 * @param key2  Key to end from
	 * @return      Collection of entries
	 */
	public Collection<Entry<K,V>> getKeyToKey(Object key1, Object key2) {
		ArrayList<Entry<K,V>> values = new ArrayList<Entry<K,V>>();
		
		@SuppressWarnings("unchecked")
		LeafNode current = root.getLeaf((K)key1);
		
		// I know this can be done in a more optimal way, but I'm not sure if
		// it would be worth it, at the moment, to focus on this at the moment.
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				@SuppressWarnings("unchecked")
				int cmp1 = entry.getKey().compareTo((K)key1);
				@SuppressWarnings("unchecked")
				int cmp2 = entry.getKey().compareTo((K)key2);
				
				if (cmp1 >= 0 && cmp2 <= 0)
					values.add(entry);
				
				if (cmp2 >= 0)
					return values;
			}
			current = current.next;
		}
		
		return values;
	}

	/**
	 * Puts an entry into the B+ Tree with a specified
	 * key and value.
	 * 
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 * 
	 * @param key    Key of the entry
	 * @param value  Value of the entry
	 * @return       Value of the entry stored
	 */
	@Override
	public V put(K key, V value) {
		// Create entry and insert it
		KeyVal<K,V> entry = new KeyVal<K,V>(key, value);
		V oldVal = root.insert(entry);
		
		if (root.tooLarge()) {
			SplitResult sr = root.split();
			
			InnerNode newRoot = new InnerNode(this);
			newRoot.makeRoot(sr.midKey, root, sr.splitNode);
			root = newRoot;
		}
		
		return oldVal;
	}

	/**
	 * Remove entry that has this key.  If the entry is not in the B+ Tree
	 * nothing will happen.
	 * 
	 * @see java.util.Map#remove(java.lang.Object)
	 * 
	 * @param key  Key of entry to remove.
	 * @return     Value of entry that had been removed, or null if not found.
	 */
	@Override
	public V remove(Object key) {
		@SuppressWarnings("unchecked")
		K k = (K)key;
		
		V oldVal = root.remove(k);
		
		if (root instanceof BPTree.InnerNode && root.size() == 1) {
			@SuppressWarnings("unchecked")
			InnerNode oldRoot = (InnerNode)root; 
			root = oldRoot.children.get(0).getValue();
		}
		
		return oldVal;
	}

	/**
	 * Put all of the entries from the specified Map into this B+ Tree.
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 * 
	 * @param m  Map to copy entries from.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Clear the map so there are no entries in it.
	 * 
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		root = new LeafNode(this);
	}

	/**
	 * Returns a set containing all of the keys in the B+ Tree.
	 * 
	 * @see java.util.Map#keySet()
	 * 
	 * @return  Set of keys.
	 */
	@Override
	public Set<K> keySet() {
		HashSet<K> keys = new HashSet<K>(entryCount);
		
		LeafNode current = root.getFirstLeaf();
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				keys.add(entry.getKey());
			}
			current = current.next;
		}
		
		return keys;
	}

	/**
	 * Returns a collection of values stored in the B+ Tree.
	 * 
	 * @see java.util.Map#values()
	 * 
	 * @return  Collection of values
	 */
	@Override
	public Collection<V> values() {
		ArrayList<V> values = new ArrayList<V>(entryCount);
		
		LeafNode current = root.getFirstLeaf();
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				values.add(entry.getValue());
			}
			current = current.next;
		}
		
		return values;
	}

	/**
	 * Returns a set of entries from the B+ Tree.
	 * 
	 * @see java.util.Map#entrySet()
	 * 
	 * @return  A Set of entries from the B+ Tree
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		HashSet<Entry<K,V>> entries = new HashSet<Entry<K,V>>(entryCount);
		
		LeafNode current = root.getFirstLeaf();
		
		while (current != null) {
			for (KeyVal<K,V> entry : current.entries) {
				entries.add(entry);
			}
			current = current.next;
		}
		
		return entries;
	}
	
	/**
	 * Prints the full tree to System.out -- for debugging.
	 */
	public void printFullTree() {
		root.printFullTree(0);
	}
}
