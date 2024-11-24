package net.crystopia.onlyup.events

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper
import net.crystopia.onlyup.OnlyUp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.WorldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import java.util.*


class PlayerEventListener(private val plugin: OnlyUp) : Listener {

    val mm = MiniMessage.miniMessage()


    private val playerTimers = HashMap<UUID, Long>()
    private val playerBestTimes = plugin.getPlayerBestTimes().toMutableMap()

    init {
        startActionBarUpdater()
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val playerLoc = player.location

        for ((key, locations) in plugin.startPoints) {
            val (start, end) = locations

            if (isAtLocation(playerLoc, start)) {
                if (!playerTimers.containsKey(player.uniqueId)) {
                    playerTimers[player.uniqueId] = System.currentTimeMillis()
                    player.sendMessage("⁌ Timer started! - Good luck!")
                }
            } else if (isAtLocation(playerLoc, end)) {
                val startTime = playerTimers[player.uniqueId] ?: return
                val elapsed = System.currentTimeMillis() - startTime
                playerTimers.remove(player.uniqueId)
                val formattedTime = formatTime(elapsed)
                val currentBestTime = playerBestTimes[player.uniqueId]
                if (currentBestTime == null || elapsed < currentBestTime) {
                    playerBestTimes[player.uniqueId] = elapsed
                    plugin.updatePlayerTimeIfBetter(player.uniqueId, elapsed)
                    player.sendMessage("⁌ §aNew best time! §7Time: $formattedTime")
                    updatePlayerSkull(player, elapsed)
                } else {
                    player.sendMessage("⁌ §7Timer stopped! Time: $formattedTime")
                }
            }
        }
    }

    private fun isAtLocation(loc: Location, target: Location): Boolean {
        return loc.blockX == target.blockX && loc.blockY == target.blockY && loc.blockZ == target.blockZ
    }

    private fun formatTime(elapsed: Long): String {
        val hours = (elapsed / 3600000).toInt()
        val minutes = ((elapsed % 3600000) / 60000).toInt()
        val seconds = ((elapsed % 60000) / 1000).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.COMPASS && item.itemMeta?.hasCustomModelData() == true && item.itemMeta?.customModelData == 200) {
            showTopPlayers(player)
        }

        if (item.type == Material.BARRIER && item.itemMeta?.hasCustomModelData() == true && item.itemMeta?.customModelData == 210) {
            playerTimers.remove(player.uniqueId)
            player.sendMessage("⁌ §7Your Timer has been reset! - Try again!")
        }
    }

    @EventHandler
    fun onInventoryClick(event: PlayerMoveEvent) {
        val player = event.player
        if (player.world !== Bukkit.getWorld("onlyup")) {
            playerTimers.remove(player.uniqueId)
        }


    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "§f:offset_-13:⇉") {
            event.isCancelled = true
        }
    }

    private fun showTopPlayers(player: Player) {
        val topPlayers = playerBestTimes.entries.sortedBy { it.value }.take(10)


        val inventory = Bukkit.createInventory(null, 27, "§f:offset_-13:⇉")

        for ((index, entry) in topPlayers.withIndex()) {
            val (uuid, time) = entry
            val skull = ItemStack(Material.PLAYER_HEAD)
            val meta = skull.itemMeta as SkullMeta
            meta.owningPlayer = Bukkit.getOfflinePlayer(uuid)

            meta.setDisplayName("§f${Bukkit.getOfflinePlayer(uuid).name}")

            val parsedone: Component =
                mm.deserialize("")
            val parsedtwo: Component =
                mm.deserialize(" <white>Best Time                        <gray>")
            val parsedthree: Component =
                mm.deserialize(" <strikethrough><gray>-</strikethrough> <gradient:#74D680:#378B29>${formatTime(time)}</gradient>")
            val parsedfour: Component =
                mm.deserialize(" ")
            meta.lore(listOf(parsedone, parsedtwo, parsedthree, parsedfour))
            skull.itemMeta = meta
            inventory.setItem(index + 9, skull)  // Start from the second row
        }

        player.openInventory(inventory)
    }

    private fun updatePlayerSkull(player: Player, elapsed: Long) {
        val topPlayers = playerBestTimes.entries.sortedBy { it.value }.take(10)
        for ((index, entry) in topPlayers.withIndex()) {
            if (entry.key == player.uniqueId) {
                val time = entry.value
                val skull = ItemStack(Material.PLAYER_HEAD)
                val meta = skull.itemMeta as SkullMeta
                meta.owningPlayer = Bukkit.getOfflinePlayer(entry.key)
                meta.setDisplayName("${Bukkit.getOfflinePlayer(entry.key).name}: ${formatTime(time)}")
                skull.itemMeta = meta
                player.openInventory.topInventory.setItem(index + 9, skull)  // Start from the second row
                break
            }
        }
    }

    private fun startActionBarUpdater() {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val startTime = playerTimers[player.uniqueId] ?: continue
                    val elapsed = System.currentTimeMillis() - startTime
                    player.sendActionBar("Time: ${formatTime(elapsed)}")
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }
}