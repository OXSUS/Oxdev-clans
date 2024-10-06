package ox.dev.oxdevclans.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ox.dev.oxdevclans.Oxdev_clans;

import java.util.concurrent.ThreadLocalRandom;

public class DeathListener implements Listener {

    private final Oxdev_clans plugin;

    public DeathListener(Oxdev_clans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        Player killer = killed.getKiller();

        if (killer != null) {
            int rankingGained = ThreadLocalRandom.current().nextInt(1, 101);
            int rankingLost = rankingGained;

            plugin.getRankingManager().addRankingPoints(killer.getName(), rankingGained);
            plugin.getRankingManager().subtractRankingPoints(killed.getName(), rankingLost);
            plugin.getRankingManager().addKill(killer.getName());
            plugin.getRankingManager().addDeath(killed.getName());

            killer.sendMessage(colorize(plugin.getPluginConfig().getString("messages.kill_message")
                    .replace("{killer}", killer.getName())
                    .replace("{killed}", killed.getName())
                    .replace("{ranking_gain}", String.valueOf(rankingGained))));
            killed.sendMessage(colorize(plugin.getPluginConfig().getString("messages.death_message")
                    .replace("{killer}", killer.getName())
                    .replace("{ranking_loss}", String.valueOf(rankingLost))));
        }
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

