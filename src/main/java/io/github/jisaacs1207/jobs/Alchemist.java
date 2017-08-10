package io.github.jisaacs1207.jobs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Alchemist implements Listener {

    // unstable transmute - transmutes all blocks in hotbar if shift-rightclick with gold block
    @EventHandler
    public void uTransmute(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (player.getInventory().getItemInOffHand().getType().equals(Material.GOLD_BLOCK)) {

                // Material Maps
                HashMap<String, Material> metalsMap = new HashMap<>();
                HashMap<String, Material> gemsMap = new HashMap<>();
                HashMap<String, Material> woodMap = new HashMap<>();
                HashMap<String, Material> earthMap = new HashMap<>();
                HashMap<String, Material> woolMap = new HashMap<>();
                HashMap<String, Material> glassMap = new HashMap<>();
                HashMap<String, Material> foodMap = new HashMap<>();
                HashMap<String, Material> concreteMap = new HashMap<>();
                HashMap<String, Material> eggMap = new HashMap<>();
                HashMap<String, Material> greensMap = new HashMap<>();

                // Metal
                metalsMap.put("gold", Material.GOLD_BLOCK);
                metalsMap.put("iron", Material.IRON_BLOCK);

                // Gems
                gemsMap.put("diamond", Material.DIAMOND_BLOCK);
                gemsMap.put("emerald", Material.EMERALD_BLOCK);
                gemsMap.put("glowstone", Material.GLOWSTONE);
                gemsMap.put("lapis", Material.LAPIS_BLOCK);
                gemsMap.put("quartz", Material.QUARTZ_BLOCK);

                // Wood
                woodMap.put("log", Material.LOG);

                // Earth
                earthMap.put("dirt", Material.DIRT);
                earthMap.put("stone", Material.STONE);
                earthMap.put("gravel", Material.GRAVEL);
                earthMap.put("log", Material.LOG);
                earthMap.put("log", Material.LOG);
                earthMap.put("log", Material.LOG);
                earthMap.put("log", Material.LOG);
                earthMap.put("log", Material.LOG);

                // Wool

                // Glass

                // Food

                // Concrete


                for (int i = 0; i <= 8; i++) {
                    ItemStack item = player.getInventory().getItem(i); // Get the item in the item slot
                    if (item != null) {
                        for (Object o : metalsMap.entrySet()) {
                            Map.Entry pair = (Map.Entry) o;
                            if (item.getType().equals(pair.getValue())) {
                                Jobs.plugin.getServer().broadcastMessage("Ding");
                            }

                        }
                    }
                }
            }
        }
    }
}
