package net.horizonsend.ion.server.features.multiblock.type.drills

import org.bukkit.Material

object DrillMultiblockTier3 : DrillMultiblock("&aTier 3", Material.EMERALD_BLOCK) {
	override val maxPower = 300_000
	override val radius = 4
	override val coolDown = 10
	override val mirrored = false
}

object DrillMultiblockTier3Mirrored : DrillMultiblock("&aTier 3", Material.EMERALD_BLOCK) {
	override val maxPower = 300_000
	override val radius = 4
	override val coolDown = 10
	override val mirrored = true
}
