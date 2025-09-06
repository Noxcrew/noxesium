package com.noxcrew.noxesium.example

import com.noxcrew.noxesium.paper.NoxesiumPaper
import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up the example extension.
 */
public class ExampleExtension : JavaPlugin() {
    override fun onLoad() {
        val noxesiumPaper = getPlugin(NoxesiumPaper::class.java)
        noxesiumPaper.registerEntrypoint { ExamplePaperEntrypoint() }
    }
}
