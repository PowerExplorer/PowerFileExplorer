package net.gnu.util;

import java.util.Map;

public class ComparableEntry<K extends Comparable<K>, V> implements
		Map.Entry<K, V>, java.io.Serializable,
		Comparable<ComparableEntry<K, V>> {

	private static final long serialVersionUID = 5887584761454864149L;
	private K key;
	private V value;

	public ComparableEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public K setKey(K key) {
		K oldValue = this.key;
		this.key = key;
		return oldValue;
	}

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ComparableEntry))
			return false;
		ComparableEntry<?, ?> e = (ComparableEntry<?, ?>) o;
		return key == null ? e.getKey() == null : key.equals(e.getKey());
	}

	public int hashCode() {
		return (key == null ? 0 : key.hashCode())
				^ (value == null ? 0 : value.hashCode());
	}

	public String toString() {
		return key + "=" + value;
	}

	@Override
	public int compareTo(ComparableEntry<K, V> o) {
		return this.getKey().compareTo(o.getKey());
	}

}
