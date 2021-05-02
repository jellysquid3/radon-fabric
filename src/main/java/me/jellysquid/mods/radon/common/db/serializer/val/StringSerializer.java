package me.jellysquid.mods.radon.common.db.serializer.val;

import me.jellysquid.mods.radon.common.db.serializer.ValueSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements ValueSerializer<String> {
    @Override
    public ByteBuffer serialize(String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
        buf.put(data);

        return buf;
    }

    @Override
    public String deserialize(ByteBuffer input) {
        return StandardCharsets.UTF_8.decode(input)
                .toString();
    }
}
