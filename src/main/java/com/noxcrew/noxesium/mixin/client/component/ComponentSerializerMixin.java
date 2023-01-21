package com.noxcrew.noxesium.mixin.client.component;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.GameProfile;
import com.noxcrew.noxesium.skull.GameProfileFetcher;
import com.noxcrew.noxesium.skull.SkullContents;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Modifies [Component] serialization and de-serialization to include custom
 * skull components.
 */
@Mixin(Component.Serializer.class)
public abstract class ComponentSerializerMixin {

    @Shadow
    public abstract MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException;

    @Shadow
    protected abstract void serializeStyle(Style style, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext);

    @Shadow
    public abstract JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext);

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("HEAD"), cancellable = true)
    private void injected(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<MutableComponent> cir) {
        if (!jsonElement.isJsonObject()) return;
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has("score")) {
            // We allow custom servers to use a custom score component since it renders as empty if the value is not found.
            var jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
            if (jsonObject2.has("name")) {
                var jsonString = GsonHelper.getAsString(jsonObject2, "name");

                // We check for a string that starts with %NCPH% for Noxesium Custom Player Head.
                if (jsonString.startsWith("%NCPH%")) {
                    var values = jsonString.substring("%NCPH%".length()).split(",");
                    try {
                        UUID uuid = null;
                        var stringUuid = values[0];
                        var grayscale = Boolean.parseBoolean(values[1]);
                        var advance = Integer.parseInt(values[2]);
                        var ascent = Integer.parseInt(values[3]);
                        var scale = Float.parseFloat(values[4]);

                        CompletableFuture<String> texture = new CompletableFuture<>();
                        try {
                            uuid = UUID.fromString(stringUuid);
                            GameProfile gameprofile = new GameProfile(uuid, null);
                            GameProfileFetcher.updateGameProfile(gameprofile, (profile) -> {
                                var property = Iterables.getFirst(profile.getProperties().get(SkinManager.PROPERTY_TEXTURES), null);
                                if (property != null) {
                                    texture.complete(property.getValue());
                                }
                            });
                        } catch (Exception x) {
                            // We ignore any errors from fetching the player data.
                        }

                        MutableComponent mutableComponent = MutableComponent.create(new SkullContents(uuid, texture, grayscale, advance, ascent, scale));
                        if (jsonObject.has("extra")) {
                            JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                            if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                            for (int j = 0; j < jsonArray2.size(); ++j) {
                                mutableComponent.append(deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                            }
                        }
                        mutableComponent.setStyle(jsonDeserializationContext.deserialize(jsonElement, Style.class));
                        cir.setReturnValue(mutableComponent);
                    } catch (Exception x) {
                        // Ignore exceptions while loading
                    }
                }
            }
        } else if (jsonObject.has("skull")) {
            var jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "skull");

            CompletableFuture<String> texture = new CompletableFuture<>();
            UUID uuid = null;
            if (jsonObject2.has("texture")) {
                texture.complete(GsonHelper.getAsString(jsonObject2, "texture"));
            } else {
                try {
                    uuid = UUID.fromString(GsonHelper.getAsString(jsonObject2, "uuid"));
                    GameProfile gameprofile = new GameProfile(uuid, null);
                    GameProfileFetcher.updateGameProfile(gameprofile, (profile) -> {
                        var property = Iterables.getFirst(profile.getProperties().get(SkinManager.PROPERTY_TEXTURES), null);
                        if (property != null) {
                            texture.complete(property.getValue());
                        }
                    });
                } catch (Exception x) {
                    // We ignore any errors from fetching the player data.
                }
            }

            var grayscale = GsonHelper.getAsBoolean(jsonObject, "grayscale", false);
            var advance = GsonHelper.getAsInt(jsonObject2, "advance", 0);
            var ascent = GsonHelper.getAsInt(jsonObject2, "ascent", 0);
            var scale = GsonHelper.getAsFloat(jsonObject2, "scale", 1f);
            MutableComponent mutableComponent = MutableComponent.create(new SkullContents(uuid, texture, grayscale, advance, ascent, scale));
            if (jsonObject.has("extra")) {
                JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                for (int j = 0; j < jsonArray2.size(); ++j) {
                    mutableComponent.append(deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
                }
            }
            mutableComponent.setStyle(jsonDeserializationContext.deserialize(jsonElement, Style.class));
            cir.setReturnValue(mutableComponent);
        }
    }

    @Inject(method = "serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;", at = @At("HEAD"), cancellable = true)
    private void injected(Component component, Type type, JsonSerializationContext jsonSerializationContext, CallbackInfoReturnable<JsonElement> cir) {
        ComponentContents componentContents = component.getContents();
        if (componentContents instanceof SkullContents contents) {
            JsonObject jsonObject = new JsonObject();
            if (!component.getStyle().isEmpty()) {
                serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
            }
            if (!component.getSiblings().isEmpty()) {
                JsonArray jsonArray = new JsonArray();
                for (Component component2 : component.getSiblings()) {
                    jsonArray.add(serialize(component2, Component.class, jsonSerializationContext));
                }
                jsonObject.add("extra", jsonArray);
            }

            JsonObject jsonObject2 = new JsonObject();
            if (contents.getUuid() != null) {
                jsonObject2.addProperty("uuid", contents.getUuid().toString());
            } else {
                var texture = contents.getTexture();
                if (texture != null) {
                    jsonObject2.addProperty("texture", texture);
                }
            }
            jsonObject2.addProperty("grayscale", contents.isGrayscale());
            jsonObject2.addProperty("advance", contents.getAdvance());
            jsonObject2.addProperty("ascent", contents.getAscent());
            jsonObject2.addProperty("scale", contents.getScale());
            jsonObject.add("skull", jsonObject2);
            cir.setReturnValue(jsonObject);
        }
    }
}
