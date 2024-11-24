package net.crystopia.onlyup

import net.crystopia.onlyup.events.PlayerEventListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.UUID

class OnlyUp : JavaPlugin() {

    private lateinit var connection: Connection
    val startPoints = hashMapOf(
        "1" to Pair(
            Location(Bukkit.getWorld("onlyup"), 388.0, -5.0, 802.0),
            Location(Bukkit.getWorld("onlyup"), 391.0, 250.0, 808.0)
        ),
        "2" to Pair(
            Location(Bukkit.getWorld("world"), 20.0, 30.0, 20.0),
            Location(Bukkit.getWorld("world"), 25.0, 30.0, 25.0)
        )
    )

    override fun onEnable() {
        setupDatabase()
        server.pluginManager.registerEvents(PlayerEventListener(this), this)
    }

    override fun onDisable() {
        connection.close()
    }

    private fun setupDatabase() {
        val url = "jdbc:mysql://mysql.db.system.tnsstudio.net:3306/crystopia_onlyup"
        val user = "root"
        val password = "mhgdfzhjdhfnfgi7587685uhdfoo~9mfvm"


        connection = DriverManager.getConnection(url, user, password)

        val createTableStatement = connection.prepareStatement(
            """
            CREATE TABLE IF NOT EXISTS player_times (
                uuid VARCHAR(36) PRIMARY KEY,
                best_time BIGINT
            )
        """
        )
        createTableStatement.executeUpdate()
        createTableStatement.close()
    }

    fun updatePlayerTimeIfBetter(uuid: UUID, time: Long) {
        val selectStatement = connection.prepareStatement("SELECT best_time FROM player_times WHERE uuid = ?")
        selectStatement.setString(1, uuid.toString())
        val resultSet = selectStatement.executeQuery()

        if (resultSet.next()) {
            val bestTime = resultSet.getLong("best_time")
            if (time < bestTime) {
                val updateStatement =
                    connection.prepareStatement("UPDATE player_times SET best_time = ? WHERE uuid = ?")
                updateStatement.setLong(1, time)
                updateStatement.setString(2, uuid.toString())
                updateStatement.executeUpdate()
                updateStatement.close()
            }
        } else {
            val insertStatement =
                connection.prepareStatement("INSERT INTO player_times (uuid, best_time) VALUES (?, ?)")
            insertStatement.setString(1, uuid.toString())
            insertStatement.setLong(2, time)
            insertStatement.executeUpdate()
            insertStatement.close()
        }

        resultSet.close()
        selectStatement.close()
    }

    fun getPlayerBestTimes(): Map<UUID, Long> {
        val playerBestTimes = HashMap<UUID, Long>()
        val selectStatement = connection.prepareStatement("SELECT uuid, best_time FROM player_times")
        val resultSet = selectStatement.executeQuery()

        while (resultSet.next()) {
            val uuid = UUID.fromString(resultSet.getString("uuid"))
            val bestTime = resultSet.getLong("best_time")
            playerBestTimes[uuid] = bestTime
        }

        resultSet.close()
        selectStatement.close()

        return playerBestTimes
    }
}

