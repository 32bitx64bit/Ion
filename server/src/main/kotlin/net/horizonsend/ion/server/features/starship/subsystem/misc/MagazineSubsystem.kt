package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.misc.AbstractMagazineMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class MagazineSubsystem(starship: ActiveStarship, sign: Sign, multiblock: AbstractMagazineMultiblock) :
	AbstractMultiblockSubsystem<AbstractMagazineMultiblock>(starship, sign, multiblock) {
	fun isAmmoAvailable(itemStack: ItemStack): Boolean {
		val inventory = getInventory()
			?: return false

		return inventory.containsAtLeast(itemStack, itemStack.amount)
	}

	fun tryConsumeAmmo(itemStack: ItemStack): Boolean {
		val inventory = getInventory()
			?: return false

		if (!inventory.containsAtLeast(itemStack, itemStack.amount)) {
			return false
		}

		// clone bc:
		// "It is known that in some implementations this method will also set the
		// inputted argument amount to the number of that item not removed from slots."
		// - javadoc for this method
		inventory.removeItemAnySlot(itemStack.clone())
		return true
	}

	private fun getInventory(): Inventory? {
		if (!isIntact()) {
			return null
		}

		val inventoryHolder = if (!multiblock.mirrored) {
			starship.world
				.getBlockAtKey(pos.toBlockKey())
				.getRelative(face)
				.getRelative(face.rightFace)
				.state as? InventoryHolder
				?: return null
		} else {
			starship.world
				.getBlockAtKey(pos.toBlockKey())
				.getRelative(face)
				.getRelative(face.leftFace)
				.state as? InventoryHolder
				?: return null
		}

		return inventoryHolder.inventory
	}
}
