package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.util.lmdb.LMDB;

public class LmdbException extends RuntimeException {
    private final int code;

    public LmdbException(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return LMDB.mdb_strerror(this.code);
    }
}
