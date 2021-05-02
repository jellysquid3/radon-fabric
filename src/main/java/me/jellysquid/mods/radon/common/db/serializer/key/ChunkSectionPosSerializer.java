package me.jellysquid.mods.radon.common.db.serializer.key;

import me.jellysquid.mods.radon.common.db.serializer.KeySerializer;
import net.minecraft.util.math.ChunkSectionPos;

import java.nio.ByteBuffer;

public class ChunkSectionPosSerializer implements KeySerializer<ChunkSectionPos> {
    @Override
    public void serializeKey(ByteBuffer buf, ChunkSectionPos value) {
        buf.putInt(0, value.getX());
        buf.putInt(4, value.getY());
        buf.putInt(8, value.getZ());
    }

    @Override
    public int getKeyLength() {
        return 12;
    }
}
