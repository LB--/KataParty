package com.lb_stuff.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class PluginReloadCommand implements CommandExecutor
{
	private final Plugin inst;
	public PluginReloadCommand(Plugin plugin)
	{
		inst = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args.length == 0)
		{
			PluginDescriptionFile d = inst.getDescription();
			sender.sendMessage("["+d.getName()+"] Reloading/regenerating config...");
			inst.reloadConfig();
			sender.sendMessage("["+d.getName()+"] Done.");
			return true;
		}
		return false;
	}
}
