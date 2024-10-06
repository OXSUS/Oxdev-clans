package ox.dev.oxdevclans.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ox.dev.oxdevclans.Oxdev_clans;
import ox.dev.oxdevclans.data.Klan;

public class OxdevClansPlaceholder extends PlaceholderExpansion {

    private final Oxdev_clans plugin;

    public OxdevClansPlaceholder(Oxdev_clans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "oxclans";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("clans")) {
            Klan klan = plugin.getKlanManager().getKlanByMember(player.getName());
            if (klan != null) {
                return klan.getName();
            } else {
                return "Brak klanu";
            }
        }

        if (identifier.equals("pkt")) {
            int ranking = plugin.getRankingManager().getRankingPoints(player.getName());
            return String.valueOf(ranking);
        }

        if (identifier.equals("kills")) {
            int kills = plugin.getRankingManager().getKills(player.getName());
            return String.valueOf(kills);
        }

        if (identifier.equals("death")) {
            int deaths = plugin.getRankingManager().getDeaths(player.getName());
            return String.valueOf(deaths);
        }

        return null;
    }
}

