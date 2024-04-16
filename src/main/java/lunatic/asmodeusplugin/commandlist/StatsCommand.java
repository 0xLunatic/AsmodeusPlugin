package lunatic.asmodeusplugin.commandlist;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("showbelial")) {
            sender.sendMessage("This is the /showbelial command.");
            return true;
        }
        return false;
    }
}
