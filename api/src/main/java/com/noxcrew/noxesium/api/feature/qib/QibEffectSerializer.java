package com.noxcrew.noxesium.api.feature.qib;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * Implements serialization and de-serialization for QibEffect.
 */
public class QibEffectSerializer implements JsonSerializer<QibEffect>, JsonDeserializer<QibEffect> {
    @Override
    public QibEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        var object = json.getAsJsonObject();
        var data = object.getAsJsonObject("effect");
        return switch (object.getAsJsonPrimitive("type").getAsString()) {
            case "Multiple" -> context.deserialize(data, QibEffect.Multiple.class);
            case "Stay" -> context.deserialize(data, QibEffect.Stay.class);
            case "Wait" -> context.deserialize(data, QibEffect.Wait.class);
            case "Conditional" -> context.deserialize(data, QibEffect.Conditional.class);
            case "PlaySound" -> context.deserialize(data, QibEffect.PlaySound.class);
            case "GivePotionEffect" -> context.deserialize(data, QibEffect.GivePotionEffect.class);
            case "RemovePotionEffect" -> context.deserialize(data, QibEffect.RemovePotionEffect.class);
            case "RemoveAllPotionEffects" -> context.deserialize(data, QibEffect.RemoveAllPotionEffects.class);
            case "Move" -> context.deserialize(data, QibEffect.Move.class);
            case "AddVelocity" -> context.deserialize(data, QibEffect.AddVelocity.class);
            case "SetVelocity" -> context.deserialize(data, QibEffect.SetVelocity.class);
            case "SetVelocityYawPitch" -> context.deserialize(data, QibEffect.SetVelocityYawPitch.class);
            case "ModifyVelocity" -> context.deserialize(data, QibEffect.ModifyVelocity.class);
            case "StartGliding" -> context.deserialize(data, QibEffect.StartGliding.class);
            case "StopGliding" -> context.deserialize(data, QibEffect.StopGliding.class);
            case "ApplyImpulse" -> context.deserialize(data, QibEffect.ApplyImpulse.class);
            default -> throw new JsonParseException(
                    "Invalid input type " + object.getAsJsonPrimitive("type").getAsString());
        };
    }

    @Override
    public JsonElement serialize(QibEffect src, Type typeOfSrc, JsonSerializationContext context) {
        var object = new JsonObject();
        object.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
        object.add("effect", context.serialize(src));
        return object;
    }
}
