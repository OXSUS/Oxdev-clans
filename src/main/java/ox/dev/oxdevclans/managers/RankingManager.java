package ox.dev.oxdevclans.managers;

import ox.dev.oxdevclans.Oxdev_clans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RankingManager {

    private final Map<String, Integer> rankings;
    private final Map<String, Integer> kills;
    private final Map<String, Integer> deaths;
    private final int startRanking;
    private final Oxdev_clans plugin;

    public RankingManager(Oxdev_clans plugin, int startRanking) {
        this.plugin = plugin;
        this.rankings = new HashMap<>();
        this.kills = new HashMap<>();
        this.deaths = new HashMap<>();
        this.startRanking = startRanking;
        loadRankings();
    }

    public void addRankingPoints(String playerName, int points) {
        rankings.put(playerName, rankings.getOrDefault(playerName, startRanking) + points);
        saveRanking(playerName);
    }

    public void subtractRankingPoints(String playerName, int points) {
        rankings.put(playerName, Math.max(0, rankings.getOrDefault(playerName, startRanking) - points));
        saveRanking(playerName);
    }

    public int getRankingPoints(String playerName) {
        return rankings.getOrDefault(playerName, startRanking);
    }

    public void addKill(String playerName) {
        kills.put(playerName, kills.getOrDefault(playerName, 0) + 1);
        saveRanking(playerName);
    }

    public int getKills(String playerName) {
        return kills.getOrDefault(playerName, 0);
    }

    public void addDeath(String playerName) {
        deaths.put(playerName, deaths.getOrDefault(playerName, 0) + 1);
        saveRanking(playerName);
    }

    public int getDeaths(String playerName) {
        return deaths.getOrDefault(playerName, 0);
    }

    public void resetStats(String playerName) {
        rankings.put(playerName, startRanking);
        kills.put(playerName, 0);
        deaths.put(playerName, 0);
        saveRanking(playerName);
    }

    public void loadRankings() {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM player_stats");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String player = rs.getString("player");
                rankings.put(player, rs.getInt("ranking"));
                kills.put(player, rs.getInt("kills"));
                deaths.put(player, rs.getInt("deaths"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player stats: " + e.getMessage(), e);
        }
    }

    private void saveRanking(String playerName) {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO player_stats (player, ranking, kills, deaths) VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE ranking = VALUES(ranking), kills = VALUES(kills), deaths = VALUES(deaths)")) {

            ps.setString(1, playerName);
            ps.setInt(2, rankings.getOrDefault(playerName, startRanking));
            ps.setInt(3, kills.getOrDefault(playerName, 0));
            ps.setInt(4, deaths.getOrDefault(playerName, 0));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save ranking for player: " + playerName + ". " + e.getMessage(), e);
        }
    }
}
