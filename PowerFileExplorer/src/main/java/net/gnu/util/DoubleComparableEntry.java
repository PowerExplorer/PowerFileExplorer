package net.gnu.util;

import java.util.Comparator;
import java.util.Map;

public class DoubleComparableEntry<K extends Comparable<K>, V extends Comparable<V>>
		implements Map.Entry<K, V>, java.io.Serializable,
		Comparable<DoubleComparableEntry<K, V>> {

	private static final long serialVersionUID = 5887584761454864149L;
	private K key;
	private V value;

	public DoubleComparableEntry(K key, V value) {
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
		if (!(o instanceof DoubleComparableEntry))
			return false;
		DoubleComparableEntry<?, ?> e = (DoubleComparableEntry<?, ?>) o;
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
	public int compareTo(DoubleComparableEntry<K, V> o) {
		return this.getKey().compareTo(o.getKey());
	}

	public static class RevertValueOrder<K extends Comparable<K>, V extends Comparable<V>>
			implements Comparator<DoubleComparableEntry<K, V>> {

		@Override
		public int compare(DoubleComparableEntry<K, V> e1,
				DoubleComparableEntry<K, V> e2) {
			if (e1.getValue().equals(e2.getValue())) {
				return e1.getKey().compareTo(e2.getKey());
			} else {
				return -(e1.getValue().compareTo(e2.getValue()));
			}
		}
	}
}
