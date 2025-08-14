package com.noxcrew.noxesium.paper

import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up Noxesium for usage on Paper. Noxesium can be either compiled into your jar or it can
 * be put in the plugins folder as separate plugin. Make sure to initialize this file and run
 * setup() if you compile it into your plugin.
 */
public class NoxesiumPaper : JavaPlugin() {
    public companion object {
        /** Sets up Noxesium's server-side API. */
        public fun setup() {
        }
    }

    override fun onEnable() {
        setup()
        // TODO getCommand("noxlist")?.setExecutor(NoxesiumListCommand())
    }
}
