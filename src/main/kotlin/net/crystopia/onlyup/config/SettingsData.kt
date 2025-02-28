package net.crystopia.onlyup.config

import kotlinx.serialization.Serializable
import org.bukkit.Material
import java.time.Duration

@Serializable
data class SettingsData(
    var onlyups: MutableMap<String, OnlyUpData> = mutableMapOf(),
    val onlyUpGuiName: String,
    val leaderboardGuiName: String,
    val resetGuiName: String,
)

@Serializable
data class OnlyUpData(
    val worldName: String, val start: PosData, val end: PosData, val spawn: PosData, val guiItem: GuiItemData
)

@Serializable
data class PosData(
    val x: Double,
    val y: Double,
    val z: Double,
)

@Serializable
data class GuiItemData(
    val name: String, val lore: MutableList<String>, val material: Material, val customModelData: Int, val slot: Int
)

@Serializable
data class PlayersData(
    val players: MutableMap<String, PlayerData> = mutableMapOf(),
)

@Serializable
data class PlayerData(
    val uuid: String,
    val name: String,
    var onlyups: MutableMap<String, TimeData>,
)

@Serializable
data class TimeData(
    var time: kotlin.time.Duration,
    var bestTime: kotlin.time.Duration,
)