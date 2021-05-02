package me.jellysquid.mods.radon.common.db.serializer.key;

import me.jellysquid.mods.radon.common.db.serializer.KeySerializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDSerializer implements KeySerializer<UUID> {
    @Override
    public void serializeKey(ByteBuffer buf, UUID value) {
        buf.putLong(0, value.getLeastSignificantBits());
        buf.putLong(8, value.getMostSignificantBits());
    }

    @Override
    public int getKeyLength() {
        return Long.BYTES * 2;
    }
}
