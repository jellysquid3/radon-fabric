package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.util.lmdb.LMDB;

public class Txn {
    private final long id;

    Txn(long pointer) {
        this.id = pointer;
    }

    public void commit() {
        LmdbUtil.checkError(LMDB.mdb_txn_commit(this.id));
    }

    public void abort() {
        LMDB.mdb_txn_abort(this.id);
    }

    public long raw() {
        return this.id;
    }
}
