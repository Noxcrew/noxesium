package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.core.nms.registry.CommonBlockEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonGameComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonItemComponentSerializers
import com.noxcrew.noxesium.paper.commands.NoxesiumListCommand
import com.noxcrew.noxesium.paper.network.FabricPaperClientboundNetworking
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
            NoxesiumClientboundNetworking.setInstance(FabricPaperClientboundNetworking())

            CommonBlockEntityComponentSerializers.register()
            CommonEntityComponentSerializers.register()
            CommonGameComponentSerializers.register()
            CommonItemComponentSerializers.register()
        }
    }

    override fun onEnable() {
        setup()
        getCommand("noxlist")?.setExecutor(NoxesiumListCommand())
    }
}
