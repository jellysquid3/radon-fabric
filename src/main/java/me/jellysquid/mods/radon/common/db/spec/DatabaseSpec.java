package me.jellysquid.mods.radon.common.db.spec;

import me.jellysquid.mods.radon.common.io.compression.StreamCompressor;

public class DatabaseSpec<K, V> {
    private final String name;

    private final Class<K> key;
    private final Class<V> value;

    private final StreamCompressor compressor;
    private final int initialSize;

    public DatabaseSpec(String name, Class<K> key, Class<V> value, StreamCompressor compressor, int initialSize) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.compressor = compressor;
        this.initialSize = initialSize;
    }

    public Class<K> getKeyType() {
        return this.key;
    }

    public Class<V> getValueType() {
        return this.value;
    }

    public StreamCompressor getCompressor() {
        return this.compressor;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("DatabaseSpec{key=%s, value=%s}@%s", this.key.getName(), this.value.getName(), this.hashCode());
    }

    public int getInitialSize() {
        return this.initialSize;
    }
}
