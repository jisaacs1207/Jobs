package io.github.jisaacs1207.jobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Commands implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
        String cmd = cmnd.getName();
        Player player = (Player) sender;
        if ((cmd.equalsIgnoreCase("skills")) || (cmd.equalsIgnoreCase("skill")) ||
                (cmd.equalsIgnoreCase("skil")) || (cmd.equalsIgnoreCase("ski")) ||
                (cmd.equalsIgnoreCase("sk"))) {

            // no args
            // Shows player skills.
            if (args.length == 0) {
                player.sendMessage("Skills");
            }

            // help (<empty>,[all skills],[all classes],[all admin commands],[all commands],<commands>)
            else if (args[0].equalsIgnoreCase("help") && args.length == 1) {
                player.sendMessage("h1");
            } else if (args[0].equalsIgnoreCase("help") && args.length == 2) {
                player.sendMessage("h2");
            }
        }

        if ((cmd.equalsIgnoreCase("teach")) || (cmd.equalsIgnoreCase("teac")) ||
                (cmd.equalsIgnoreCase("tea")) || (cmd.equalsIgnoreCase("te"))) {
            // no args
            // Shows player skills.
            if (args.length == 0) {
                player.sendMessage("Teach");
            }

            // help (<empty>,[all skills],[all classes],[all admin commands],[all commands],<commands>)
            else if (args[0].equalsIgnoreCase("help") && args.length == 1) {
                player.sendMessage("h1");
            } else if (args[0].equalsIgnoreCase("help") && args.length == 2) {
                player.sendMessage("h2");
            }

        }

        if ((cmd.equalsIgnoreCase("strip")) || (cmd.equalsIgnoreCase("stri")) ||
                (cmd.equalsIgnoreCase("str")) || (cmd.equalsIgnoreCase("st"))) {
            // no args
            // Shows player skills.
            if (args.length == 0) {
                player.sendMessage("Strip");
            }

            // help (<empty>,[all skills],[all classes],[all admin commands],[all commands],<commands>)
            else if (args[0].equalsIgnoreCase("help") && args.length == 1) {
                player.sendMessage("h1");
            } else if (args[0].equalsIgnoreCase("help") && args.length == 2) {
                player.sendMessage("h2");
            }

        }
            
        return false;
    }
}

