package io.github.jisaacs1207.jobs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Thief implements Listener{



    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent peek){
        Player player = peek.getPlayer();

        if(peek.getAction() == Action.RIGHT_CLICK_AIR){ //you can also use Action.RIGHT_CLICK_BLOCK
            //do what you want here
        }
    }
}
