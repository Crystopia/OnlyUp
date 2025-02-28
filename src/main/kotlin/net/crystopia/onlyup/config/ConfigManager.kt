package net.crystopia.onlyup.config

import java.io.File

object ConfigManager {

    private val settingsFile = File("plugins/OnlyUp/config.json")
    private val playerFile = File("plugins/OnlyUp/players.json")

    var settings = settingsFile.loadConfig(
        SettingsData(
            leaderboardGuiName = "", resetGuiName = "", onlyUpGuiName = ""
        )
    )

    var players = playerFile.loadConfig(PlayersData())

    fun save() {
        settingsFile.writeText(json.encodeToString(settings))
        playerFile.writeText(json.encodeToString(players))
    }

    fun reload() {
        settings = loadFromFile(settingsFile)
        players = loadFromFile(playerFile)
    }

}