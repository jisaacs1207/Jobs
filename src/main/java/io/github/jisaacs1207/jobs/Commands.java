package io.github.jisaacs1207.jobs;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Commands implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
        String cmd = cmnd.getName();
        if ((cmd.equalsIgnoreCase("skills")) || (cmd.equalsIgnoreCase("skill")) ||
                (cmd.equalsIgnoreCase("skil")) || (cmd.equalsIgnoreCase("ski")) ||
                (cmd.equalsIgnoreCase("sk"))) {
            Player player = (Player) sender;

            // no args
            // Shows player skills.
            if (args.length == 0) {
                player.sendMessage("Chickenpotpie");
            }

            // help (<empty>,[all skills],[all classes],[all admin commands],[all commands],<commands>)
            else if (args[0].equalsIgnoreCase("help") && args.length == 1) {
                player.sendMessage("h1");
            } else if (args[0].equalsIgnoreCase("help") && args.length == 2) {
                player.sendMessage("h2");
            }

            // info (<empty>,<playername>)

            // teach (<empty>,<list>,<playername>,<playername> <skill block>,<playernaame> <skillblock> <confirm>)

            // strip (<empty>,<list>,<playername>,<playername> <skill block>,<playername> <skillblock> <confirm>))

            // catchall

            else {
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "SB" + ChatColor.GRAY + "] " +
                        ChatColor.WHITE + "Unknown command.");
            }

        }
        return false;
    }
}

