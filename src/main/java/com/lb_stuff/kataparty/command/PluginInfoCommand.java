package com.lb_stuff.kataparty.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import static org.bukkit.ChatColor.*;

public class PluginInfoCommand implements CommandExecutor
{
	private final Plugin inst;
	public PluginInfoCommand(Plugin plugin)
	{
		inst = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args.length == 0)
		{
			PluginDescriptionFile d = inst.getDescription();
			sender.sendMessage(""+BOLD+d.getName()+RESET+" v"+d.getVersion()+" by "+d.getAuthors().get(0));
			sender.sendMessage("For help, use "+"/help "+d.getName()+" [page-#]");
			String website = d.getWebsite();
			if(website != null)
			{
				sender.sendMessage(""+AQUA+UNDERLINE+website);
			}
			return true;
		}
		return false;
	}
}
