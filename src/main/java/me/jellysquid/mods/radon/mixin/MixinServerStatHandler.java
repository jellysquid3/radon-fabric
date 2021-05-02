package me.jellysquid.mods.radon.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ServerStatHandler.class)
public abstract class MixinServerStatHandler extends StatHandler implements DatabaseItem {
    private LMDBInstance storage;

    @Shadow
    protected abstract String asString();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract <T> Optional<Stat<T>> createStat(StatType<T> type, String id);

    @Shadow
    private static CompoundTag jsonToCompound(JsonObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    @Final
    private File file;

    @Shadow
    @Final
    private MinecraftServer server;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectDisableFileLoad(File file) {
        return false;
    }

    @Overwrite
    public void save() {
        this.storage
                .getTransaction(PlayerDatabaseSpecs.STATISTICS)
                .add(this.getUuid(), this.asString());
    }


    @Override
    public LMDBInstance getStorage() {
        return this.storage;
    }

    @Override
    public void setStorage(LMDBInstance storage) {
        this.storage = storage;

        String json = this.storage
                .getDatabase(PlayerDatabaseSpecs.STATISTICS)
                .getValue(this.getUuid());

        if (json == null) {
            return;
        }

        try {
            this.parse(this.server.getDataFixer(), json);
        } catch (JsonParseException var5) {
            LOGGER.error("Couldn't parse statistics file for player {}", this.getUuid(), var5);
        } catch (Exception var4) {
            LOGGER.error("Couldn't read statistics file for player {}", this.getUuid(), var4);
        }
    }

    @Overwrite
    public void parse(DataFixer dataFixer, String json) {
        try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
            jsonReader.setLenient(false);

            JsonElement jsonElement = Streams.parse(jsonReader);

            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from player {}", this.getUuid());
                return;
            }

            CompoundTag tag = jsonToCompound(jsonElement.getAsJsonObject());

            if (!tag.contains("DataVersion", 99)) {
                tag.putInt("DataVersion", 1343);
            }

            tag = NbtHelper.update(dataFixer, DataFixTypes.STATS, tag, tag.getInt("DataVersion"));

            if (!tag.contains("stats", 10)) {
                return;
            }

            CompoundTag stats = tag.getCompound("stats");

            for (String string : stats.getKeys()) {
                if (!stats.contains(string, 10)) {
                    continue;
                }

                Util.ifPresentOrElse(Registry.STAT_TYPE.getOrEmpty(new Identifier(string)), (statType) -> {
                    CompoundTag compoundTag2x = stats.getCompound(string);

                    for (String string2 : compoundTag2x.getKeys()) {
                        if (!compoundTag2x.contains(string2, 99)) {
                            LOGGER.warn("Invalid statistic value on player {}: Don't know what {} is for key {}", this.getUuid(), compoundTag2x.get(string2), string2);
                            continue;
                        }

                        Util.ifPresentOrElse(this.createStat(statType, string2), (stat) -> {
                            this.statMap.put(stat, compoundTag2x.getInt(string2));
                        }, () -> LOGGER.warn("Invalid statistic on player {}: Don't know what {} is", this.getUuid(), string2));
                    }
                }, () -> LOGGER.warn("Invalid statistic type on player {}: Don't know what {} is", this.getUuid(), string));
            }
        } catch (IOException | JsonParseException var21) {
            LOGGER.error("Unable to parse Stat data for player {}", this.getUuid(), var21);
        }
    }

    private UUID getUuid() {
        return UUID.fromString(this.file.getName().replace(".json", ""));
    }
}
