package com.noxcrew.noxesium.feature;

import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Adds an extra creative tab which contains various items defined by the server. This allows
 * servers to add custom items and have them easily show up in the clients.
 */
public class CustomServerCreativeItems implements NoxesiumModule {

    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, "server_items"));

    @Override
    public void onStartup() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB,
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.noxesium.server_items"))
                        .displayItems(
                                (parameters, output) -> output.acceptAll(ServerRules.CUSTOM_CREATIVE_ITEMS.getValue()))
                        .icon(() -> new ItemStack(Items.STRUCTURE_BLOCK))
                        .build());
    }
}
