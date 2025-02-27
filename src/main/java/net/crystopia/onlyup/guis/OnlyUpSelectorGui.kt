package net.crystopia.onlyup.guis

import gg.flyte.twilight.gui.GUI.Companion.openInventory
import gg.flyte.twilight.gui.gui
import net.crystopia.onlyup.config.ConfigManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.stream.Collectors


class OnlyUpSelectorGui {
    private val mm = MiniMessage.miniMessage()

    fun openGUI(player: Player) {

        val complexGui = gui(mm.deserialize(ConfigManager.settings.onlyUpGuiName)) {

            ConfigManager.settings.onlyups.forEach { onlyup ->
                set(onlyup.value.guiItem.slot, ItemStack(onlyup.value.guiItem.material).apply {
                    val meta = this.itemMeta

                    meta.displayName(mm.deserialize(onlyup.value.guiItem.name))
                    meta.setCustomModelData(onlyup.value.guiItem.customModelData)
                    val lore: List<Component> =
                        onlyup.value.guiItem.lore.stream().map(mm::deserialize).collect(Collectors.toList())
                    meta.lore(lore)

                    this.itemMeta = meta
                }) {
                    isCancelled = true
                    player.closeInventory()

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
                        0, resetItem
                    )

                    player.teleport(
                        Location(
                            Bukkit.getWorld(onlyup.value.worldName),
                            onlyup.value.spawn.x,
                            onlyup.value.spawn.y,
                            onlyup.value.spawn.z
                        )
                    )
                }
            }


        }
        player.openInventory(complexGui)

    }
}