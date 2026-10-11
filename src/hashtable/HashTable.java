package hashtable;

import java.util.ArrayList;
import java.util.List;

public class HashTable<K, V> implements HashInterface<K, V> {
    private HashEntry<K, V>[] table;
    private int size;
    private int capacity;
    private double maxLoadFactor;
    private long collisionCount;
    private HashFunctionType hashFunctionType;
    private CollisionType collisionType;

    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity, double loadFactor, HashFunctionType hashFunctionType, CollisionType collisionType) {
        this.capacity = nextPrime(initialCapacity);
        this.maxLoadFactor = loadFactor;
        this.hashFunctionType = hashFunctionType;
        this.collisionType = collisionType;
        this.size = 0;
        this.collisionCount = 0;
        this.table = (HashEntry<K, V>[]) new HashEntry[capacity];
    }

    //returns the first prime number bigger than or equal to n
    private int nextPrime(int n) {
        int x = Math.max(n, 2);
        while (!isPrime(x)) {
            x++;
        }
        return x;
    }

    //returns true if x is true
    private boolean isPrime(int x) {
        if (x < 2) return false;
        if (x == 2) return true;
        if (x % 2 == 0) return false;
        for (int i = 3; i * i <= x; i += 2) {
            if (x % i == 0) return false;
        }
        return true;
    }

    //returns the biggest prime number less than n
    //used for double hashing's second function
    private int previousPrime(int n) {
        int x = n-1;
        while (x>=2 && !isPrime(x)) {
            x--;
        }
        return x;
    }

    //alpha
    //returns the load factor at that time
    private double loadFactor() {
        return (double) size/capacity;
    }

    @Override
    public long getCollisionCount() {
        return collisionCount;
    }

    @Override
    public void resetCollisionCount() {
        collisionCount = 0;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    //turns every string to lower case
    //sums every string as a number(a=1, b=2, ... ,z=26) and returns the sum
    private int computeSSF(String str) {
        str = str.toLowerCase();
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                sum += c - 'a' + 1;
            }
        }
        return sum;
    }

    //turns every string to lower case
    //turns every string to a number (a=1, b=2, ... ,z=26)
    //returns sum = sum * z + value (z=33 (a prime num))
    private int computePAF(String str) {
        str = str.toLowerCase();
        int sum = 0;
        int z = 33;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int value = 0;
            if (c >= 'a' && c <= 'z') {
                value += c - 'a' + 1;
            }
            sum = sum * z + value;
        }
        return sum;
    }

    //uses SSF or PAF on the string according to the hash function type
    //returns compressed hash (|hash|%capacity)
    private int primaryHash(K key) {
        int hashCode;
        if (key instanceof String) {
            String str = (String) key;
            if (hashFunctionType == HashFunctionType.SSF) {
                hashCode = computeSSF(str);
            } else {
                hashCode = computePAF(str);
            }
        } else {
            hashCode = key.hashCode();
        }
        hashCode = Math.abs(hashCode);
        return hashCode % capacity;
    }

    //calculates step for double hashing
    private int secondHash(K key) {
        int hashCode;
        if (key instanceof String) {
            String str = (String) key;
            if (hashFunctionType == HashFunctionType.SSF) {
                hashCode = computeSSF(str);
            } else {
                hashCode = computePAF(str);
            }
        } else {
            hashCode = key.hashCode();
        }
        hashCode = Math.abs(hashCode);
        int q = previousPrime(capacity);
        int d = q - (hashCode % q);
        //if d=0 then it tries the same place over and over again so make it at least 1
        if (d == 0) d = 1;
        return d;
    }

    //used for resizing when a certain load factor point is exceeded
    @SuppressWarnings("unchecked")
    private void resize() {
        HashEntry<K, V>[] oldTable = table;
        int oldCapacity = capacity;
        //capacity becomes 2 times bigger and then it is rounded to the next prime
        capacity = nextPrime(oldCapacity * 2);
        table = (HashEntry<K, V>[]) new HashEntry[capacity];
        int oldSize = size;
        size = 0;

        long oldCollisions = collisionCount;
        collisionCount = 0;

        //put every entry in the old table to the new one (one by one)
        for (int i = 0; i < oldCapacity; i++) {
            HashEntry<K, V> e = oldTable[i];
            if (e != null) {
                put(e.getKey(), e.getValue());
            }
        }
        collisionCount += oldCollisions;
        size = oldSize;
    }

    //puts key, value pairs into hash table
    @Override
    public V put(K key, V value) {
        //if load factor exceeds a certain point it gets resized
        if (loadFactor() > maxLoadFactor) {
            resize();
        }
        //first index is found by primary hash
        int hashIndex = primaryHash(key);
        //step is 1 for linear probing and secondHash(key) for double hashing
        int step = 1;
        if (collisionType == CollisionType.DOUBLE_HASHING) {
            step = secondHash(key);
            if (step == 0) {
                step = 1;
            }
        }
        int index = hashIndex;
        int i = 0;

        while (true) {
            HashEntry<K, V> entry = table[index];
            //if place is null just put it there
            if (entry == null) {
                table[index] = new HashEntry<>(key, value);
                size++;
                return null;
            }
            //if there is the same key replace the value with the new one
            else if (entry.getKey().equals(key)) {
                V oldValue = entry.getValue();
                entry.setValue(value);
                return oldValue;
            }
            //if there is a different key, that means a collision has happened
            else {
                collisionCount++;
                i++;
                if (i >= capacity) {
                    throw new IllegalStateException("hashtable is full. cannot add more.");
                }

                //index = (hash + i *step) %capacity
                //for the risk of overflow and negative mod result, long is used
                long offset;
                if (collisionType == CollisionType.LINEAR_PROBING) {
                    offset = i;
                } else {
                    offset = (long) i * (long) step;
                }
                long tmp = (long) hashIndex + offset;
                int newIndex = (int) (tmp % capacity);
                if (newIndex < 0) {
                    newIndex += capacity; //if the mod is negative, normalize it
                }
                index = newIndex;
            }
        }
    }

    //probes until it finds a null place or the key searched and returns it
    @Override
    public V get(K key) {
        int hashIndex = primaryHash(key);
        int step = 1;
        if (collisionType == CollisionType.DOUBLE_HASHING) {
            step = secondHash(key);
            if (step == 0) {
                step = 1;
            }
        }
        int index = hashIndex;
        int i = 0;

        while (true) {
            HashEntry<K, V> entry = table[index];
            //null - the key isnt in the table
            if (entry == null) {
                return null;
            }
            //the key is found - return its value
            else if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
            //differint key - keep probing
            else {
                i++;
                if (i >= capacity) {
                    throw new IllegalStateException("key not found after probing all of the table");
                }

                //the same probing as the put
                long offset;
                if (collisionType == CollisionType.LINEAR_PROBING) {
                    offset = i;
                } else {
                    offset = (long) i * (long) step;
                }
                long tmp = (long) hashIndex + offset;
                int newIndex = (int) (tmp % capacity);
                if (newIndex < 0) {
                    newIndex += capacity;
                }
                index = newIndex;
            }
        }
    }

    //returns true if the key is found in the table, false if not
    @Override
    public boolean contains(K key) {
        return get(key) != null;
    }

}
