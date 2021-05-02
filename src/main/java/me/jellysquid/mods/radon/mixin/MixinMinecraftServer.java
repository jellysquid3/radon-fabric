package me.jellysquid.mods.radon.mixin;

import me.jellysquid.mods.radon.common.PlayerDatabaseAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Shadow private PlayerManager playerManager;

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void postTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ((PlayerDatabaseAccess) this.playerManager)
                .getDatabase()
                .flushChanges();
    }

    @Inject(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;saveAllPlayerData()V"))
    private void postSaveAllPlayerData(CallbackInfo ci) {
        ((PlayerDatabaseAccess) this.playerManager)
                .getDatabase()
                .close();
    }
}
