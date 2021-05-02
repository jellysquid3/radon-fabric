package me.jellysquid.mods.radon.common.db.spec.impl;

import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.io.compression.DefaultStreamCompressors;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class PlayerDatabaseSpecs {
    public static final DatabaseSpec<UUID, String> ADVANCEMENTS =
            new DatabaseSpec<>("advancements", UUID.class, String.class, DefaultStreamCompressors.ZSTD, 128 * 1024);

    public static final DatabaseSpec<UUID, String> STATISTICS =
            new DatabaseSpec<>("statistics", UUID.class, String.class, DefaultStreamCompressors.ZSTD, 128 * 1024);

    public static final DatabaseSpec<UUID, CompoundTag> PLAYER_DATA =
            new DatabaseSpec<>("player_data", UUID.class, CompoundTag.class, DefaultStreamCompressors.ZSTD, 128 * 1024);
}
