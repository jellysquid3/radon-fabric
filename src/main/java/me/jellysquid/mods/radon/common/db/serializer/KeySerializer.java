package me.jellysquid.mods.radon.common.db.serializer;

import java.nio.ByteBuffer;

public interface KeySerializer<T> {
    void serializeKey(ByteBuffer buf, T value);

    int getKeyLength();
}
