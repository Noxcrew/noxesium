package com.noxcrew.noxesium.paper.feature

import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.api.nms.serialization.CommonSerializerPair
import com.noxcrew.noxesium.api.nms.serialization.SerializableRegistries
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry
import com.noxcrew.noxesium.paper.NoxesiumPaper
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

/**
 * Loads all registry contents defined in JSON files in the plugin's folder.
 */
public class RegistryLoader : NoxesiumFeature() {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchKeys = mutableMapOf<Map.Entry<NoxesiumRegistry<*>, CommonSerializerPair<*>>, WatchKey>()

    init {
        for (entry in SerializableRegistries.getAllSerializers()) {
            val (registry, codec) = entry
            val folder =
                NoxesiumPaper.plugin.dataFolder
                    .toPath()
                    .resolve("registries/${registry.id().value()}")
            folder.createDirectories()

            // Read the initial values in the folder
            for (file in folder.listDirectoryEntries()) {
                if (file.isDirectory()) continue
                if (file.extension != "json") continue
                registry.updateEntry(file, codec.codec)
            }

            // Start watching for changes
            watchKeys[entry] =
                folder.register(
                    watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE,
                )
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            NoxesiumPaper.plugin,
            {
                if (!isRegistered) return@scheduleSyncRepeatingTask
                for ((entry, watchKey) in watchKeys) {
                    if (!watchKey.isValid) continue
                    val (registry, codec) = entry

                    // Poll events and then immediately reset
                    val events = watchKey.pollEvents()
                    if (events.isEmpty()) continue
                    watchKey.reset()
                    events
                        .mapNotNull {
                            val file = (watchKey.watchable() as Path).resolve(it.context() as Path)
                            if (file.isDirectory()) return@mapNotNull null
                            if (file.extension != "json") return@mapNotNull null

                            if (it.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                // If a file was deleted we remove it!
                                registry.remove(Key.key(file.nameWithoutExtension))
                                null
                            } else {
                                // Otherwise we update this file!
                                file
                            }
                        }.distinct()
                        .forEach {
                            registry.updateEntry(it, codec.codec)
                        }
                }
            },
            4, 4,
        )
    }

    /** Updates the entry in the given file with the given codec. */
    private fun <T> NoxesiumRegistry<T>.updateEntry(file: Path, codec: Codec<*>) {
        try {
            val value = (codec as Codec<T>).decode(JsonOps.INSTANCE, JsonPrimitive(file.readText())).orThrow.first
            register(Key.key(file.nameWithoutExtension), value)
        } catch (x: Exception) {
            NoxesiumApi.getLogger().error("Could not read file $file as valid registry entry in ${id()}", x)
        }
    }
}
