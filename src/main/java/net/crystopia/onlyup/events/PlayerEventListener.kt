package net.crystopia.onlyup.events

import gg.flyte.twilight.gui.GUI.Companion.openInventory
import gg.flyte.twilight.gui.gui
import net.crystopia.onlyup.config.ConfigManager
import net.crystopia.onlyup.OnlyUp
import net.crystopia.onlyup.TimeParser
import net.crystopia.onlyup.config.PlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import java.io.ObjectInputFilter.Config
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap


class PlayerEventListener(private val plugin: OnlyUp) : Listener {
    private val mm = MiniMessage.miniMessage()
    private val timer = HashMap<String, Instant>()

    init {
        startActionBarUpdater()
    }

    private fun isAtLocation(loc: Location, target: Location): Boolean {
        return loc.x >= target.blockX - 0.5 || loc.x <= target.blockX + 0.5 && loc.y >= target.blockY - 0.5 || loc.y <= target.blockY + 0.5 && loc.z >= target.blockZ - 0.5 || loc.z <= target.blockZ + 0.5
    }

    // Time Fixen!!!!!

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val playerLoc = player.location

        for (onlyup in ConfigManager.settings.onlyups) {

            // Handle Start OnlyUp
            if (isAtLocation(
                    playerLoc, Location(
                        Bukkit.getWorld(onlyup.value.worldName),
                        onlyup.value.start.x,
                        onlyup.value.start.y,
                        onlyup.value.start.y
                    )
                )
            ) {
                if (!timer.contains(player.uniqueId.toString())) {
                    startActionBarUpdater()
                    timer[player.uniqueId.toString()] = Instant.now()
                    player.sendMessage(mm.deserialize("\n<color:#85c6ff>You started your Run! Good Luck and fun.</color>\n"))
                }
            }

            // Handle End OnlyUp
            if (isAtLocation(
                    playerLoc, Location(
                        Bukkit.getWorld(onlyup.value.worldName),
                        onlyup.value.end.x,
                        onlyup.value.end.y,
                        onlyup.value.end.y
                    )
                ) && timer.containsKey(player.uniqueId.toString())
            ) {
                val playerId = player.uniqueId.toString()
                val startTime = timer[playerId] ?: return

                if (!ConfigManager.players.players.contains(playerId)) {
                    ConfigManager.players.players[playerId] = PlayerData(
                        name = player.name,
                        uuid = playerId,
                        time = startTime.toString(),
                        bestTime = startTime.toString()
                    )
                }

                val newTime = TimeParser().parseDuration(startTime.toString())
                val bestTime = TimeParser().parseDuration(ConfigManager.players.players[playerId]?.bestTime!!)

                if (newTime < bestTime) {
                    player.sendMessage(mm.deserialize("You have a new best time $newTime (${bestTime})"))
                    ConfigManager.players.players[playerId] = PlayerData(
                        name = player.name, uuid = playerId, time = newTime.toString(), bestTime = newTime.toString()
                    )
                }

                player.sendMessage(mm.deserialize("\n<color:#4fff4d><b>You did it, you needed exactly ${newTime}!</b></color>\n"))
            }
        }
    }


    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type == Material.COMPASS && item.itemMeta?.hasCustomModelData() == true && item.itemMeta?.customModelData == 200) {

            var slot = 0

            val complexGui = gui(mm.deserialize(ConfigManager.settings.leaderboardGuiName)) {
                ConfigManager.players.players.forEach { player ->
                    set(slot, ItemStack(Material.PLAYER_HEAD).apply {
                        slot += 1
                        val meta = this.itemMeta as SkullMeta

                        meta.displayName(mm.deserialize("<color:#efff94>${OnlyUp.instance.server.getOfflinePlayer(player.value.uuid).name}</color>"))

                        val lore: MutableList<Component> = mutableListOf(
                            mm.deserialize("<i><color:#80ddff>Time: ${TimeParser().parseDuration(player.value.time)}</color></i>"),
                            mm.deserialize("<i><color:#80ddff>Best Time: ${TimeParser().parseDuration(player.value.bestTime)}</color></i>")
                        )
                        meta.lore(lore)

                        meta.owningPlayer = viewer

                        this.itemMeta = meta
                    }) {
                        isCancelled = true
                    }

                }

            }
            player.openInventory(complexGui)


        }

        if (item.type == Material.BARRIER && item.itemMeta?.hasCustomModelData() == true && item.itemMeta?.customModelData == 210) {

            val complexGui = gui(mm.deserialize(ConfigManager.settings.resetGuiName)) {

                // ResetGUI
                set(12, ItemStack(Material.LIME_DYE).apply {
                    val meta = this.itemMeta

                    meta.displayName(mm.deserialize("<b><color:#8aff80>✅ Reset the Timer</color></b>"))

                    this.itemMeta = meta
                }) {
                    isCancelled = true
                    timer.remove(player.uniqueId.toString())
                    player.sendMessage(mm.deserialize("<color:#4dff4a>You timer has been reset!</color>"))
                    player.closeInventory()
                }
                // CloseGUI
                set(14, ItemStack(Material.RED_DYE).apply {
                    val meta = this.itemMeta

                    meta.displayName(mm.deserialize("<color:#ff4340><b>❌ Cancel</b></color>"))

                    this.itemMeta = meta
                }) {
                    isCancelled = true
                    player.closeInventory()
                }
            }
            player.openInventory(complexGui)

        }
    }

    @EventHandler
    fun onInventoryClick(event: PlayerMoveEvent) {
        val player = event.player
        if (player.world !== Bukkit.getWorld("onlyup")) {
            timer.remove(player.uniqueId.toString())
        }
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        timer.remove(event.player.uniqueId.toString())
    }

    private fun startActionBarUpdater() {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val startTime = timer[player.uniqueId.toString()] ?: continue
                    val elapsedTimes = Duration.between(startTime, Instant.now()).seconds
                    player.sendActionBar(mm.deserialize("<color:#f3ff6e>Timer: $elapsedTimes</color>"))
                }
            }
        }.runTaskTimer(OnlyUp.instance, 0L, 20L)
    }

}