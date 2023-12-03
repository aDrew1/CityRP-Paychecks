package io.github.adrew1.cityrppaychecks;

import io.github.adrew1.cityrppaychecks.Commands.PaycheckCommand;
import io.github.adrew1.cityrppaychecks.Commands.PaycheckTabComplete;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class CityRPPaychecks extends JavaPlugin {

    private static Economy econ = null;
    public static Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {

        plugin = this;
        createFiles();

        // VaultAPI Init
        setupEconomy();
        if (!setupEconomy()) {
            log.severe(String.format("CityRP-Paychecks Disabled due to no Vault dependency found!"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("paycheck").setExecutor(new PaycheckCommand());
        this.getCommand("paycheck").setTabCompleter(new PaycheckTabComplete());

        Integer delay = this.getConfig().getInt("Paycheck Frequency") * 1200;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
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
        }, delay, delay);

        // Plugin Enabled
        log.info("CityRP - Paychecks v1.2.0 ENABLED!");
    }

    // Vault API
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    // CONFIG

    private File configf;
    private FileConfiguration config;
    public static CityRPPaychecks plugin;

    private void createFiles() {
        configf = new File(getDataFolder(), "config.yml");
        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = new YamlConfiguration();
        try {
            config.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }




}
