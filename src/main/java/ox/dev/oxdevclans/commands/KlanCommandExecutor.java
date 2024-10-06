package ox.dev.oxdevclans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ox.dev.oxdevclans.data.Klan;
import ox.dev.oxdevclans.Oxdev_clans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KlanCommandExecutor implements CommandExecutor, TabCompleter {

    private final Oxdev_clans plugin;

    public KlanCommandExecutor(Oxdev_clans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Komenda tylko dla graczy!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || (args.length == 1 && args[0].trim().isEmpty())) {
            List<String> commands = getAvailableCommands(player);
            player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.help_header")));
            for (String cmd : commands) {
                player.sendMessage(colorize(cmd));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "stworz":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan stworz <nazwa>");
                    return true;
                }
                String klanName = args[1];
                if (klanName.length() < 3) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.clan_name_too_short")));
                    return true;
                }
                boolean created = plugin.getKlanManager().createKlan(klanName, player.getName());
                if (created) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.create_clan_success").replace("{klan}", klanName)));
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.create_clan_failure").replace("{klan}", klanName)));
                }
                break;
            case "usun":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan usun <nazwa>");
                    return true;
                }
                String klanToDelete = args[1];
                boolean deleted = plugin.getKlanManager().deleteKlan(klanToDelete);
                if (deleted) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.delete_clan_success").replace("{klan}", klanToDelete)));
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.delete_clan_failure").replace("{klan}", klanToDelete)));
                }
                break;
            case "dodajlidera":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan dodajlidera <nazwa> <lider>");
                    return true;
                }
                String klanNameToAddLeader = args[1];
                String newLeader = args[2];
                boolean addedLeader = plugin.getKlanManager().addLeader(klanNameToAddLeader, newLeader);
                if (addedLeader) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.add_leader_success")
                            .replace("{leader}", newLeader)
                            .replace("{klan}", klanNameToAddLeader)));
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.add_leader_failure")
                            .replace("{leader}", newLeader)
                            .replace("{klan}", klanNameToAddLeader)));
                }
                break;
            case "wyrzuc":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan wyrzuc <nazwa> <gracz>");
                    return true;
                }
                String klanNameToKickFrom = args[1];
                String playerToKick = args[2];
                boolean kicked = plugin.getKlanManager().kickMember(klanNameToKickFrom, playerToKick);
                if (kicked) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.kick_member_success")
                            .replace("{player}", playerToKick)
                            .replace("{klan}", klanNameToKickFrom)));
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.kick_member_failure")
                            .replace("{player}", playerToKick)
                            .replace("{klan}", klanNameToKickFrom)));
                }
                break;
            case "opusc":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan opusc <nazwa>");
                    return true;
                }
                String klanNameToLeave = args[1];
                boolean left = plugin.getKlanManager().leaveKlan(klanNameToLeave, player.getName());
                if (left) {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.leave_clan_success").replace("{klan}", klanNameToLeave)));
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.leave_clan_failure").replace("{klan}", klanNameToLeave)));
                }
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan info <nazwa>");
                    return true;
                }
                String klanNameToDisplayInfo = args[1];
                displayKlanInfo(player, klanNameToDisplayInfo);
                break;
            case "ranking":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan ranking <gracz>");
                    return true;
                }
                String playerToCheckRanking = args[1];
                if (plugin.getServer().getOfflinePlayer(playerToCheckRanking).hasPlayedBefore()) {
                    displayPlayerRanking(player, playerToCheckRanking);
                } else {
                    player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.never_played").replace("{player}", playerToCheckRanking)));
                }
                break;
            case "admin":
                if (args.length < 3 || !args[1].equalsIgnoreCase("reset") || !player.hasPermission("oxclans.admin")) {
                    player.sendMessage(ChatColor.RED + "Użycie: /klan admin reset <nick>");
                    return true;
                }
                String playerToReset = args[2];
                plugin.getRankingManager().resetStats(playerToReset);
                player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.admin_reset_success").replace("{player}", playerToReset)));
                break;
            case "reload":
                plugin.reloadConfigs();
                player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.reload_success")));
                break;
            default:
                player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.unknown_command")));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("stworz", "usun", "dodajlidera", "wyrzuc", "opusc", "info", "ranking", "admin", "reload");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return Arrays.asList("reset");
        }

        return new ArrayList<>();
    }

    private List<String> getAvailableCommands(Player player) {
        List<String> commands = new ArrayList<>();
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "stworz <nazwa>")
                .replace("{description}", "Tworzy nowy klan."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "usun <nazwa>")
                .replace("{description}", "Usuwa istniejący klan."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "dodajlidera <nazwa> <lider>")
                .replace("{description}", "Dodaje lidera do klanu."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "wyrzuc <nazwa> <gracz>")
                .replace("{description}", "Wyrzuca gracza z klanu."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "opusc <nazwa>")
                .replace("{description}", "Opuść klan."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "info <nazwa>")
                .replace("{description}", "Wyświetla informacje o klanie."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "ranking <gracz>")
                .replace("{description}", "Wyświetla ranking gracza."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "admin reset <nick>")
                .replace("{description}", "Resetuje statystyki gracza."));
        commands.add(plugin.getPluginConfig().getString("messages.help_command")
                .replace("{command}", "reload")
                .replace("{description}", "Przeładowuje konfigurację pluginu."));
        return commands;
    }

    private void displayKlanInfo(Player player, String klanName) {
        Klan klan = plugin.getKlanManager().getKlan(klanName);
        if (klan != null) {
            player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.clan_info")
                    .replace("{klan}", klanName)
                    .replace("{leaders}", String.join(", ", klan.getLeaders()))
                    .replace("{members}", String.join(", ", klan.getMembers()))));
        } else {
            player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.clan_not_exist").replace("{klan}", klanName)));
        }
    }

    private void displayPlayerRanking(Player player, String playerName) {
        int ranking = plugin.getRankingManager().getRankingPoints(playerName);
        player.sendMessage(colorize(plugin.getPluginConfig().getString("messages.player_ranking")
                .replace("{player}", playerName)
                .replace("{ranking}", String.valueOf(ranking))));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}


