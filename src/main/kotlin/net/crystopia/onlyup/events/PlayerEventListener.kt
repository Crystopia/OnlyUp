package net.crystopia.onlyup.events

import gg.flyte.twilight.gui.GUI.Companion.openInventory
import gg.flyte.twilight.gui.gui
import net.crystopia.onlyup.config.ConfigManager
import net.crystopia.onlyup.OnlyUp
import net.crystopia.onlyup.config.PlayerData
import net.crystopia.onlyup.config.TimeData
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class PlayerEventListener(private val plugin: OnlyUp) : Listener {
    private val mm = MiniMessage.miniMessage()
    private val timer = HashMap<UUID, Instant>()
    private val runOnlyup = HashMap<UUID, String>()

    init {
        startActionBarUpdater()
    }

    private fun isExactLocation(loc: Location, target: Location): Boolean {
        return loc.blockX == target.blockX && loc.blockY == target.blockY && loc.blockZ == target.blockZ
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val playerLoc = player.location
        val playerId = player.uniqueId

        for (onlyup in ConfigManager.settings.onlyups) {
            val world = Bukkit.getWorld(onlyup.value.worldName) ?: continue

            // Start the Timer only if player is exactly at the start location
            val startLocation = Location(world, onlyup.value.start.x, onlyup.value.start.y, onlyup.value.start.z)
            if (isExactLocation(playerLoc, startLocation)) {
                if (!timer.containsKey(playerId)) {
                    timer[playerId] = Instant.now()
                    runOnlyup[playerId] = onlyup.key
                    player.sendMessage(mm.deserialize("\n<color:#85c6ff>You started your run! Good luck!</color>\n"))
                }
            }

            // Stop the Timer only if player is exactly at the end location
            val endLocation = Location(world, onlyup.value.end.x, onlyup.value.end.y, onlyup.value.end.z)
            if (isExactLocation(playerLoc, endLocation) && timer.containsKey(playerId)) {
                val elapsed = Duration.between(timer[playerId], Instant.now())
                timer.remove(playerId)

                val playerData = ConfigManager.players.players.getOrPut(playerId.toString()) {
                    PlayerData(
                        name = player.name,
                        uuid = playerId.toString(),
                        onlyups = mutableMapOf(),
                    )
                }
                if (!playerData.onlyups.contains(runOnlyup[playerId])) {
                    ConfigManager.players.players[playerId.toString()]!!.onlyups[runOnlyup[playerId].toString()] =
                        TimeData(
                            time = elapsed.toKotlinDuration(), bestTime = elapsed.toKotlinDuration()
                        )
                }

                ConfigManager.save()

                if (onlyup.value.rewardCommand.length > 1) {
                    val command = onlyup.value.rewardCommand
                    OnlyUp.instance.server.dispatchCommand(OnlyUp.instance.server.consoleSender, command)
                }

                val bestTimeDuration = playerData.onlyups[runOnlyup[playerId]]!!.bestTime


                if (elapsed.toMillis() < bestTimeDuration.toJavaDuration().toMillis()) {
                    player.sendMessage(
                        mm.deserialize(
                            "<gray>You have a new best time: <color:#fff475>${elapsed.toMinutesPart()}m</color> <color:#fff475>${elapsed.toSecondsPart()}s</color> (Previous: <color:#fff475>${
                                bestTimeDuration.toJavaDuration().toMinutesPart()
                            }m ${bestTimeDuration.toJavaDuration().toSecondsPart()}s</color>)</gray>"
                        )
                    )
                    playerData.onlyups[runOnlyup[playerId].toString()]!!.bestTime = elapsed.toKotlinDuration()
                    ConfigManager.save()
                }

                playerData.onlyups[runOnlyup[playerId]]!!.time = elapsed.toKotlinDuration()
                ConfigManager.save()

                player.sendMessage(mm.deserialize("\n<color:#4fff4d><b>You did it! Time: ${elapsed.toMinutesPart()}m ${elapsed.toSecondsPart()}s.</b></color>\n"))
                runOnlyup.remove(playerId)
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
                ConfigManager.players.players.values.sortedBy {
                    it.onlyups.values.minOfOrNull { entry -> entry.bestTime.toJavaDuration().toMillis() }
                        ?: Long.MAX_VALUE
                } // Sortierung nach kleinstem BestTime-Wert
                    .take(26) // Maximal 26 Spieler anzeigen
                    .forEach { entry ->
                        set(slot++, ItemStack(Material.PLAYER_HEAD).apply {
                            val meta = this.itemMeta as SkullMeta
                            meta.owningPlayer = player
                            meta.displayName(mm.deserialize("<color:#efff94>${entry.name}</color>"))
                            meta.lore(
                                entry.onlyups.map { (key, entry) ->
                                    listOf(
                                        mm.deserialize(
                                            "<i><color:#80ddff>$key - Time: ${
                                                entry.time.toJavaDuration().toMinutesPart()
                                            }m ${entry.time.toJavaDuration().toSecondsPart()}s</color></i>"
                                        ), mm.deserialize(
                                            "<i><color:#80ddff>$key - Best Time: ${
                                                entry.bestTime.toJavaDuration().toMinutesPart()
                                            }m ${entry.bestTime.toJavaDuration().toSecondsPart()}s</color></i>"
                                        )
                                    )
                                }.flatten()
                            )
                            this.itemMeta = meta
                        }) { isCancelled = true }
                    }

            }
            player.openInventory(complexGui)
        }

        if (item.type == Material.BARRIER && item.itemMeta?.hasCustomModelData() == true && item.itemMeta?.customModelData == 210) {
            val complexGui =
                gui(title = mm.deserialize(ConfigManager.settings.resetGuiName), type = InventoryType.HOPPER) {
                    set(1, ItemStack(Material.LIME_DYE).apply {
                        val meta = this.itemMeta
                        meta.displayName(mm.deserialize("<b><color:#8aff80>✅ Reset the Timer</color></b>"))
                        this.itemMeta = meta
                    }) {
                        isCancelled = true
                        timer.remove(player.uniqueId)
                        player.sendMessage(mm.deserialize("<color:#4dff4a>Your timer has been reset!</color>"))
                        player.closeInventory()
                    }

                    set(3, ItemStack(Material.RED_DYE).apply {
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
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked.world.name != "onlyup") {
            timer.remove(event.whoClicked.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        timer.remove(event.player.uniqueId)
    }

    private fun startActionBarUpdater() {
        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val startTime = timer[player.uniqueId] ?: continue
                    val elapsedSeconds = Duration.between(startTime, Instant.now())
                    player.sendActionBar(mm.deserialize("<color:#f3ff6e>Timer: ${elapsedSeconds.toMinutesPart()}m ${elapsedSeconds.toSecondsPart()}s</color>"))
                }
            }
        }.runTaskTimer(OnlyUp.instance, 0L, 20L)
    }
}
