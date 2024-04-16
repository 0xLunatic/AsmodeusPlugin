package lunatic.asmodeusplugin.db;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.UUID;

public class AsmodeusDatabase {

    private Connection connection;
    private FileConfiguration config;

    public AsmodeusDatabase(FileConfiguration config) {
        this.config = config;
    }

    public void connect() throws SQLException {
        String host = config.getString("database.host");
        String port = config.getString("database.port");
        String database = config.getString("database.database");
        String user = config.getString("database.user");
        String password = config.getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        connection = DriverManager.getConnection(url, user, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void registerDataPlayer(UUID playerUUID, String playerName) throws SQLException {
        String sql = "INSERT INTO asmodeus (PlayerUUID, PlayerName, TreasurePower, AsmodeusDamageResistance, AsmodeusDamageMultiplier, AsmodeusKilled, DemonEssence) " +
                "VALUES (?, ?, 0, 0, 0, 0, 0) " +
                "ON DUPLICATE KEY UPDATE PlayerUUID=PlayerUUID"; // This line is for MySQL databases
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }


    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS asmodeus (" +
                "PlayerUUID VARCHAR(36) PRIMARY KEY," +
                "PlayerName VARCHAR(255)," +
                "TreasurePower INT DEFAULT 0," +
                "AsmodeusDamageResistance INT DEFAULT 0," +
                "AsmodeusDamageMultiplier INT DEFAULT 0," +
                "AsmodeusKilled INT DEFAULT 0)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    public void addDemonEssence(UUID playerUUID, String playerName, int amount) throws SQLException {
        String sql = "UPDATE asmodeus SET DemonEssence = DemonEssence + ? WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, playerName);
            statement.executeUpdate();
        }
    }

    public void subtractDemonEssence(UUID playerUUID, String playerName, int amount) throws SQLException {
        String sql = "UPDATE asmodeus SET DemonEssence = DemonEssence - ? WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, playerName);
            statement.executeUpdate();
        }
    }
    public int getDemonEssence(UUID playerUUID, String playerName) throws SQLException {
        String sql = "SELECT DemonEssence FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        int demonEssence = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    demonEssence = resultSet.getInt("DemonEssence");
                }
            }
        }
        return demonEssence;
    }

    public void addTreasurePower(UUID playerUUID, String playerName) throws SQLException {
        String sql = "UPDATE asmodeus SET TreasurePower = TreasurePower + 1 WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public void subtractTreasurePower(UUID playerUUID, String playerName, int amount) throws SQLException {
        String sql = "UPDATE asmodeus SET TreasurePower = TreasurePower - ? WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, playerName);
            statement.executeUpdate();
        }
    }

    public void setAsmodeusKilled(UUID playerUUID, String playerName, boolean asmodeusKilled) throws SQLException {
        int value = asmodeusKilled ? 1 : 0;
        String sql = "UPDATE asmodeus SET AsmodeusKilled = ? WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            statement.setString(2, playerUUID.toString());
            statement.setString(3, playerName);
            statement.executeUpdate();
        }
    }

    public void addAsmodeusKilled(UUID playerUUID, String playerName) throws SQLException {
        String sql = "UPDATE asmodeus SET AsmodeusKilled = AsmodeusKilled + 1 WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public void subtractAsmodeusKilled(UUID playerUUID, String playerName) throws SQLException {
        String sql = "UPDATE asmodeus SET AsmodeusKilled = AsmodeusKilled - 1 WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public int getTreasurePower(UUID playerUUID, String playerName) throws SQLException {
        String sql = "SELECT TreasurePower FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        int treasurePower = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    treasurePower = resultSet.getInt("TreasurePower");
                }
            }
        }
        return treasurePower;
    }

    public int getAsmodeusKilledCount(UUID playerUUID, String playerName) throws SQLException {
        String sql = "SELECT AsmodeusKilled FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        int asmodeusKilledCount = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    asmodeusKilledCount = resultSet.getInt("AsmodeusKilled");
                }
            }
        }
        return asmodeusKilledCount;
    }

    public void addDamageResistance(UUID playerUUID, String playerName) throws SQLException {
        String sql = "UPDATE asmodeus SET AsmodeusDamageResistance = AsmodeusDamageResistance + 1 WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public void addDamageBooster(UUID playerUUID, String playerName) throws SQLException {
        String sql = "UPDATE asmodeus SET AsmodeusDamageMultiplier = AsmodeusDamageMultiplier + 1 WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public int getDamageResistance(UUID playerUUID, String playerName) throws SQLException {
        String sql = "SELECT AsmodeusDamageResistance FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        int damageResistance = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    damageResistance = resultSet.getInt("AsmodeusDamageResistance");
                }
            }
        }
        return damageResistance;
    }

    public int getDamageBooster(UUID playerUUID, String playerName) throws SQLException {
        String sql = "SELECT AsmodeusDamageMultiplier FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        int damageBooster = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    damageBooster = resultSet.getInt("AsmodeusDamageMultiplier");
                }
            }
        }
        return damageBooster;
    }


    public void deletePlayerData(UUID playerUUID, String playerName) throws SQLException {
        String sql = "DELETE FROM asmodeus WHERE PlayerUUID = ? AND PlayerName = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }
}
