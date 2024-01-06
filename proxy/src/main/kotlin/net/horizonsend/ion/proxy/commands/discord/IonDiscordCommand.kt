package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.InvalidCommandArgument
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.utils.JDACommandManager
import net.horizonsend.ion.proxy.utils.JDACommandManager.Companion.handleException
import net.horizonsend.ion.proxy.wrappers.WrappedScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class IonDiscordCommand {
	private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

	companion object {
		val ASYNC_COMMAND_THREAD: ExecutorService =
			Executors.newSingleThreadExecutor(WrappedScheduler.namedThreadFactory("ion-async-commands"))
	}

	open fun onEnable(commandManager: JDACommandManager) {}

	protected fun fail(message: () -> String): Nothing = throw InvalidCommandArgument(message())

	protected fun resolveNation(name: String): Oid<Nation> = NationCache.getByName(name)
		?: fail { "Nation $name not found" }

	protected fun resolveSettlement(name: String): Oid<Settlement> = SettlementCache.getByName(name)
		?: fail { "Settlement $name not found" }

	protected fun respondEmbed(
		event: SlashCommandInteractionEvent,
		embed: MessageEmbed,
		ephemeral: Boolean = true
	) {
		event.replyEmbeds(embed).setEphemeral(ephemeral).queue()
	}

	protected fun getPlayerName(id: SLPlayerId): String {
		return PLUGIN.getProxy().getPlayer(id.uuid)?.name ?: SLPlayer.getName(id) ?: error("No such player $id")
	}

	protected fun asyncDiscordCommand(event: SlashCommandInteractionEvent, block: () -> Unit) {
		ASYNC_COMMAND_THREAD.submit {
			try {
				block()
			} catch (e: Exception) {
				handleException(log, event, e)
			}
		}
	}
}
