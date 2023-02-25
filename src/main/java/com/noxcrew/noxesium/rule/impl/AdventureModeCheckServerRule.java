package com.noxcrew.noxesium.rule.impl;

import com.noxcrew.noxesium.rule.ServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * A server rule that stores a [CustomAdventureModeCheck].
 */
public class AdventureModeCheckServerRule extends ServerRule<CustomAdventureModeCheck> {

    public AdventureModeCheckServerRule(int index) {
        super(index);
    }

    @Override
    public CustomAdventureModeCheck getDefault() {
        return new CustomAdventureModeCheck(List.of());
    }

    @Override
    public CustomAdventureModeCheck read(FriendlyByteBuf buffer) {
        var stringList = buffer.readList(FriendlyByteBuf::readUtf);
        return new CustomAdventureModeCheck(stringList);
    }
}
