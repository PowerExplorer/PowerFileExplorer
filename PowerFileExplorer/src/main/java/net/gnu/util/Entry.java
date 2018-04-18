//package net.gnu.util;
//import java.util.*;
//
//public class Entry<K, V> implements Map.Entry<K, V>{
//
//	private static final long serialVersionUID = 58875847614548649L;
//	private K key;
//	private V value;
//
//	public Entry(K key, V value) {
//		this.key = key;
//		this.value = value;
//	}
//
//	public K getKey() {
//		return key;
//	}
//
//	public V getValue() {
//		return value;
//	}
//
//	public K setKey(K key) {
//		K oldValue = this.key;
//		this.key = key;
//		return oldValue;
//	}
//
//	public V setValue(V value) {
//		V oldValue = this.value;
//		this.value = value;
//		return oldValue;
//	}
//
//	public boolean equals(Object o) {
//		if (!(o instanceof Entry))
//			return false;
//		Entry<?, ?> e = (Entry<?, ?>) o;
//		return (key == null ? e.getKey() == null : key.equals(e.getKey()))
//			&& (value == null ? e.getValue() == null : value.equals(e.getValue()));
//	}
//
//	public int hashCode() {
//		return (key == null ? 0 : key.hashCode())
//			^ (value == null ? 0 : value.hashCode());
//	}
//
//	public String toString() {
//		return key + "=" + value;
//	}
//}
