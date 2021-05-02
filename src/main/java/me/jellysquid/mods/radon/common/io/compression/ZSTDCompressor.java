package me.jellysquid.mods.radon.common.io.compression;

import org.lwjgl.util.zstd.Zstd;

import java.nio.ByteBuffer;

public class ZSTDCompressor implements StreamCompressor {
    @Override
    public ByteBuffer compress(ByteBuffer src) {
        ByteBuffer dst = ByteBuffer.allocateDirect((int) Zstd.ZSTD_COMPRESSBOUND(src.remaining()));
        dst.limit((int) checkError(Zstd.ZSTD_compress(dst, src, 7)));

        return dst;
    }

    @Override
    public ByteBuffer decompress(ByteBuffer src) {
        ByteBuffer dst = ByteBuffer.allocateDirect((int) checkError(Zstd.ZSTD_getFrameContentSize(src)));
        checkError(Zstd.ZSTD_decompress(dst, src));

        return dst;
    }

    private static long checkError(long rc) {
        if (Zstd.ZSTD_isError(rc)) {
            throw new IllegalStateException(Zstd.ZSTD_getErrorName(rc));
        }

        return rc;
    }
}
