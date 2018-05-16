public class Pair<K, V> {
  final K key;
  final V val;

  public Pair(K key, V val) {
    this.key = key;
    this.val = val;
  }

  public K getKey() {
    return key;
  }

  public V getVal() {
    return val;
  }
}