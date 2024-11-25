package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * A standard server rule that stores a mapping of qib behaviors.
 */
public class QibBehaviorServerRule extends ClientServerRule<Map<String, QibDefinition>> {

    private final Map<String, QibDefinition> defaultValue;

    public QibBehaviorServerRule(int index) {
        this(index, Map.of());
    }

    public QibBehaviorServerRule(int index, Map<String, QibDefinition> defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Map<String, QibDefinition> getDefault() {
        return defaultValue;
    }

    @Override
    public Map<String, QibDefinition> read(RegistryFriendlyByteBuf buffer) {
        var amount = buffer.readVarInt();
        var array = new HashMap<String, QibDefinition>(amount);
        for (int i = 0; i < amount; i++) {
            var key = buffer.readUtf();
            var value = buffer.readUtf();
            array.put(key, QibDefinition.QIB_GSON.fromJson(value, QibDefinition.class));
        }
        return array;
    }

    @Override
    public void write(Map<String, QibDefinition> value, RegistryFriendlyByteBuf buffer) {
        buffer.writeCollection(value.entrySet(), (buf, entry) -> {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(QibDefinition.QIB_GSON.toJson(entry.getValue()));
        });
    }
}
