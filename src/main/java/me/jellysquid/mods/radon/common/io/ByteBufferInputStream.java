package me.jellysquid.mods.radon.common.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer buf;

    public ByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() {
        if (!this.buf.hasRemaining()) {
            return -1;
        }

        return Byte.toUnsignedInt(this.buf.get());
    }

    @Override
    public int read(byte @NotNull [] bytes, int off, int len) {
        if (!this.buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, this.buf.remaining());

        this.buf.get(bytes, off, len);

        return len;
    }
}