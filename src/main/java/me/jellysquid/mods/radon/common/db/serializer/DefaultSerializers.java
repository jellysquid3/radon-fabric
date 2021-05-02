package me.jellysquid.mods.radon.common.db.serializer;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.radon.common.db.serializer.val.CompoundTagSerializer;
import me.jellysquid.mods.radon.common.db.serializer.val.StringSerializer;
import me.jellysquid.mods.radon.common.db.serializer.key.ChunkPosSerializer;
import me.jellysquid.mods.radon.common.db.serializer.key.ChunkSectionPosSerializer;
import me.jellysquid.mods.radon.common.db.serializer.key.UUIDSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.UUID;

public class DefaultSerializers {
    private static final Reference2ReferenceMap<Class<?>, KeySerializer<?>> keySerializers = new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceMap<Class<?>, ValueSerializer<?>> valueSerializers = new Reference2ReferenceOpenHashMap<>();

    static {
        keySerializers.put(UUID.class, new UUIDSerializer());
        keySerializers.put(ChunkSectionPos.class, new ChunkSectionPosSerializer());
        keySerializers.put(ChunkPos.class, new ChunkPosSerializer());

        valueSerializers.put(CompoundTag.class, new CompoundTagSerializer());
        valueSerializers.put(String.class, new StringSerializer());
    }

    @SuppressWarnings("unchecked")
    public static <K> KeySerializer<K> getKeySerializer(Class<K> clazz) {
        KeySerializer<?> serializer = keySerializers.get(clazz);

        if (serializer == null) {
            throw new NullPointerException("No serializer exists for type: " + clazz.getName());
        }

        return (KeySerializer<K>) serializer;
    }

    @SuppressWarnings("unchecked")
    public static <K> ValueSerializer<K> getValueSerializer(Class<K> clazz) {
        ValueSerializer<?> serializer = valueSerializers.get(clazz);

        if (serializer == null) {
            throw new NullPointerException("No serializer exists for type: " + clazz.getName());
        }

        return (ValueSerializer<K>) serializer;
    }
}
