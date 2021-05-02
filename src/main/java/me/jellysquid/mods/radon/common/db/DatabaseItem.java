package me.jellysquid.mods.radon.common.db;

public interface DatabaseItem {
    void setStorage(LMDBInstance holder);

    LMDBInstance getStorage();
}
