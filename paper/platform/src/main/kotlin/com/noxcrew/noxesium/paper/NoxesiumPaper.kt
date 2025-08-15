package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.nms.network.NoxesiumNetworking
import com.noxcrew.noxesium.core.nms.registry.CommonBlockEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonGameComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonItemComponentSerializers
import com.noxcrew.noxesium.paper.commands.NoxesiumListCommand
import com.noxcrew.noxesium.paper.network.NoxesiumServerHandshaker
import com.noxcrew.noxesium.paper.network.PaperNoxesiumClientboundNetworking
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up Noxesium for usage on Paper. Noxesium can be either compiled into your jar or it can
 * be put in the plugins folder as separate plugin. Make sure to initialize this file and run
 * setup() if you compile it into your plugin.
 */
public class NoxesiumPaper : JavaPlugin() {
    public companion object {
        /** The main plugin instance to use. */
        internal lateinit var plugin: Plugin

        /** Sets up Noxesium's server-side API. */
        public fun setup(plugin: Plugin) {
            NoxesiumPaper.plugin = plugin
            NoxesiumNetworking.setInstance(PaperNoxesiumClientboundNetworking())

            CommonBlockEntityComponentSerializers.register()
            CommonEntityComponentSerializers.register()
            CommonGameComponentSerializers.register()
            CommonItemComponentSerializers.register()

            NoxesiumServerHandshaker().register()
        }
    }

    override fun onEnable() {
        setup(this)
        getCommand("noxlist")?.setExecutor(NoxesiumListCommand())
    }
}