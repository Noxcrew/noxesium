package com.noxcrew.noxesium.core.fabric.feature.misc;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.core.nms.registry.NmsGameComponentTypes;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

/**
 * Adds an extra creative tab which contains various items defined by the server. This allows
 * servers to add custom items and have them easily show up in the clients.
 */
public class CustomServerCreativeItems {

    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, "server_items"));

    public CustomServerCreativeItems() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB,
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup.noxesium.server_items"))
                        .displayItems((parameters, output) -> output.acceptAll(GameComponents.getInstance()
                                .noxesium$getComponentOr(NmsGameComponentTypes.CUSTOM_CREATIVE_ITEMS, List::of)))
                        .icon(() -> {
                            var item = new ItemStack(Items.STRUCTURE_BLOCK);
                            item.set(
                                    DataComponents.CUSTOM_MODEL_DATA,
                                    new CustomModelData(
                                            List.of(), List.of(), List.of("server_items_header"), List.of()));
                            return item;
                        })
                        .build());
    }
}
