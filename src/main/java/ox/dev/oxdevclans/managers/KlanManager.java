package ox.dev.oxdevclans.managers;

import ox.dev.oxdevclans.data.Klan;
import ox.dev.oxdevclans.Oxdev_clans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class KlanManager {

    private final Map<String, Klan> klany;
    private final Oxdev_clans plugin;

    public KlanManager(Oxdev_clans plugin) {
        this.plugin = plugin;
        this.klany = new HashMap<>();
        loadClans();
    }

    public boolean createKlan(String name, String owner) {
        if (klany.containsKey(name)) {
            return false;
        }
        Klan klan = new Klan(name, owner);
        klany.put(name, klan);

        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO clans (name, owner) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, owner);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create clan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean deleteKlan(String name) {
        if (!klany.containsKey(name)) {
            return false;
        }
        klany.remove(name);

        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM clans WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete clan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean addLeader(String klanName, String leader) {
        Klan klan = klany.get(klanName);
        if (klan != null) {
            boolean added = klan.addLeader(leader);
            if (added) {
                saveMembers(klan);
            }
            return added;
        }
        return false;
    }

    public boolean kickMember(String klanName, String playerName) {
        Klan klan = klany.get(klanName);
        if (klan != null) {
            boolean removed = klan.removeMember(playerName);
            if (removed) {
                saveMembers(klan);
            }
            return removed;
        }
        return false;
    }

    public boolean leaveKlan(String klanName, String playerName) {
        Klan klan = klany.get(klanName);
        if (klan != null) {
            boolean removed = klan.removeMember(playerName);
            if (removed) {
                saveMembers(klan);
            }
            return removed;
        }
        return false;
    }

    public Klan getKlan(String name) {
        return klany.get(name);
    }

    public Klan getKlanByMember(String playerName) {
        for (Klan klan : klany.values()) {
            if (klan.getMembers().contains(playerName)) {
                return klan;
            }
        }
        return null;
    }

    public void loadClans() {
        try (Connection connection = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM clans");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String owner = rs.getString("owner");
                Klan klan = new Klan(name, owner);

                try (PreparedStatement psMembers = connection.prepareStatement("SELECT * FROM clan_members WHERE clan_id = ?")) {
                    psMembers.setInt(1, rs.getInt("id"));
                    try (ResultSet rsMembers = psMembers.executeQuery()) {
                        while (rsMembers.next()) {
                            klan.addMember(rsMembers.getString("member"));
                        }
                    }
                }

                klany.put(name, klan);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load clans: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveMembers(Klan klan) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            try (PreparedStatement psDelete = connection.prepareStatement("DELETE FROM clan_members WHERE clan_id = (SELECT id FROM clans WHERE name = ?)")) {
                psDelete.setString(1, klan.getName());
                psDelete.executeUpdate();
            }

            for (String member : klan.getMembers()) {
                try (PreparedStatement psInsert = connection.prepareStatement("INSERT INTO clan_members (clan_id, member) VALUES ((SELECT id FROM clans WHERE name = ?), ?)")) {
                    psInsert.setString(1, klan.getName());
                    psInsert.setString(2, member);
                    psInsert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save members for clan: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
