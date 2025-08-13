package com.noxcrew.noxesium.fabric.example;

import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.registry.RegistryCollection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up an entrypoint into Noxesium's APIs.
 */
public class ExampleNoxesiumEntrypoint implements NoxesiumEntrypoint {

    @Override
    public String getId() {
        return "example";
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return ExampleNoxesiumEntrypoint.class.getClassLoader().getResource("encryption-key.aes");
    }

    @Override
    public Collection<RegistryCollection<?>> getRegistryCollections() {
        return List.of(ExampleBlockEntityComponents.INSTANCE);
    }
}
