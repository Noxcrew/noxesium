package com.noxcrew.noxesium.example

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.registry.RegistryCollection
import java.net.URL

/**
 * Implements an example Noxesium entrypoint on Paper.
 */
public class ExamplePaperEntrypoint : NoxesiumEntrypoint {
    override fun getId(): String = "example"

    override fun getRegistryCollections(): Collection<RegistryCollection<*>> = listOf(
        ExampleBlockEntityComponents.INSTANCE
    )

    override fun getEncryptionKey(): URL = ExamplePaperEntrypoint::class.java.getClassLoader().getResource("encryption-key.aes")
}
