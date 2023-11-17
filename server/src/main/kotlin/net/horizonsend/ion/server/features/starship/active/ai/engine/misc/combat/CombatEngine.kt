package net.horizonsend.ion.server.features.starship.active.ai.engine.misc.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting.TargetingEngine
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

abstract class CombatEngine(controller: AIController, val targetingSupplier: TargetingEngine) : AIEngine(controller) {
	var shotDeviation: Double = 0.025

	var shouldFaceTarget: Boolean = false

	// Turn cooldown adjustment
	private var turnTicks: Int = 0
	var turnCooldown: Int = 20 * 3

	/** Rotate to face a specified blockface */
	fun handleRotation(faceDirection: BlockFace) {
		if (!shouldFaceTarget) return
		if (!CARDINAL_BLOCK_FACES.contains(faceDirection)) throw IllegalArgumentException("Ships can only face cardinal directions!")

		if (turnTicks >= turnCooldown) {
			turnTicks = 0
			return
		}

		turnTicks++

		AIControlUtils.faceDirection(controller, faceDirection)
	}

	/**
	 * Use Vec3i as a target to allow block targeting
	 *
	 * Lambda allows modification of the aiming direction
	 **/
	fun fireAllWeapons(origin: Vec3i, target: Vector, direction: Vector) {
		if (shotDeviation > 0) {
			val offsetX = randomDouble(-shotDeviation, shotDeviation)
			val offsetY = randomDouble(-shotDeviation, shotDeviation)
			val offsetZ = randomDouble(-shotDeviation, shotDeviation)

			direction.add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}

		val distance = target.distance(origin.toVector())

		val weaponSet = controller.manualWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }?.name?.lowercase()

		Tasks.sync {
			fireHeavyWeapons(direction, target, weaponSet = weaponSet)
			fireLightWeapons(direction, target, weaponSet = weaponSet)
		}
	}

	/** Fires light weapons (left click) in a direction */
	fun fireLightWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		debugAudience.debug("Firing light weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = true, target = target, weaponSet = weaponSet)
	}

	/** Fires heavy weapons (right click) in a direction */
	fun fireHeavyWeapons(direction: Vector, target: Vector? = null, weaponSet: String? = null) {
		debugAudience.debug("Firing heavy weapons: Set: $weaponSet")
		AIControlUtils.shootInDirection(controller, direction, leftClick = false, target = target, weaponSet = weaponSet)
	}

	/** Updates all auto weapons,that are in range, to fire on the target */
	fun handleAutoWeapons(origin: Vec3i, target: AITarget) {
		val (x, y, z) = origin
		val distance = target.getVec3i(false).distance(x, y, z)
		val weaponSet = controller.autoWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }?.name?.lowercase()

		if (weaponSet == null) {
			AIControlUtils.unSetAllWeapons(controller)
			return
		}

		AIControlUtils.setAutoWeapons(controller, weaponSet, target.getAutoTurretTarget())
	}
}
