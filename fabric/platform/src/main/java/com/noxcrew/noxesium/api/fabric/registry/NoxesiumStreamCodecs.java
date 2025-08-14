package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.datafixers.util.Function12;
import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import io.netty.buffer.ByteBuf;
import java.awt.Color;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.modcommon.impl.AdventureByteBufCodecs;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines extra stream codecs used by Noxesium.
 */
public class NoxesiumStreamCodecs {

    public static final StreamCodec<ByteBuf, Unit> UNIT = StreamCodec.unit(Unit.INSTANCE);

    public static final StreamCodec<FriendlyByteBuf, Integer> COLOR_INT_ARGB = new StreamCodec<>() {
        public Integer decode(FriendlyByteBuf buffer) {
            return buffer.readVarInt();
        }

        public void encode(FriendlyByteBuf buffer, Integer value) {
            buffer.writeVarInt(value);
        }
    };

    public static final StreamCodec<FriendlyByteBuf, Color> COLOR_ARGB = COLOR_INT_ARGB.map(Color::new, Color::getRGB);

    public static final StreamCodec<ByteBuf, Key> KEY = AdventureByteBufCodecs.KEY;

    public static final StreamCodec<RegistryFriendlyByteBuf, Component> COMPONENT =
            ComponentSerialization.STREAM_CODEC.map(
                    NonWrappingComponentSerializer.INSTANCE::deserialize,
                    NonWrappingComponentSerializer.INSTANCE::serialize);

