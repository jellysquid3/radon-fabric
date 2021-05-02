package me.jellysquid.mods.radon.common.db.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ValueSerializer<T> {
    ByteBuffer serialize(T value) throws IOException;

    T deserialize(ByteBuffer input) throws IOException;
}
