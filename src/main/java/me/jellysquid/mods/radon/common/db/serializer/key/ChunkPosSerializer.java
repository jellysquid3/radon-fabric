package me.jellysquid.mods.radon.common.db.serializer.key;

import me.jellysquid.mods.radon.common.db.serializer.KeySerializer;
import net.minecraft.util.math.ChunkPos;

import java.nio.ByteBuffer;

public class ChunkPosSerializer implements KeySerializer<ChunkPos> {
    @Override
    public void serializeKey(ByteBuffer buf, ChunkPos value) {
        buf.putInt(0, value.x);
        buf.putInt(4, value.z);
    }

    @Override
    public int getKeyLength() {
        return 8;
    }
}
