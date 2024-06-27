package com.noxcrew.noxesium.feature.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Determines if custom item overrides are solely for key/value mappings of custom model
 * data, in which case we build a hash map representation for them and use that instead.
 */
public class CustomItemOverrides extends ItemOverrides {

    /**
     * The maximum level of precision available. Any values beyond this
     * cannot be optimized.
     */
    private final static int MAXIMUM_PRECISION = 16777216;

    /**
     * The identifier of the custom model data tag.
     */
    private static final ResourceLocation CUSTOM_MODEL_DATA_ID = ResourceLocation.withDefaultNamespace("custom_model_data");

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

    public CustomItemOverrides(ModelBaker modelBaker, BlockModel blockModel, List<ItemOverride> list) {
        super(modelBaker, blockModel, list);

        // Ignore empty inputs
        if (list.isEmpty()) return;

        // Iterate through the list in reverse order
        var canOptimize = true;
        var modelCache = new HashMap<ResourceLocation, BakedModel>();
        for (var i = list.size() - 1; i >= 0; --i) {
            var override = list.get(i);

            // Determine if this override has one predicate and if it's a custom model data
            var first = new MutableBoolean(false);
            var invalid = new MutableBoolean(false);
            var customModelData = new MutableObject<Float>(null);
            override.getPredicates().forEach(predicate -> {
                // If we find any second element it's invalid
                if (first.getValue()) {
                    invalid.setValue(true);
                    return;
                }

                // Mark down whenever we find the first element
                first.setValue(true);

                // If this is custom model data we set the value
                if (Objects.equals(predicate.getProperty(), CUSTOM_MODEL_DATA_ID)) {
                    customModelData.setValue(predicate.getValue());
                }
            });

            // If there's more no element or more than one element, it's invalid
            if (!first.getValue() || invalid.getValue()) {
                canOptimize = false;
                break;
            }

            // If the one value is not a custom model data value, it's invalid
            var rawValue = customModelData.getValue();
            if (rawValue == null) {
                canOptimize = false;
                break;
            }

            // Cast the value to the closest integer
            var value = (int) rawValue.floatValue();

            // If the value exceeds the precision threshold when the property is cast
            // to a float it mismatches and becomes a higher number which no longer
            // matches with the true value we check later, so we need to mark the entire
            // overrides as non-optimizable!
            if (rawValue >= MAXIMUM_PRECISION || rawValue <= -MAXIMUM_PRECISION) {
                canOptimize = false;
                break;
            }

            // We only enter the custom model data if it's the new minimum. Otherwise any of the other
            // models would have taken up this model instead.
            if (lowestCustomModelData == null || value < lowestCustomModelData) {
                // Micro-optimization: cache the model in case it's used multiple times per model, we ditch this hashmap after
                // this constructor anyway, and it only stores references. But if we had to make the model multiple times it'd be
                // much more costly!
                var bakedmodel = modelCache.computeIfAbsent(override.getModel(), (t) -> bakeModel(modelBaker, blockModel, override));

                // Only create the hashmap if we're using it
                if (customModelDatas == null) {
                    customModelDatas = new HashMap<>();
                }

                customModelDatas.put(value, bakedmodel);
                lowestCustomModelData = value;

                // Cache the highest value
                if (highestCustomModelData == null || value > highestCustomModelData) {
                    highestCustomModelData = value;
                }
            }
        }

        // If we cannot optimize we delete all data and return
        if (!canOptimize) {
            customModelDatas = null;
            lowestCustomModelData = null;
            highestCustomModelData = null;
        }

        // Fill in the gaps in the custom model data, so we can fetch them all properly
        if (customModelDatas != null) {
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

    @Nullable
    @Override
    public BakedModel resolve(BakedModel fallback, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        if (customModelDatas != null) {
            // Determine the custom model of the item as fast as possible (this code is called a lot per tick if there's many models)
            if (!itemStack.has(DataComponents.CUSTOM_MODEL_DATA)) return fallback;
            var id = itemStack.get(DataComponents.CUSTOM_MODEL_DATA).value();

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
            return fallback;
        }
        return super.resolve(fallback, itemStack, clientLevel, livingEntity, i);
    }
}
