package io.github.msdk.io.mgf;

/**
 * Created by evger on 17-May-18.
 */
public class Pair<K, V> {
  private K key;
  private V value;
  Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }


  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }


}
