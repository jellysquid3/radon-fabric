package me.jellysquid.mods.radon.mixin;

import me.jellysquid.mods.radon.common.PlayerDatabaseAccess;
import me.jellysquid.mods.radon.common.db.DatabaseItem;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.spec.impl.PlayerDatabaseSpecs;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(PlayerManager.class)
public class MixinPlayerManager implements PlayerDatabaseAccess {
    @Shadow
    @Final
    private WorldSaveHandler saveHandler;

    private LMDBInstance storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(MinecraftServer server, DynamicRegistryManager.Impl registryManager, WorldSaveHandler saveHandler, int maxPlayers, CallbackInfo ci) {
        File dir = server.getSavePath(WorldSavePath.ADVANCEMENTS).getParent().toFile();

        this.storage = new LMDBInstance(dir, "players", new DatabaseSpec[] {
                PlayerDatabaseSpecs.PLAYER_DATA,
                PlayerDatabaseSpecs.ADVANCEMENTS,
                PlayerDatabaseSpecs.STATISTICS
        });

        ((DatabaseItem) this.saveHandler)
                .setStorage(this.storage);
    }

    @Inject(method = "getAdvancementTracker", at = @At("RETURN"))
    private void postGetAdvancementTracker(ServerPlayerEntity player, CallbackInfoReturnable<PlayerAdvancementTracker> cir) {
        DatabaseItem item = (DatabaseItem) cir.getReturnValue();

        if (item.getStorage() == null) {
            item.setStorage(this.storage);
        }
    }

    @Inject(method = "createStatHandler", at = @At("RETURN"))
    private void postCreateStatHandler(PlayerEntity player, CallbackInfoReturnable<ServerStatHandler> cir) {
        DatabaseItem item = (DatabaseItem) cir.getReturnValue();

        if (item.getStorage() == null) {
            item.setStorage(this.storage);
        }
    }

    @Override
    public LMDBInstance getDatabase() {
        return this.storage;
    }
}
