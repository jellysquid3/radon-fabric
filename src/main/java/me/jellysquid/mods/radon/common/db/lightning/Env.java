package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lmdb.LMDB;
import org.lwjgl.util.lmdb.MDBEnvInfo;

import java.io.File;
import java.nio.IntBuffer;

public class Env {
    private final long env;

    Env(long env) {
        this.env = env;
    }

    public Dbi openDbi(String name, int flags) {
        return LmdbUtil.transaction(this, (stack, txn) -> {
            IntBuffer ib = stack.mallocInt(1);
            LmdbUtil.checkError(LMDB.mdb_dbi_open(txn, name, flags, ib));

            return new Dbi(this, ib.get(0));
        });
    }

    public static Builder builder() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pb = stack.mallocPointer(1);
            LmdbUtil.checkError(LMDB.mdb_env_create(pb));

            return new Env.Builder(pb.get(0));
        }
    }

    public EnvInfo getInfo() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MDBEnvInfo info = new MDBEnvInfo(stack.malloc(MDBEnvInfo.SIZEOF));
            LmdbUtil.checkError(LMDB.mdb_env_info(this.env, info));

            return new EnvInfo(info);
        }
    }

    public void setMapSize(long size) {
        LmdbUtil.checkError(LMDB.mdb_env_set_mapsize(this.env, size));
    }

    public Txn txnWrite() {
        return this.txn(0);
    }

    public Txn txnRead() {
        return this.txn(LMDB.MDB_RDONLY);
    }

    private Txn txn(int flags) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pb = stack.mallocPointer(1);
            LmdbUtil.checkError(LMDB.mdb_txn_begin(this.env, MemoryUtil.NULL, flags, pb));

            return new Txn(pb.get(0));
        }
    }

    public void close() {
        LMDB.mdb_env_close(this.env);
    }

    long raw() {
        return this.env;
    }

    public static class Builder {
        private final long pointer;

        public Builder(long pointer) {
            this.pointer = pointer;
        }

        public Builder setMaxDatabases(int limit) {
            LmdbUtil.checkError(LMDB.mdb_env_set_maxdbs(this.pointer, limit));

            return this;
        }

        public Env open(File file, int flags) {
            return this.open(file.getAbsolutePath(), flags);
        }

        public Env open(String path, int flags) {
            LmdbUtil.checkError(LMDB.mdb_env_open(this.pointer, path, flags, 0_664));

            return new Env(this.pointer);
        }
    }
}
