package com.noxcrew.noxesium.feature.rule.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * A standard server rule that stores a mapping of qib behaviors.
 */
public class QibBehaviorServerRule extends ClientServerRule<Map<String, QibDefinition>> {

    private static final Gson gson = new GsonBuilder().create();
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
    public Map<String, QibDefinition> read(FriendlyByteBuf buffer) {
        var amount = buffer.readVarInt();
        var array = new HashMap<String, QibDefinition>(amount);
        for (int i = 0; i < amount; i++) {
            var key = buffer.readUtf();
            var value = buffer.readUtf();
            array.put(key, gson.fromJson(value, QibDefinition.class));
        }
        return array;
    }
}
