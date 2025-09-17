package com.noxcrew.noxesium.example;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up an entrypoint into Noxesium's APIs.
 */
public class ExampleFabricEntrypoint implements ClientNoxesiumEntrypoint {

    @Override
    public String getId() {
        return "example";
    }

    @Override
    public String getVersion() {
        return "example";
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return ExampleFabricEntrypoint.class.getClassLoader().getResource("encryption-key.aes");
    }

    @Override
    public Collection<RegistryCollection<?>> getRegistryCollections() {
        return List.of(ExampleBlockEntityComponents.INSTANCE);
    }
}
