package com.noxcrew.noxesium.rule;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.Objects;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

/**
 * A custom implementation of [AdventureModeCheck] that takes in a list of strings as data.
 */
public class CustomAdventureModeCheck {
    private final List<String> input;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public CustomAdventureModeCheck(List<String> input) {
        this.input = input;
    }

    private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2, boolean bl) {
        if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
            return false;
        }
        if (!bl) {
            return true;
        }
        if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
            return true;
        }
        if (blockInWorld.getEntity() == null || blockInWorld2.getEntity() == null) {
            return false;
        }
        return Objects.equals(blockInWorld.getEntity().saveWithId(), blockInWorld2.getEntity().saveWithId());
    }

    public boolean test(Registry<Block> registry, BlockInWorld blockInWorld) {
        if (areSameBlocks(blockInWorld, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        }
        this.lastCheckedBlock = blockInWorld;
        this.checksBlockEntity = false;
        for (String string : input) {
            try {
                BlockPredicateArgument.Result result = BlockPredicateArgument.parse(registry.asLookup(), new StringReader(string));
                this.checksBlockEntity |= result.requiresNbt();
                if (result.test(blockInWorld)) {
                    this.lastResult = true;
                    return true;
                }
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        this.lastResult = false;
        return false;
    }
}

