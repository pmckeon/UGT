package psidum.ugt.util;

public class HashKey {
    int hashCode;

    int[] data;

    public HashKey(int[] data) {
        this.data = data;
        this.hashCode = MurmurHash3.murmurhash3_x86_32(data, 0, MurmurHash3.seed);
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HashKey hashKey = (HashKey)o;
        for (int i = 0; i < this.data.length; i++) {
            if (hashKey.data[i] != this.data[i])
                return false;
        }
        return true;
    }

    public int hashCode() {
        return this.hashCode;
    }
}
