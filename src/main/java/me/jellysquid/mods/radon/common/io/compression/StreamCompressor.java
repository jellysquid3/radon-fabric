package me.jellysquid.mods.radon.common.io.compression;

import java.nio.ByteBuffer;

public interface StreamCompressor {
    ByteBuffer compress(ByteBuffer in);

    ByteBuffer decompress(ByteBuffer in);
}
