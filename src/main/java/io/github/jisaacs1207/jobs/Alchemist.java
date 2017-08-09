package io.github.jisaacs1207.jobs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Alchemist implements Listener {

    // unstable transmute - transmutes all blocks in hotbar if shift-rightclick with gold block
    @EventHandler
    public void uTransmute(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) && player.isSneaking()) {
            if ((player.getInventory().getItemInMainHand().getType().equals(Material.GOLD_BLOCK)) ||
                    (player.getInventory().getItemInOffHand().getType().equals(Material.GOLD_BLOCK))) {
                Jobs.plugin.getServer().broadcastMessage("Potato");
            }

        }
    }
}
