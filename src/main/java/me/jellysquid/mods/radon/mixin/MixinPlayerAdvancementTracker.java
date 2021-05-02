package me.jellysquid.mods.radon.mixin;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(PlayerAdvancementTracker.class)
public abstract class MixinPlayerAdvancementTracker implements DatabaseItem {
    @Shadow @Final private Map<Advancement, AdvancementProgress> advancementToProgress;

    @Shadow @Final private static Gson GSON;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private ServerPlayerEntity owner;

    @Shadow @Final private static TypeToken<Map<Identifier, AdvancementProgress>> JSON_TYPE;

    @Shadow @Final private DataFixer field_25324;

    @Shadow protected abstract void initProgress(Advancement advancement, AdvancementProgress progress);

    @Shadow protected abstract void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader);

    @Shadow protected abstract void updateCompleted();

    @Shadow protected abstract void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader);

    private LMDBInstance storage;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;load(Lnet/minecraft/server/ServerAdvancementLoader;)V"))
    private void redirectInitialLoad(PlayerAdvancementTracker playerAdvancementTracker, ServerAdvancementLoader advancementLoader) {

    }

    @Overwrite
    private void load(ServerAdvancementLoader advancementLoader) {
        String json = this.storage
                .getDatabase(PlayerDatabaseSpecs.ADVANCEMENTS)
                .getValue(this.getUuid());

        if (json != null) {
            try {
                JsonReader jsonReader = new JsonReader(new StringReader(json));
                jsonReader.setLenient(false);

                Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(jsonReader));

                if (!dynamic.get("DataVersion").asNumber().result().isPresent()) {
                    dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
                }

                dynamic = this.field_25324.update(DataFixTypes.ADVANCEMENTS.getTypeReference(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getGameVersion().getWorldVersion());
                dynamic = dynamic.remove("DataVersion");

                Map<Identifier, AdvancementProgress> map = GSON.getAdapter(JSON_TYPE)
                        .fromJsonTree(dynamic.getValue());

                if (map == null) {
                    throw new JsonParseException("Found null for advancements");
                }

                Stream<Map.Entry<Identifier, AdvancementProgress>> stream = map.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue());

                for (Map.Entry<Identifier, AdvancementProgress> entry : stream.collect(Collectors.toList())) {
                    Advancement advancement = advancementLoader.get(entry.getKey());

                    if (advancement == null) {
                        LOGGER.warn("Ignored advancement '{}' in progress file for player {} - it doesn't exist anymore?", entry.getKey(), this.getUuid());
                    } else {
                        this.initProgress(advancement, entry.getValue());
                    }
                }
            } catch (JsonParseException e) {
                LOGGER.error("Couldn't parse player advancements for player {}", this.getUuid(), e);
            } catch (Exception e) {
                LOGGER.error("Couldn't read player advancements for player {}", this.getUuid(), e);
            }
        }

        this.rewardEmptyAdvancements(advancementLoader);
        this.updateCompleted();
        this.beginTrackingAllAdvancements(advancementLoader);
    }

    @Overwrite
    public void save() {
        Map<Identifier, AdvancementProgress> map = Maps.newHashMap();

        for (Map.Entry<Advancement, AdvancementProgress> entry : this.advancementToProgress.entrySet()) {
            AdvancementProgress advancementProgress = entry.getValue();

            if (advancementProgress.isAnyObtained()) {
                map.put(entry.getKey().getId(), advancementProgress);
            }
        }

        JsonElement json = GSON.toJsonTree(map);
        json.getAsJsonObject()
                .addProperty("DataVersion", SharedConstants.getGameVersion().getWorldVersion());

        try (StringWriter writer = new StringWriter()) {
            GSON.toJson(json, writer);

            this.storage
                    .getTransaction(PlayerDatabaseSpecs.ADVANCEMENTS)
                    .add(this.getUuid(), writer.toString());
        } catch (IOException var35) {
            LOGGER.error("Couldn't save player advancements for {}", this.getUuid());
        }
    }

    private UUID getUuid() {
        return this.owner.getUuid();
    }

    @Override
    public void setStorage(LMDBInstance storage) {
        this.storage = storage;

        this.load(this.owner.getServerWorld().getServer().getAdvancementLoader());
    }

    @Override
    public LMDBInstance getStorage() {
        return this.storage;
    }
}
