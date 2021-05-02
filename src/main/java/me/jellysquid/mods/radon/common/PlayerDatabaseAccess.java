package me.jellysquid.mods.radon.common;

import me.jellysquid.mods.radon.common.db.LMDBInstance;

public interface PlayerDatabaseAccess {
    LMDBInstance getDatabase();
}
