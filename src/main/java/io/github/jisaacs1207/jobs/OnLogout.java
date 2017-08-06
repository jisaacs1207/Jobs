package io.github.jisaacs1207.jobs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnLogout implements Listener{
    @EventHandler(priority= EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        String player = event.getPlayer().getName();
        Methods.saveMapToPFile(player);
        Jobs.playerStats.put(player, null);
    }
}
