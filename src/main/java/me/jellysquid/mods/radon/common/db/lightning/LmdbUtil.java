package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.lmdb.LMDB;

public class LmdbUtil {
    public static <T> T transaction(Env env, Transaction<T> transaction) {
        T ret;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            LmdbUtil.checkError(LMDB.mdb_txn_begin(env.raw(), MemoryUtil.NULL, 0, pp));
            long txn = pp.get(0);

            int err;

            try {
                ret = transaction.exec(stack, txn);
                err = LMDB.mdb_txn_commit(txn);
            } catch (Throwable t) {
                LMDB.mdb_txn_abort(txn);
                throw t;
            }

            LmdbUtil.checkError(err);
        }

        return ret;
    }

    public static void checkError(int rc) {
        if (rc != LMDB.MDB_SUCCESS) {
            throw new LmdbException(rc);
        }
    }
}
