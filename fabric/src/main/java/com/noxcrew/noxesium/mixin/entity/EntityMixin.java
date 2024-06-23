package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.entity.ExtraEntityDataHolder;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements [ExtraEntityDataHolder] onto the Entity class.
 */
@Mixin(Entity.class)
public class EntityMixin implements ExtraEntityDataHolder {

    @Unique
    private Map<Integer, Object> noxesium$extraData = null;

    @Override
    public <T> T getExtraData(ClientServerRule<T> rule) {
        if (noxesium$extraData != null) {
            var data = noxesium$extraData.get(rule);
            if (data != null) {
                return (T) data;
            }
        }
        return ExtraEntityDataHolder.super.getExtraData(rule);
    }

    @Override
    public void setExtraData(ClientServerRule<?> rule, Object value) {
        if (noxesium$extraData == null) {
            noxesium$extraData = new HashMap<>();
        }
        noxesium$extraData.put(rule.getIndex(), value);
    }

    @Override
    public void resetExtraData(ClientServerRule<?> rule) {
        if (noxesium$extraData == null) return;
        noxesium$extraData.remove(rule.getIndex());
        if (noxesium$extraData.isEmpty()) {
            noxesium$extraData = null;
        }
    }
}
