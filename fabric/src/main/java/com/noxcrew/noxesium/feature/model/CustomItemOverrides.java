package com.noxcrew.noxesium.feature.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.nbt.NumericTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A complete re-implementation of the item overrides system. We do diverge slightly from vanilla behaviour because
 * it doesn't really make sense. Vanilla's algorithm goes as follows:
 * - Start iterating over all overrides bottom up
 * - Find the first override that is exceeded by the values, e.g. if the bottom model has custom model data 5, an item
 * with custom model data 6 will match that override. Even if another override exists for model data 6.
 * <p>
 * Instead, this patch uses a hashmap for custom model data, if and only if, such a hashmap will not cause any differences
 * to vanilla behaviour.
 */
public class CustomItemOverrides extends ItemOverrides {

    /**
     * A pre-built empty overrides instance.
     */
    public static final CustomItemOverrides EMPTY = new CustomItemOverrides();
    private static final ResourceLocation CUSTOM_MODEL_DATA_ID = new ResourceLocation("custom_model_data");

    private ResourceLocation[] properties;

    /**
     * All basic fallback overrides.
     */
    private List<BakedOverride> overrides;

    /**
     * All models that are directly determined by a single custom model data in the correct order.
     */
    private Map<Integer, BakedModel> customModelDatas;

    /**
     * All optional model ranges.
     */
    private List<Triple<Integer, Integer, BakedModel>> optionalRanges;

    /**
     * The lowest valid custom model data integer. No new values below this are entered as any such
     * values would use a different custom model.
     */
    private Integer lowestCustomModelData;

    /**
     * The highest valid custom model data integer. Any values above this use this value.
     */
    private Integer highestCustomModelData;

    public CustomItemOverrides() {
        super();
    }

    public CustomItemOverrides(ModelBaker modelBaker, BlockModel blockModel, List<ItemOverride> list) {
        super(modelBaker, blockModel, list);

        // Ignore empty inputs
        if (list.isEmpty()) return;

        // Determine all properties used
        properties = list.stream()
                .flatMap(ItemOverride::getPredicates)
                .map(ItemOverride.Predicate::getProperty)
                .distinct()
                .toArray(ResourceLocation[]::new);

        // Cache the indices for each property
        var indices = new HashMap<ResourceLocation, Integer>();
        Integer customModelIndex = null;
        for (var i = 0; i < properties.length; i++) {
            if (Objects.equals(properties[i], CUSTOM_MODEL_DATA_ID)) {
                customModelIndex = i;
            }
            indices.put(properties[i], i);
        }

        // Iterate through the list in reverse order
        var canOptimize = true;
        var modelCache = new HashMap<ResourceLocation, BakedModel>();
        for (var i = list.size() - 1; i >= 0; --i) {
            var override = list.get(i);

            // Micro-optimization: cache the model in case it's used multiple times per model, we ditch this hashmap after
            // this constructor anyway, and it only stores references. But if we had to make the model multiple times it'd be
            // much more costly!
            var bakedmodel = modelCache.computeIfAbsent(override.getModel(), (t) -> bakeModel(modelBaker, blockModel, override));
            var properties = override.getPredicates().map((predicate) -> {
                var index = indices.get(predicate.getProperty());
                return new Property(index, predicate.getValue());
            }).toArray(Property[]::new);

            // Test if this is a custom model property
            if (properties.length == 1 && customModelIndex != null && Objects.equals(customModelIndex, properties[0].index)) {
                if (customModelDatas == null) {
                    // Only create the hashmap if we're using it
                    customModelDatas = new HashMap<>();
                }
                var value = (int) properties[0].minimum;

                // We only enter the custom model data if it's the new minimum. Otherwise any of the other
                // models would have taken up this model instead.
                if (lowestCustomModelData == null || value < lowestCustomModelData) {
                    customModelDatas.put(value, bakedmodel);
                    lowestCustomModelData = value;

                    // Cache the highest value
                    if (highestCustomModelData == null || value > highestCustomModelData) {
                        highestCustomModelData = value;
                    }
                }
            } else {
                // Indicate that there is a non-custom model data override!
                canOptimize = false;
            }

            // Add this model to the overrides list
            if (overrides == null) {
                overrides = new ArrayList<>();
            }
            overrides.add(new BakedOverride(bakedmodel, properties));
        }

        // Determine if we can optimize and clear the appropriate maps
        if (!canOptimize) {
            customModelDatas = null;
        } else {
            overrides = null;

            if (customModelDatas != null) {
                // Fill in the gaps in the custom model data, so we can fetch them all properly
                Map.Entry<Integer, BakedModel> lastPair = null;
                var copy = new ArrayList<>(new HashMap<>(customModelDatas).entrySet());
                copy.sort(Map.Entry.comparingByKey());
                for (var pair : copy) {
                    if (lastPair != null) {
                        // Go over each index between these two pairs
                        var distance = (pair.getKey() - 1) - (lastPair.getKey() + 1);
                        if (distance > 0) {
                            if (optionalRanges == null) {
                                optionalRanges = new ArrayList<>();
                            }
                            optionalRanges.add(Triple.of(lastPair.getKey() + 1, pair.getKey() - 1, lastPair.getValue()));
                        }
                    }
                    lastPair = pair;
                }
            }
        }
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel fallback, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        // Try to test individual overrides if possible
        if (overrides != null) {
            var item = itemStack.getItem();

            // Determine the values for each property, this is decently slow but we assume
            // few overrides need this behaviour.
            var values = new float[properties.length];
            for (int index = 0; index < properties.length; ++index) {
                var resourcelocation = properties[index];
                var itempropertyfunction = ItemProperties.getProperty(item, resourcelocation);
                if (itempropertyfunction != null) {
                    values[index] = itempropertyfunction.call(itemStack, clientLevel, livingEntity, i);
                } else {
                    values[index] = Float.NEGATIVE_INFINITY;
                }
            }

            // Go through all baked overrides in order and find one that applies
            for (var baked : overrides) {
                if (baked.test(values)) {
                    var bakedmodel = baked.model;
                    return bakedmodel == null ? fallback : bakedmodel;
                }
            }
        } else if (customModelDatas != null) {
            // Determine the custom model of the item as fast as possible (this code is called a lot per tick if there's many models)
            var customTag = itemStack.getTag();
            if (customTag == null) return fallback;
            var customModelData = customTag.get("CustomModelData");
            if (customModelData == null || customModelData.getId() > 6) return fallback;
            var numericTag = (NumericTag) customModelData;
            var id = numericTag.getAsInt();

            // Snap to the highest valid id
            if (highestCustomModelData != null && id > highestCustomModelData) {
                id = highestCustomModelData;
            }

            // If the id is below the minimum we will never find a hit and
            // we simply return the fallback
            if (lowestCustomModelData != null && id < lowestCustomModelData) {
                return fallback;
            }

            // Try to get the exact model from the cache
            var model = customModelDatas.get(id);
            if (model != null) {
                return model;
            }

            if (optionalRanges != null) {
                // We fall somewhere in between, let's assess the ranges
                for (var triple : optionalRanges) {
                    if (id >= triple.getLeft() && id <= triple.getMiddle()) {
                        return triple.getRight();
                    }
                }
            }
        }
        return fallback;
    }

    /**
     * Stores a single model and its properties.
     */
    private record BakedOverride(BakedModel model, Property[] properties) {
        /**
         * Tests the required properties for this override against
         * the inputs.
         */
        boolean test(float[] values) {
            for (var property : properties) {
                var value = values[property.index];
                if (value < property.minimum) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The minimum value for each property.
     */
    private record Property(int index, float minimum) {
    }
}
