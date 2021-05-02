package me.jellysquid.mods.radon.common.db;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.lightning.Env;
import me.jellysquid.mods.radon.common.db.lightning.EnvInfo;
import me.jellysquid.mods.radon.common.db.lightning.LmdbException;
import me.jellysquid.mods.radon.common.db.lightning.Txn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.lmdb.LMDB;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LMDBInstance {
    private static final long MAP_RESIZE_STEP = 16 * 1024 * 1024;
    private static final Logger LOGGER = LogManager.getLogger("Argon");

    protected final Env env;
    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVDatabase<?, ?>> databases = new Reference2ObjectOpenHashMap<>();
    protected final Reference2ObjectMap<DatabaseSpec<?, ?>, KVTransaction<?, ?>> transactions = new Reference2ObjectOpenHashMap<>();

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LMDBInstance(File dir, String name, DatabaseSpec<?, ?>[] databases) {
        if (!dir.isDirectory() && dir.mkdirs()) {
            throw new RuntimeException("Couldn't create directory: " + dir);
        }

        File file = new File(dir, name + ".db");

        this.env = Env.builder()
                .setMaxDatabases(databases.length)
                .open(file, LMDB.MDB_NOLOCK | LMDB.MDB_NOSUBDIR);

        EnvInfo info = this.env.getInfo();

        int initialSize = Arrays.stream(databases).mapToInt(DatabaseSpec::getInitialSize).sum();

        if (info.mapSize < initialSize) {
            this.env.setMapSize(initialSize);
        }

        for (DatabaseSpec<?, ?> spec : databases) {
            KVDatabase<?, ?> database = new KVDatabase<>(this, spec);

            this.databases.put(spec, database);
            this.transactions.put(spec, new KVTransaction<>(database));
        }
    }

    @SuppressWarnings("unchecked")
    public <K, V> KVDatabase<K, V> getDatabase(DatabaseSpec<K, V> spec) {
        KVDatabase<?, ?> database = this.databases.get(spec);

        if (database == null) {
            throw new NullPointerException("No database is registered for spec " + spec);
        }

        return (KVDatabase<K, V>) database;
    }

    @SuppressWarnings("unchecked")
    public <K, V> KVTransaction<K, V> getTransaction(DatabaseSpec<K, V> spec) {
        KVTransaction<?, ?> transaction = this.transactions.get(spec);

        if (transaction == null) {
            throw new NullPointerException("No transaction is registered for spec " + spec);
        }

        return (KVTransaction<K, V>) transaction;
    }

    public void flushChanges() {
        this.lock.writeLock()
                .lock();

        try {
            this.commitTransaction();
        } finally {
            this.lock.writeLock()
                    .unlock();
        }
    }

    private void commitTransaction() {
        Iterator<KVTransaction<?, ?>> it = this.transactions.values()
                .iterator();

        Txn txn = this.env.txnWrite();

        try {
            while (it.hasNext()) {
                try {
                    KVTransaction<?, ?> transaction = it.next();
                    transaction.addChanges(txn);
                } catch (LmdbException e) {
                    if (e.getCode() == LMDB.MDB_MAP_FULL) {
                        LOGGER.warn("Map became full during commit; Requesting " +
                                "another {} bytes and restarting transaction", MAP_RESIZE_STEP);

                        txn.abort();

                        this.growMap(MAP_RESIZE_STEP);

                        txn = this.env.txnWrite();
                        it = this.transactions.values()
                                .iterator();
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Throwable t) {
            txn.abort();

            throw t;
        }

        txn.commit();

        this.transactions.values()
                .forEach(KVTransaction::clear);
    }

    private void growMap(long size) {
        EnvInfo info = this.env.getInfo();
        long newSize = info.mapSize + size;
        LOGGER.info("Growing map size from {} to {} bytes", info.mapSize, newSize);
        this.env.setMapSize(newSize);
    }

    ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    Env env() {
        return this.env;
    }

    public void close() {
        this.flushChanges();

        for (KVDatabase<?, ?> database : this.databases.values()) {
            database.close();
        }

        this.env.close();
    }
}
