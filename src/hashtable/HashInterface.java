package hashtable;

import java.util.List;

public interface HashInterface<K, V> {

    V put(K key, V value);
    V get(K key);
    boolean contains(K key);
    int getSize();
    int getCapacity();
    long getCollisionCount();
    void resetCollisionCount();
}
