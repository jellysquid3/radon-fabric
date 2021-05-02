package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.lmdb.LMDB;
import org.lwjgl.util.lmdb.MDBVal;

import java.nio.ByteBuffer;

public class Dbi {
    private final Env env;
    private final int dbi;

    public Dbi(Env env, int dbi) {
        this.env = env;
        this.dbi = dbi;
    }

    public void put(Txn txn, ByteBuffer keyBuf, ByteBuffer valueBuf, int flags) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MDBVal key = new MDBVal(stack.malloc(MDBVal.SIZEOF));
            key.mv_data(keyBuf);
            key.mv_size(keyBuf.remaining());

            MDBVal value = new MDBVal(stack.malloc(MDBVal.SIZEOF));
            value.mv_data(valueBuf);
            value.mv_size(valueBuf.remaining());

            LmdbUtil.checkError(LMDB.mdb_put(txn.raw(), this.dbi, key, value, flags));
        }
    }

    public void close() {
        LMDB.mdb_dbi_close(this.env.raw(), this.dbi);
    }

    public ByteBuffer get(Txn txn, ByteBuffer keyBuf) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            MDBVal key = new MDBVal(stack.malloc(MDBVal.SIZEOF));
            key.mv_data(keyBuf);
            key.mv_size(keyBuf.remaining());

            MDBVal value = new MDBVal(stack.malloc(MDBVal.SIZEOF));

            int result = LMDB.mdb_get(txn.raw(), this.dbi, key, value);

            if (result == LMDB.MDB_NOTFOUND) {
                return null;
            }

            LmdbUtil.checkError(result);

            return value.mv_data();
        }
    }
}
