package com.noxcrew.noxesium.core.fabric.example;

import com.noxcrew.noxesium.api.nms.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up an entrypoint into Noxesium's APIs.
 */
public class ExampleNoxesiumEntrypoint implements ClientNoxesiumEntrypoint {

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
