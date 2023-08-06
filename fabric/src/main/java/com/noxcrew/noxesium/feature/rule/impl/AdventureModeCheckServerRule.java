package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * A server rule that stores a [CustomAdventureModeCheck].
 */
public class AdventureModeCheckServerRule extends ClientServerRule<CustomAdventureModeCheck> {

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
