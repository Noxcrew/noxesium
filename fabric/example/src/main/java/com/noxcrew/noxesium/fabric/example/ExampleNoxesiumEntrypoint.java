package com.noxcrew.noxesium.fabric.example;

import org.jetbrains.annotations.Nullable;

import java.net.URL;

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
}
