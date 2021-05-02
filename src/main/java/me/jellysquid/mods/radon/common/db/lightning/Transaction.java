package me.jellysquid.mods.radon.common.db.lightning;

import org.lwjgl.system.MemoryStack;

@FunctionalInterface
interface Transaction<T> {
    T exec(MemoryStack stack, long txn);

}