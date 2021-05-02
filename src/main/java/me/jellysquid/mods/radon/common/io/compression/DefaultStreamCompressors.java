package me.jellysquid.mods.radon.common.io.compression;

import org.lwjgl.util.zstd.Zstd;

import java.nio.ByteBuffer;

public class DefaultStreamCompressors {
    public static final StreamCompressor NONE = new StreamCompressor() {
        @Override
        public ByteBuffer compress(ByteBuffer in) {
            return in;
        }

        @Override
        public ByteBuffer decompress(ByteBuffer in) {
            return in;
        }
    };

    public static final StreamCompressor ZSTD = new ZSTDCompressor();
}
