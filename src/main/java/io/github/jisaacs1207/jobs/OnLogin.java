package io.github.jisaacs1207.jobs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;

public class OnLogin implements Listener {

    @EventHandler
    public void PlayerFileCreation(PlayerLoginEvent event) {
        String player = event.getPlayer().getName();
        //Create a reference to (playername).yml
        File playerfile = new File(Jobs.plugin.getDataFolder()+"/players/"+player);

        //Check if file exists in the referenced location
        if(!playerfile.exists())
        {
            // profile creation
            Methods.generateNewPlayerFile(player);
        }
        Methods.populateMapFromPFile(player);
        Methods.updateLastJoin(player);
    }
}
