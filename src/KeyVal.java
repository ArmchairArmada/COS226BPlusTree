import java.util.Map.Entry;


/**
 * A Key Value pair.
 * 
 * Even though it implements the Entry interface, I'm not 100% its behavior
 * will be entirely correct, so use with caution.
 * 
 * @author Nathan
 *
 * @param <K>  Key type
 * @param <V>  Value type
 */
public class KeyVal<K extends Comparable<K>,V> implements Comparable<KeyVal<K,V>>, Entry<K,V> {
	/**
	 * Boxing keys can simplify making an "infinite" key.
	 * 
	 * @author Nathan
	 *
	 */
	private class Key implements Comparable<Key> {
		public K k;
		
		public Key(K key) {
			k = key;
		}
		
		@Override
		public int compareTo(Key other) {
			if (other instanceof KeyVal.InfKey)
				return -1;
			return k.compareTo(other.k);
		}
		
	}
	
	/**
	 * An infinite key that is larger than everything.
	 * 
	 * @author Nathan
	 *
	 */
	private class InfKey extends Key {
		public InfKey() {
			super(null);
		}
		
		@Override
		public int compareTo(Key other) {
			return 1;
		}
	}
	
	
	private Key k;
	private V v;
	
	/**
	 * Constructor for key value pair
	 * 
	 * @param key    Key
	 * @param value  Value
	 */
	public KeyVal(K key, V value) {
		k = new Key(key);
		v = value;
	}
	
	/**
	 * Constructor for key value pair with infinite key.
	 * 
	 * @param value  Value
	 */
	public KeyVal(V value) {
		k = new InfKey();
		v = value;
	}
	
	/**
	 * Check if it has an infinite key.
	 * 
	 * @return  True if infinite, else false
	 */
	public boolean hasInfKey() {
		return k instanceof KeyVal.InfKey;
	}
	
	/**
	 * Get key from the key value pair.  This should be checked, first, to
	 * see if it is infinite.
	 * 
	 * @return  Key
	 */
	public K getKey() {
		return k.k;
	}
	
	/**
	 * Get value from the key value pair.
	 * 
	 * @return  Value
	 */
	public V getValue() {
		return v;
	}
	
	/**
	 * Set the key to a new key value.
	 * 
	 * @param newKey  New Key
	 */
	public void setKey(K newKey) {
		k = new Key(newKey);
	}
	
	/**
	 * Set the key using the key from another key value pair.
	 * 
	 * @param kv  KeyVal to get a key from.
	 */
	public void setKey(KeyVal<K,V> kv) {
		if (kv.hasInfKey()) {
			k = new InfKey();
		}
		else {
			k = new Key(kv.getKey());
		}
	}
	
	/**
	 * Set the key to be infinite
	 */
	public void setKeyInf() {
		k = new InfKey();
	}
	
	/**
	 * Set key value pair to a new value.
	 * 
	 * @return Old Value
	 */
	public V setValue(V newValue) {
		V oldVal = v;
		v = newValue;
		return oldVal;
	}

	/**
	 * Compare this KeyValue pair to another.
	 * 
	 * @param other  KeyVal to compare to
	 * @return   Greater than, equal to, or less than 0 depending on comparison
	 */
	@Override
	public int compareTo(KeyVal<K, V> other) {
		return k.compareTo(other.k);
	}
	
	/**
	 * String representation
	 * 
	 * @return  String
	 */
	public String toString() {
		if (hasInfKey()) {
			return "(Inf: " + v + ")";
		}
		return "(" + k.k + ": " + v + ")";
	}
}
