package com.noxcrew.noxesium.feature;

import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Adds an extra creative tab which contains various items defined by the server. This allows
 * servers to add custom items and have them easily show up in the clients.
 */
public class CustomServerCreativeItems implements NoxesiumModule {

    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, "server_items"));

    @SubscribeEvent
    public static void registerCreativeTab(RegisterEvent event) {
        event.register(
                Registries.CREATIVE_MODE_TAB,
                registry -> {
                    registry.register(
                            CREATIVE_TAB,
                            CreativeModeTab.builder()
                                    .title(Component.translatable("itemGroup.noxesium.server_items"))
                                    .displayItems((parameters, output) -> output.acceptAll(ServerRules.CUSTOM_CREATIVE_ITEMS.getValue()))
                                    .icon(() -> new ItemStack(Items.STRUCTURE_BLOCK))
                                    .build()
                    );
                }
        );
    }
}
