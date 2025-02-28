package net.crystopia.onlyup.commands

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import net.crystopia.onlyup.config.ConfigManager
import net.crystopia.onlyup.guis.OnlyUpSelectorGui
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class OnlyUpCommand {
    val command = commandTree("onlyup") {
        executes(CommandExecutor { sender, args ->
            val player = sender as Player
            OnlyUpSelectorGui().openGUI(player)
        })
        literalArgument("reload") {
            withPermission("crystopia.commands.onlyup.reload")
            executes(CommandExecutor { sender, args ->
                ConfigManager.reload()
                sender.sendMessage("Reloaded config")
            })
        }
    }
}