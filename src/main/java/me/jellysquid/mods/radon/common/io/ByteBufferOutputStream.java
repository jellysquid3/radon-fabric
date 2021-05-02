package me.jellysquid.mods.radon.common.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
    private final ByteBuffer buf;

    public ByteBufferOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public void write(int value) {
        this.buf.put((byte) value);
    }

    @Override
    public void write(byte @NotNull [] bytes, int off, int len) {
        this.buf.put(bytes, off, len);
    }
}
