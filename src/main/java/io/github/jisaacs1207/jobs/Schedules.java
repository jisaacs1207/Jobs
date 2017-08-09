package io.github.jisaacs1207.jobs;

import org.bukkit.entity.Player;

public class Schedules{

    public static class increasePlayTimeSecond implements Runnable {
        @Override
        public void run() {
            for(Player player: Jobs.plugin.getServer().getOnlinePlayers()){
                String playerName=player.getName();
                Methods.addToPlaytime(playerName);
            }
        }
    }

    public static class saveStats implements Runnable {

        @Override
        public void run() {
            for(Player player: Jobs.plugin.getServer().getOnlinePlayers()){
                String playerName=player.getName();
                Methods.saveMapToPFile(playerName);
            }
        }
    }
}
