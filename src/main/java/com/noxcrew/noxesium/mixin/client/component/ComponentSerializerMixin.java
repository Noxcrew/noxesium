package com.noxcrew.noxesium.mixin.client.component;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.noxcrew.noxesium.skull.SkullContents;
import net.minecraft.client.Minecraft;
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
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("selector")) {
                // We allow custom servers to use a custom selector component that renders as empty in vanilla but gets
                // caught by Noxesium to become a custom skull. Would've been nicer to use translate with fallback but
                // we are not yet in 1.19.3.
                var jsonString = GsonHelper.getAsString(jsonObject, "selector");
                if (jsonString.startsWith("@X|")) {
                    var values = jsonString.substring("@X|".length()).split(",");
                    try {
                        var uuid = values[0];
                        var grayscale = Boolean.parseBoolean(values[1]);
                        var advance = Integer.parseInt(values[2]);
                        var ascent = Integer.parseInt(values[3]);
                        var scale = Float.parseFloat(values[4]);

                        String texture = null;
                        // TODO Support uuids of unknown players
                        var info = Minecraft.getInstance().player.connection.getOnlinePlayers().stream().filter(f -> f.getProfile().getId().toString().equals(uuid)).findFirst();
                        if (info.isPresent()) {
                            var property = Iterables.getFirst(info.get().getProfile().getProperties().get(SkinManager.PROPERTY_TEXTURES), null);
                            if (property != null) {
                                texture = property.getValue();
                            }
                        }

                        MutableComponent mutableComponent = MutableComponent.create(new SkullContents(texture, grayscale, advance, ascent, scale));
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
            } else if (jsonObject.has("skull")) {
                var jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "skull");

                String texture = null;
                if (jsonObject2.has("texture")) {
                    texture = GsonHelper.getAsString(jsonObject2, "texture");
                } else {
                    var uuid = GsonHelper.getAsString(jsonObject2, "uuid");
                    try {
                        var info = Minecraft.getInstance().player.connection.getOnlinePlayers().stream().filter(f -> f.getProfile().getId().toString().equals(uuid)).findFirst();
                        if (info.isPresent()) {
                            var property = Iterables.getFirst(info.get().getProfile().getProperties().get(SkinManager.PROPERTY_TEXTURES), null);
                            if (property != null) {
                                texture = property.getValue();
                            }
                        }
                    } catch (Exception x) {
                        // Ignore invalid uuids
                    }
                }

                var grayscale = GsonHelper.getAsBoolean(jsonObject, "grayscale", false);
                var advance = GsonHelper.getAsInt(jsonObject2, "advance", 0);
                var ascent = GsonHelper.getAsInt(jsonObject2, "ascent", 0);
                var scale = GsonHelper.getAsFloat(jsonObject2, "scale", 1f);
                MutableComponent mutableComponent = MutableComponent.create(new SkullContents(texture, grayscale, advance, ascent, scale));
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
            jsonObject2.addProperty("texture", contents.getTexture());
            jsonObject2.addProperty("grayscale", contents.isGrayscale());
            jsonObject2.addProperty("advance", contents.getAdvance());
            jsonObject2.addProperty("ascent", contents.getAscent());
            jsonObject2.addProperty("scale", contents.getScale());
            jsonObject.add("skull", jsonObject2);
            cir.setReturnValue(jsonObject);
        }
    }
}
