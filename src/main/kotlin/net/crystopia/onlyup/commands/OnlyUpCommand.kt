package net.crystopia.onlyup.commands

import dev.jorel.commandapi.CommandTree
import net.crystopia.onlyup.guis.OnlyUpSelectorGui
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class OnlyUpCommand {
    val command = CommandTree("onlyup").executes(
        dev.jorel.commandapi.executors.CommandExecutor { sender, commandArguments ->
            val player = sender as Player
            OnlyUpSelectorGui().openGUI(player)
        }).register()
}