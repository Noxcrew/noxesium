package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataHolder;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements [ExtraEntityDataHolder] onto the Entity class.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements ExtraEntityDataHolder {

    @Unique
    private Map<Integer, Object> noxesium$extraData = null;

    @Override
    public <T> T noxesium$getExtraData(ClientServerRule<T> rule) {
        if (noxesium$extraData != null) {
            var data = noxesium$extraData.get(rule.getIndex());
            if (data != null) {
                return (T) data;
            }
        }
        return ExtraEntityDataHolder.super.noxesium$getExtraData(rule);
    }

    @Override
    public boolean noxesium$hasExtraData(ClientServerRule<?> rule) {
        return noxesium$extraData != null && noxesium$extraData.containsKey(rule.getIndex());
    }

    @Override
    public void noxesium$setExtraData(ClientServerRule<?> rule, Object value) {
        if (noxesium$extraData == null) {
            noxesium$extraData = new HashMap<>();
        }
        noxesium$extraData.put(rule.getIndex(), value);

        // If this is the width of an interaction entity we update its bounding box!
        if (rule == ExtraEntityData.QIB_WIDTH_Z) {
            this.setBoundingBox(this.makeBoundingBox());
        }
    }

    @Override
    public void noxesium$resetExtraData(ClientServerRule<?> rule) {
        if (noxesium$extraData == null) return;
        noxesium$extraData.remove(rule.getIndex());
        if (noxesium$extraData.isEmpty()) {
            noxesium$extraData = null;
        }
    }

    @Shadow
    public abstract void setBoundingBox(AABB aABB);

    @Shadow
    protected abstract AABB makeBoundingBox();
}
