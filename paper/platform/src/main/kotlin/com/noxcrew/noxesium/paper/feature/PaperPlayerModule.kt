package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.paper.NoxesiumPaper
import org.bukkit.Bukkit

/** Ticks all players every tick. */
public class PaperPlayerModule : NoxesiumFeature() {
    override fun onRegister() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(NoxesiumPaper.plugin, {
            for (player in NoxesiumPlayerManager.getInstance().allPlayers) {
                player.tick()
            }
        }, 1, 1)
    }
}
