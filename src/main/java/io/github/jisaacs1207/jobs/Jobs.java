package io.github.jisaacs1207.jobs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Jobs extends JavaPlugin implements Listener{

    public static Jobs plugin;
    public static HashMap<String, PlayerConfig> playerStats = new HashMap<String, PlayerConfig>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        getServer().getPluginManager().registerEvents(this, this);

        // Populate pfile hashmap
        for (Player player : plugin.getServer().getOnlinePlayers()) Methods.populateMapFromPFile(player);

        // default config
        saveDefaultConfig();

        // logging
        getLogger().info("Jobs now loaded.");

        // register classes
        registerEvents(this, new Commands(), new Alchemist(), new Artisan(), new Beastmaster(),
                new Cleric(), new Laborer(), new Mage(), new Merchant(), new Psionic(), new Ranger(),
                new Rogue(), new Scribe(), new Thief(), new Tinkerer(), new Warrior());
        this.getServer().getPluginManager().registerEvents(new OnLogin(), this);
        this.getServer().getPluginManager().registerEvents(new OnLogout(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin = null;
    }


    public static void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

}

