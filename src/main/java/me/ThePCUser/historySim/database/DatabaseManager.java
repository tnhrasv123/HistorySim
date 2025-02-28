package me.ThePCUser.historySim.database;

import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages database operations for the plugin
 */
public class DatabaseManager {

    private final HistorySim plugin;
    private Connection connection;

    public DatabaseManager(HistorySim plugin) {
        this.plugin = plugin;
    }

    /**
     * Connect to the database
     */
    public void connect() {
        String dbType = plugin.getConfig().getString("database.type", "sqlite");

        try {
            if (dbType.equalsIgnoreCase("mysql")) {
                connectMySQL();
            } else {
                connectSQLite();
            }

            MessageUtils.logInfo("Connected to database successfully.");
        } catch (SQLException e) {
            MessageUtils.logSevere("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectSQLite() throws SQLException {
        String dbPath = plugin.getDataFolder().getAbsolutePath() + "/database.db";
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void connectMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "historysim");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&autoReconnect=true";

        connection = DriverManager.getConnection(url, username, password);
    }

    /**
     * Disconnect from the database
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                MessageUtils.logInfo("Disconnected from database.");
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error disconnecting from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create necessary database tables
     */
    public void createTables() {
        try {
            // Players table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "username VARCHAR(16) NOT NULL, " +
                            "current_era VARCHAR(32) NOT NULL DEFAULT 'PREHISTORIC', " +
                            "last_login BIGINT NOT NULL, " +
                            "playtime_ticks BIGINT NOT NULL DEFAULT 0)"
            );

            // Player Era Data table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS player_era_data (" +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "era VARCHAR(32) NOT NULL, " +
                            "visited BOOLEAN NOT NULL DEFAULT 0, " +
                            "progress INT NOT NULL DEFAULT 0, " +
                            "PRIMARY KEY (uuid, era))"
            );

            // Technologies table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS player_technologies (" +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "tech_id VARCHAR(64) NOT NULL, " +
                            "unlock_time BIGINT NOT NULL, " +
                            "PRIMARY KEY (uuid, tech_id))"
            );

            // Milestones table
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS player_milestones (" +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "era VARCHAR(32) NOT NULL, " +
                            "milestone_id VARCHAR(64) NOT NULL, " +
                            "completion_time BIGINT NOT NULL, " +
                            "PRIMARY KEY (uuid, era, milestone_id))"
            );

        } catch (SQLException e) {
            MessageUtils.logSevere("Error creating database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update a player's current era
     */
    public void updatePlayerEra(UUID playerId, EraType era) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET current_era = ? WHERE uuid = ?"
            );
            stmt.setString(1, era.name());
            stmt.setString(2, playerId.toString());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                // Player doesn't exist, create new record
                insertNewPlayer(playerId, era);
            }

            // Mark era as visited
            markEraVisited(playerId, era);

        } catch (SQLException e) {
            MessageUtils.logSevere("Error updating player era: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Insert a new player record
     */
    private void insertNewPlayer(UUID playerId, EraType era) throws SQLException {
        String username = Bukkit.getPlayer(playerId).getName();
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO players (uuid, username, current_era, last_login, playtime_ticks) " +
                        "VALUES (?, ?, ?, ?, 0)"
        );
        stmt.setString(1, playerId.toString());
        stmt.setString(2, username);
        stmt.setString(3, era.name());
        stmt.setLong(4, System.currentTimeMillis());

        stmt.executeUpdate();
    }

    /**
     * Mark an era as visited by a player
     */
    private void markEraVisited(UUID playerId, EraType era) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO player_era_data (uuid, era, visited) VALUES (?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE visited = 1"
        );
        stmt.setString(1, playerId.toString());
        stmt.setString(2, era.name());

        stmt.executeUpdate();
    }

    /**
     * Get a player's current era
     */
    public EraType getPlayerEra(UUID playerId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT current_era FROM players WHERE uuid = ?"
            );
            stmt.setString(1, playerId.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return EraType.valueOf(rs.getString("current_era"));
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error getting player era: " + e.getMessage());
            e.printStackTrace();
        }

        return EraType.PREHISTORIC; // Default
    }

    /**
     * Check if a player has visited an era
     */
    public boolean hasPlayerVisitedEra(UUID playerId, EraType era) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT visited FROM player_era_data WHERE uuid = ? AND era = ?"
            );
            stmt.setString(1, playerId.toString());
            stmt.setString(2, era.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("visited");
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error checking if player visited era: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Save era-specific player data
     */
    public void savePlayerEraData(UUID playerId, EraType era) {
        try {
            // Just mark it as visited for now
            // In a real implementation, this would save more era-specific data
            markEraVisited(playerId, era);
        } catch (SQLException e) {
            MessageUtils.logSevere("Error saving player era data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save a player's general data
     */
    public void savePlayerData(UUID playerId) {
        try {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) return;

            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET username = ?, last_login = ? WHERE uuid = ?"
            );
            stmt.setString(1, player.getName());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, playerId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            MessageUtils.logSevere("Error saving player data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a list of technologies a player has unlocked
     */
    public List<String> getPlayerTechnologies(UUID playerId) {
        List<String> technologies = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT tech_id FROM player_technologies WHERE uuid = ?"
            );
            stmt.setString(1, playerId.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                technologies.add(rs.getString("tech_id"));
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error getting player technologies: " + e.getMessage());
            e.printStackTrace();
        }

        return technologies;
    }

    /**
     * Check if a player has unlocked a specific technology
     */
    public boolean hasPlayerTechnology(UUID playerId, String technologyId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT 1 FROM player_technologies WHERE uuid = ? AND tech_id = ?"
            );
            stmt.setString(1, playerId.toString());
            stmt.setString(2, technologyId);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            MessageUtils.logSevere("Error checking player technology: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Unlock a technology for a player
     */
    public void unlockTechnology(UUID playerId, String technologyId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO player_technologies (uuid, tech_id, unlock_time) VALUES (?, ?, ?)"
            );
            stmt.setString(1, playerId.toString());
            stmt.setString(2, technologyId);
            stmt.setLong(3, System.currentTimeMillis());

            stmt.executeUpdate();
        } catch (SQLException e) {
            MessageUtils.logSevere("Error unlocking technology: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get a list of completed milestones for a player in a specific era
     */
    public List<String> getCompletedMilestones(UUID playerId, EraType era) {
        List<String> milestones = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT milestone_id FROM player_milestones WHERE uuid = ? AND era = ?"
            );
            stmt.setString(1, playerId.toString());
            stmt.setString(2, era.name());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                milestones.add(rs.getString("milestone_id"));
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error getting completed milestones: " + e.getMessage());
            e.printStackTrace();
        }

        return milestones;
    }

    /**
     * Get the progression percentage for a player in a specific era
     */
    public int getEraProgressionPercentage(UUID playerId, EraType era) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT progress FROM player_era_data WHERE uuid = ? AND era = ?"
            );
            stmt.setString(1, playerId.toString());
            stmt.setString(2, era.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("progress");
            }
        } catch (SQLException e) {
            MessageUtils.logSevere("Error getting era progression: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}