    public static final StreamCodec<ByteBuf, ClientSettings> CLIENT_SETTINGS =
            StreamCodec.recursive(codec -> StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientSettings::configuredGuiScale,
                    ByteBufCodecs.DOUBLE,
                    ClientSettings::trueGuiScale,
                    ByteBufCodecs.VAR_INT,
                    ClientSettings::width,
                    ByteBufCodecs.VAR_INT,
                    ClientSettings::height,
                    ByteBufCodecs.BOOL,
                    ClientSettings::enforceUnicode,
                    ByteBufCodecs.BOOL,
                    ClientSettings::touchScreenMode,
                    ByteBufCodecs.DOUBLE,
                    ClientSettings::notificationDisplayTime,
                    ClientSettings::new));

    public static final StreamCodec<ByteBuf, EntrypointProtocol> ENTRYPOINT_PROTOCOL =
            StreamCodec.recursive(codec -> StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    EntrypointProtocol::id,
                    ByteBufCodecs.VAR_INT,
                    EntrypointProtocol::protocolVersion,
                    ByteBufCodecs.STRING_UTF8,
                    EntrypointProtocol::rawVersion,
                    EntrypointProtocol::new));

    public static <T extends Enum<?>> StreamCodec<ByteBuf, T> forEnum(Class<T> clazz) {
        return new StreamCodec<>() {
            @Override
            public void encode(ByteBuf buffer, T value) {
                buffer.writeByte(value.ordinal());
            }

            @Override
            public T decode(ByteBuf buffer) {
                return clazz.getEnumConstants()[buffer.readByte()];
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, NoxesiumComponentPatch> noxesiumComponentPatch(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
        return new StreamCodec<>() {
            @Override
            public NoxesiumComponentPatch decode(RegistryFriendlyByteBuf buffer) {
                var updated = buffer.readVarInt();
                var removed = buffer.readVarInt();

                if (updated == 0 && removed == 0) {
                    return new NoxesiumComponentPatch(new HashMap<>());
                } else {
                    var total = updated + removed;
                    var map = new ConcurrentHashMap<NoxesiumComponentType<?>, Optional<?>>(total);

                    for (int i = 0; i < updated; i++) {
                        var index = buffer.readVarInt();
                        var type = registry.getById(index);
                        if (type == null) {
                            throw new IllegalArgumentException(
                                    "Received invalid component id '" + index + "' not found in registry");
                        }
                        var serializer = ComponentSerializerRegistry.getSerializers(registry, type);
                        if (serializer == null) {
                            throw new IllegalArgumentException(
                                    "Found no serializer for component type '" + type.id() + "' in registry");
                        }
                        if (serializer.streamCodec() == null) {
                            throw new IllegalArgumentException("Received component type '" + type.id()
                                    + "' that does not have a stream codec defined");
                        }
                        var decoded = serializer.streamCodec().decode(buffer);
                        map.put(type, Optional.of(decoded));
                    }

                    for (int i = 0; i < removed; i++) {
                        var index = buffer.readVarInt();
                        var type = registry.getById(index);
                        if (type == null) {
                            throw new IllegalArgumentException(
                                    "Received invalid component id '" + index + "' not found in registry");
                        }
                        map.put(type, Optional.empty());
                    }

                    return new NoxesiumComponentPatch(map);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NoxesiumComponentPatch patch) {
                if (patch.isEmpty()) {
                    buffer.writeVarInt(0);
                    buffer.writeVarInt(0);
                } else {
                    // Start by writing how many of each type there are
                    var updated = 0;
                    var removed = 0;

                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isPresent()) {
                            updated++;
                        } else {
                            removed++;
                        }
                    }

                    buffer.writeVarInt(updated);
                    buffer.writeVarInt(removed);

                    // Write the components themselves
                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isPresent()) {
                            buffer.writeVarInt(registry.getIdFor(entry.getKey()));
                            encodeComponent(
                                    buffer, entry.getKey(), entry.getValue().get());
                        }
                    }
                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isEmpty()) {
                            buffer.writeVarInt(registry.getIdFor(entry.getKey()));
                        }
                    }
                }
            }

            private <T> void encodeComponent(
                    RegistryFriendlyByteBuf buffer, NoxesiumComponentType<T> type, Object raw) {
                var serializer = ComponentSerializerRegistry.getSerializers(registry, type);
                if (serializer == null) {
                    throw new IllegalArgumentException(
                            "Found no serializer for component type '" + type.id() + "' in registry");
                }
                serializer.streamCodec().cast().encode(buffer, (T) raw);
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> streamCodec,
            final Function<C, T1> function,
            final StreamCodec<? super B, T2> streamCodec2,
            final Function<C, T2> function2,
            final StreamCodec<? super B, T3> streamCodec3,
            final Function<C, T3> function3,
            final StreamCodec<? super B, T4> streamCodec4,
            final Function<C, T4> function4,
            final StreamCodec<? super B, T5> streamCodec5,
            final Function<C, T5> function5,
            final StreamCodec<? super B, T6> streamCodec6,
            final Function<C, T6> function6,
            final StreamCodec<? super B, T7> streamCodec7,
            final Function<C, T7> function7,
            final StreamCodec<? super B, T8> streamCodec8,
            final Function<C, T8> function8,
            final StreamCodec<? super B, T9> streamCodec9,
            final Function<C, T9> function9,
            final StreamCodec<? super B, T10> streamCodec10,
            final Function<C, T10> function10,
            final StreamCodec<? super B, T11> streamCodec11,
            final Function<C, T11> function11,
            final StreamCodec<? super B, T12> streamCodec12,
            final Function<C, T12> function12,
            final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> function122) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B object) {
                T1 object2 = (T1) streamCodec.decode(object);
                T2 object3 = (T2) streamCodec2.decode(object);
                T3 object4 = (T3) streamCodec3.decode(object);
                T4 object5 = (T4) streamCodec4.decode(object);
                T5 object6 = (T5) streamCodec5.decode(object);
                T6 object7 = (T6) streamCodec6.decode(object);
                T7 object8 = (T7) streamCodec7.decode(object);
                T8 object9 = (T8) streamCodec8.decode(object);
                T9 object10 = (T9) streamCodec9.decode(object);
                T10 object11 = (T10) streamCodec10.decode(object);
                T11 object12 = (T11) streamCodec11.decode(object);
                T12 object13 = (T12) streamCodec12.decode(object);
                return (C) function122.apply(
                        object2, object3, object4, object5, object6, object7, object8, object9, object10, object11,
                        object12, object13);
            }

            @Override
            public void encode(B object, C object2) {
                streamCodec.encode(object, function.apply(object2));
                streamCodec2.encode(object, function2.apply(object2));
                streamCodec3.encode(object, function3.apply(object2));
                streamCodec4.encode(object, function4.apply(object2));
                streamCodec5.encode(object, function5.apply(object2));
                streamCodec6.encode(object, function6.apply(object2));
                streamCodec7.encode(object, function7.apply(object2));
                streamCodec8.encode(object, function8.apply(object2));
                streamCodec9.encode(object, function9.apply(object2));
                streamCodec10.encode(object, function10.apply(object2));
                streamCodec11.encode(object, function11.apply(object2));
                streamCodec12.encode(object, function12.apply(object2));
            }
        };
    }
}
