package net.crystopia.onlyup.commands

import dev.jorel.commandapi.CommandTree
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class OnlyUpCommand {
    val command = CommandTree("onlyup")..executes(
        dev.jorel.commandapi.executors.CommandExecutor { sender, commandArguments ->
            val player = sender as Player
            val mm = MiniMessage.miniMessage()


            val resetItem = ItemStack(Material.BARRIER)
            val resetItemMeta = resetItem.itemMeta

            resetItemMeta.displayName(mm.deserialize("<gray>Click to <color:#6eafff>reset</color> the <color:#6eafff>timer</color></gray>"))
            resetItemMeta.setCustomModelData(210)

            resetItem.itemMeta = resetItemMeta

            val statItem = ItemStack(Material.COMPASS)
            val statItemMeta = statItem.itemMeta

            statItemMeta.setCustomModelData(200)
            statItemMeta.displayName(mm.deserialize("<color:#87ff9d>Leaderboard</color>"))

            statItem.itemMeta = statItemMeta

            player.inventory.clear()
            player.inventory.setItem(4, statItem)
            player.inventory.setItem(
                0,
                resetItem
            )


            player.teleport(Location(Bukkit.getWorld("onlyup"), 385.0, -7.0, 764.0, -92.1F, 2F))


        }
    ).register()
}