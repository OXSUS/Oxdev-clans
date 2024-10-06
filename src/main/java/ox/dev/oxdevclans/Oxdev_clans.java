package ox.dev.oxdevclans;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ox.dev.oxdevclans.commands.KlanCommandExecutor;
import ox.dev.oxdevclans.listeners.DeathListener;
import ox.dev.oxdevclans.managers.DatabaseManager;
import ox.dev.oxdevclans.managers.KlanManager;
import ox.dev.oxdevclans.managers.RankingManager;
import ox.dev.oxdevclans.placeholder.OxdevClansPlaceholder;

import java.sql.SQLException;

public class Oxdev_clans extends JavaPlugin {

    private KlanManager klanManager;
    private RankingManager rankingManager;
    private DatabaseManager databaseManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
            databaseManager.setupDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Nie udało się połączyć z bazą danych! Szczegóły: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        klanManager = new KlanManager(this);
        rankingManager = new RankingManager(this, config.getInt("ranking.start", 1000));

        KlanCommandExecutor commandExecutor = new KlanCommandExecutor(this);
        getCommand("klan").setExecutor(commandExecutor);
        getCommand("klan").setTabCompleter(commandExecutor);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OxdevClansPlaceholder(this).register();
        }

        getLogger().info("Plugin oxdev-clans został włączony");
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
        getLogger().info("Plugin oxdev-clans został wyłączony");
    }

    public KlanManager getKlanManager() {
        return klanManager;
    }

    public RankingManager getRankingManager() {
        return rankingManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public void reloadConfigs() {
        reloadConfig();
        config = getConfig();

        klanManager.loadClans();
        rankingManager.loadRankings();
    }
}
