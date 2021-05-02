package me.jellysquid.mods.radon.mixin;

import com.mojang.datafixers.DataFixer;
import me.jellysquid.mods.radon.common.ChunkDatabaseAccess;
import me.jellysquid.mods.radon.common.db.LMDBInstance;
import me.jellysquid.mods.radon.common.db.spec.DatabaseSpec;
import me.jellysquid.mods.radon.common.db.spec.impl.WorldDatabaseSpecs;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {
    @Shadow
    @Final
    private PointOfInterestStorage pointOfInterestStorage;

    private LMDBInstance storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(ServerWorld serverWorld, LevelStorage.Session session, DataFixer dataFixer, StructureManager structureManager, Executor workerExecutor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, Supplier<PersistentStateManager> supplier, int i, boolean bl, CallbackInfo ci) {
        this.storage = new LMDBInstance(session.getWorldDirectory(serverWorld.getRegistryKey()), "chunks", new DatabaseSpec[] {
                WorldDatabaseSpecs.CHUNK_DATA,
                WorldDatabaseSpecs.POI
        });

        ((ChunkDatabaseAccess) this.pointOfInterestStorage)
                .setDatabase(this.storage);

        ((ChunkDatabaseAccess) this)
                .setDatabase(this.storage);
    }

    @Inject(method = "save(Z)V", at = @At("RETURN"))
    private void postSaveChunks(boolean flush, CallbackInfo ci) {
        this.flushChunks();
    }

    @Inject(method = "unloadChunks", at = @At("RETURN"))
    private void postUnloadChunks(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.flushChunks();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void postTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.flushChunks();
    }

    private void flushChunks() {
        this.storage.flushChanges();
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void postClose(CallbackInfo ci) {
        this.storage.close();
    }
}
