package io.github.adrew1.cityrppaychecks.Commands;

import io.github.adrew1.cityrppaychecks.CityRPPaychecks;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import sun.security.krb5.Config;

import java.util.ArrayList;
import java.util.List;


//COMMAND: /pc <help/reload/send> <all/player>
public class PaycheckCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // CONFIG MESSAGES
        String prefix = CityRPPaychecks.plugin.getConfig().getString("Prefix");
        String incorrectUsage = CityRPPaychecks.plugin.getConfig().getString("Incorrect Usage");
        String noPermission = CityRPPaychecks.plugin.getConfig().getString("No Permission");
        String invalidPlayer = CityRPPaychecks.plugin.getConfig().getString("Invalid Player");
        String reloadMsg = CityRPPaychecks.plugin.getConfig().getString("Reload");

        // CONFIG PLACEHOLDER REPLACE
        if (prefix.contains("%p%")) {
            prefix = prefix.replaceAll("%p%", sender.getName());
        }
        if (incorrectUsage.contains("%p%")) {
            incorrectUsage = incorrectUsage.replaceAll("%p%", sender.getName());
        }
        if (noPermission.contains("%p%")) {
            noPermission = noPermission.replaceAll("%p%", sender.getName());
        }
        if (reloadMsg.contains("%p%")) {
            reloadMsg = reloadMsg.replaceAll("%p%", sender.getName());
        }

        if (!sender.hasPermission("paychecks.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + noPermission));
            return true;
        }

        // COMMAND

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + incorrectUsage));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            CityRPPaychecks.plugin.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + reloadMsg));
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l===================================="));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l» &e/pc help &8- &7View this list of commands."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l» &e/pc reload &8- &7Reload the plugin's configuration."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l» &e/pc send &ball &8- &7Manually send all players their paycheck."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l» &e/pc send &b<player> &8- &7Manually send a player their paycheck."));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l» &cCreated by aDrew1 | https://github.com/aDrew1"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l===================================="));
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {

            if (args[1].equalsIgnoreCase("all")) {
                for (Player players : Bukkit.getServer().getOnlinePlayers()) {
                    for (String key : CityRPPaychecks.plugin.getConfig().getConfigurationSection("Paychecks").getKeys(false)) {
                        ConfigurationSection pcConfigSec = CityRPPaychecks.plugin.getConfig().getConfigurationSection("Paychecks." + key);
                        double amt = pcConfigSec.getDouble("amt");
                        String permission = pcConfigSec.getString("perm");

                        if (players.hasPermission(permission)) {
                            List<String> payMsgStrList = CityRPPaychecks.plugin.getConfig().getStringList("Message");
                            List<String> payMsg = new ArrayList<>();
                            CityRPPaychecks.getEconomy().depositPlayer(players, amt);

                            for (String s : payMsgStrList) {
                                if (!s.contains("%a%")) {
                                    payMsg.add(s);
                                }
                                if (s.contains("%a%")) {
                                    String pmString = s;
                                    String money = Double.toString(amt);
                                    pmString = s.replaceAll("%a%", money);
                                    payMsg.add(pmString);
                                }
                            }
                            for (int i = 0; i < payMsg.size(); i++) {
                                players.sendMessage(ChatColor.translateAlternateColorCodes('&', payMsg.get(i)));
                            }
                        }
                    }
                }
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                if (invalidPlayer.contains("%p%")) {
                    invalidPlayer = invalidPlayer.replaceAll("%p%", args[1]);
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + invalidPlayer));
                return true;
            }

            for (String key : CityRPPaychecks.plugin.getConfig().getConfigurationSection("Paychecks").getKeys(false)) {
                ConfigurationSection pcConfigSec = CityRPPaychecks.plugin.getConfig().getConfigurationSection("Paychecks." + key);
                double amt = pcConfigSec.getDouble("amt");
                String permission = pcConfigSec.getString("perm");

                if (target.hasPermission(permission)) {
                    List<String> payMsgStrList = CityRPPaychecks.plugin.getConfig().getStringList("Message");
                    List<String> payMsg = new ArrayList<>();
                    CityRPPaychecks.getEconomy().depositPlayer(target, amt);

                    for (String s : payMsgStrList) {
                        if (!s.contains("%a%")) {
                            payMsg.add(s);
                        }
                        if (s.contains("%a%")) {
                            String pmString = s;
                            String money = Double.toString(amt);
                            pmString = s.replaceAll("%a%", money);
                            payMsg.add(pmString);
                        }
                    }
                    for (int i = 0; i < payMsg.size(); i++) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', payMsg.get(i)));
                    }
                }
            }
            return true;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + " " + incorrectUsage));
        return true;
    }
}
