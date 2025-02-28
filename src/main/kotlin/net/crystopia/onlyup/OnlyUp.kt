package net.crystopia.onlyup

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import gg.flyte.twilight.twilight
import net.crystopia.onlyup.config.ConfigManager
import net.crystopia.onlyup.commands.OnlyUpCommand
import net.crystopia.onlyup.events.PlayerEventListener
import org.bukkit.plugin.java.JavaPlugin


class OnlyUp : JavaPlugin() {

    companion object {
        lateinit var instance: OnlyUp
    }

    init {
        instance = this
    }

    override fun onEnable() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this))
        CommandAPI.onEnable()

        val twilight = twilight(this)

        // ConfigManager
        val settings = ConfigManager.settings
        val players = ConfigManager.players

        OnlyUpCommand()

        server.pluginManager.registerEvents(PlayerEventListener(this), this)
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}

